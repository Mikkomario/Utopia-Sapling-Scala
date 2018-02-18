package utopia.sapling.garden

import scala.concurrent.ExecutionContext
import utopia.sapling.util.WaitUtils
import scala.collection.immutable.HashMap
import java.util.concurrent.Executors

/**
* A sapden is a sapling that uses a garden to handle multiple simultaneous processes as a single 
* process
* @author Mikko Hilpinen
* @since 9.2.2018
**/
class Sapden[Status, StatusPart, Fruit, FruitPart, Remains, RemainsPart](
        val saplings: Seq[Sapling[StatusPart, FruitPart, RemainsPart]], 
        val parseStatus: Iterable[StatusPart] => Status, 
        val parseResult: (Vector[FruitPart], Vector[RemainsPart]) => Either[Remains, Fruit]) 
        extends Sapling[Status, Fruit, Remains]
{
    // ATTRIBUTES    -----------------------
    
    private val threadPool = Executors.newFixedThreadPool(saplings.size * 2)
    private val garden = new Garden[StatusPart, FruitPart, RemainsPart]()(
            ExecutionContext.fromExecutorService(threadPool));
    
    private var collectedFruit = Vector[FruitPart]()
    private var collectedRemains = Vector[RemainsPart]()
    private var collectedStatus = HashMap[Int, StatusPart]()
    
    private var cachedStatus: Option[Status] = None
    private var started = false
    private var completed = false
    
    
    // INITIAL CODE    --------------------
    
    // Sets up the garden
    garden += Gardener.forFunction[StatusPart]((index, status) => 
    {
        this.synchronized
        {
            collectedStatus += (index -> status)
            invalidateStatus()
            notifyAll()
        }
    })
    garden += Harvester.forFunction[FruitPart]((_, fruit) => 
    {
        this.synchronized
        {
            collectedFruit :+= fruit
            checkIfCompleted()
            notifyAll()
        }
    })
    garden += Researcher.forFunction[RemainsPart]((_, remains) => 
    {
        this.synchronized
        {
            collectedRemains :+= remains
            checkIfCompleted()
            notifyAll()
        }
    })
    
    
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
	        saplings.foreach(garden.plant(_))
	    }
	    
	    // Waits between each update
	    if (!completed)
	    {
	        WaitUtils.waitForever(this)
	    }
	    
	    if (completed)
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
	
	private def invalidateStatus() = cachedStatus = None
	
	private def checkIfCompleted() = 
	{
	    if (!completed && collectedFruit.size + collectedRemains.size >= saplings.size)
	    {
	        completed = true
	        shutDownGarden()
	    }
	}
	
	private def shutDownGarden() = 
	{
	    garden.clear()
	    threadPool.shutdown()
	}
}