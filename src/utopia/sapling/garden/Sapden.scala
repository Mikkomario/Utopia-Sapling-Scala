package utopia.sapling.garden

import scala.concurrent.ExecutionContext
import utopia.sapling.util.WaitUtils
import scala.collection.immutable.HashMap

/**
* A sapden is a sapling that uses a garden to handle multiple simultaneous processes as a single 
* process
* @author Mikko Hilpinen
* @since 9.2.2018
**/
class Sapden[Status, StatusPart, Fruit, FruitPart, Remains, RemainsPart](
        val saplings: Seq[Sapling[StatusPart, FruitPart, RemainsPart]], 
        val parseStatus: Iterable[StatusPart] => Status, 
        val parseResult: (Vector[FruitPart], Vector[RemainsPart]) => Either[Remains, Fruit])(
        implicit context: ExecutionContext) extends Sapling[Status, Fruit, Remains]
{
    // ATTRIBUTES    -----------------------
    
    private val garden = new Garden[StatusPart, FruitPart, RemainsPart]()
    
    private var collectedFruit = Vector[FruitPart]()
    private var collectedRemains = Vector[RemainsPart]()
    private var collectedStatus = HashMap[Int, StatusPart]()
    
    private var cachedStatus: Option[Status] = None
    private var started = false
    
    
    // INITIAL CODE    --------------------
    
    // Sets up the garden
    garden += Gardener.forFunction[StatusPart]((index, status) => 
    {
        println("\tSapden status update")
        this.synchronized
        {
            collectedStatus += (index -> status)
            invalidateStatus()
            notifyAll()
        }
    })
    garden += Harvester.forFunction[FruitPart]((_, fruit) => 
    {
        println("\tSapden fruit update")
        this.synchronized
        {
            collectedFruit :+= fruit
            notifyAll()
        }
    })
    garden += Researcher.forFunction[RemainsPart]((_, remains) => 
    {
        println("\tSapden remains update")
        this.synchronized
        {
            collectedRemains :+= remains
            notifyAll()
        }
    })
    
    
    // COMPUTED PROPERTIES    -------------
    
    /**
     * Whether all the subprocesses have completed already
     */
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
	        println("\tStarts the sapling")
	        started = true
	        saplings.foreach(garden.plant(_))
	    }
	    
	    // Waits between each update
	    println("\tProceeds to wait")
	    if (!isReady)
	    {
	        WaitUtils.waitForever(this)
	    }
	    
	    if (isReady)
	    {
	        println("\tCreates the final results")
	        // Parses results once ready
	        parseResult(collectedFruit, collectedRemains) match 
    	    {
    	        case Left(remains) => Wilted(remains)
    	        case Right(fruit) => Grown(fruit)
    	    }
	    }
	    else
	        Growing(this) // While growing, keeps returning this mutating sapling
	}
	
	
	// OTHER METHODS    ------------------
	
	private def invalidateStatus() = cachedStatus = None
}