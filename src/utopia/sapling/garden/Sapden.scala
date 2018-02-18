package utopia.sapling.garden

import scala.concurrent.ExecutionContext
import utopia.sapling.util.WaitUtils
import scala.collection.immutable.HashMap
import scala.collection.immutable.Traversable
import java.util.concurrent.Executors

/**
* A sapden is a sapling that uses a garden to handle multiple simultaneous processes as a single 
* process
* @author Mikko Hilpinen
* @since 9.2.2018
**/
class Sapden[Status, StatusPart, Fruit, FruitPart, Remains, RemainsPart](
        val saplings: Traversable[Sapling[StatusPart, FruitPart, RemainsPart]], 
        val parseStatus: Iterable[StatusPart] => Status, 
        val parseResult: (Vector[FruitPart], Vector[RemainsPart]) => Either[Remains, Fruit]) 
        extends Sapling[Status, Fruit, Remains]
{
    // ATTRIBUTES    -----------------------
    
    private var collectedFruit = Vector[FruitPart]()
    private var collectedRemains = Vector[RemainsPart]()
    private var collectedStatus = HashMap[Int, StatusPart]()
    
    private var cachedStatus: Option[Status] = None
    private var started = false
    
    
    // COMPUTED PROPERTIES    -------------
    
    def isReady = collectedFruit.size + collectedRemains.size >= saplings.size
    
    
    // IMPLEMENTED METHODS & PROPS    ----
    
    override def status = 
    {
        val newStatus = cachedStatus.getOrElse(parseStatus(collectedStatus.values))
        cachedStatus = Some(newStatus)
        newStatus
    }
    
	override def grow() = 
	{
	    // Starts the growth process if not already started
	    if (!started)
	    {
	        started = true
	        // Starts growing the saplings
            Garden.grow(saplings, Gardener.forFunction(statusUpdate), 
                    Harvester.forFunction(fruitUpdate), Researcher.forFunction(remainsUpdate));
	    }
	    
	    // Waits between each update
	    if (!isReady)
	    {
	        WaitUtils.waitForever(this)
	    }
	    
	    if (isReady)
	    {
	        // Parses results once ready
	        parseResult(collectedFruit, collectedRemains) match 
    	    {
    	        case Left(remains) => Wilted(remains)
    	        case Right(fruit) => Grown(fruit)
    	    }
	    }
	    else
	    {
	        Growing(this) // While growing, keeps returning this mutating sapling
	    }
	}
	
	
	// OTHER METHODS    ------------------
	
	private def statusUpdate(index: Int, status: StatusPart) = 
	{
	    this.synchronized
        {
            collectedStatus += (index -> status)
            invalidateStatus()
            notifyAll()
        }
	}
	
	private def fruitUpdate(index: Any, fruit: FruitPart) = 
	{
	    this.synchronized
        {
            collectedFruit :+= fruit
            notifyAll()
        }
	}
	
	private def remainsUpdate(index: Any, remains: RemainsPart) = 
	{
	    this.synchronized
        {
            collectedRemains :+= remains
            notifyAll()
        }
	}
	
	private def invalidateStatus() = cachedStatus = None
}