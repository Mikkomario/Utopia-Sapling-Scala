package utopia.sapling.garden

import scala.concurrent.ExecutionContext
import utopia.sapling.util.WaitUtils

/**
* A sapden is a sapling that uses a garden to handle multiple simultaneous processes as a single 
* process
* @author Mikko Hilpinen
* @since 9.2.2018
**/
class Sapden[Status, StatusPart, Fruit, FruitPart, Remains, RemainsPart](
        val saplings: Seq[Sapling[StatusPart, FruitPart, RemainsPart]], 
        val parseStatus: Seq[StatusPart] => Status, 
        val parseResult: (Vector[FruitPart], Vector[RemainsPart]) => Either[Remains, Fruit])(
        implicit context: ExecutionContext) extends Sapling[Status, Fruit, Remains]
{
    // ATTRIBUTES    -----------------------
    
    private val garden = new Garden[StatusPart, FruitPart, RemainsPart]()
    
    private var collectedFruit = Vector[FruitPart]()
    private var collectedRemains = Vector[RemainsPart]()
    
    private var cachedStatus: Option[Status] = None
    private var started = false
    
    
    // INITIAL CODE    --------------------
    
    // Sets up the garden
    garden += Gardener.forFunction[StatusPart](s => 
    {
        invalidateStatus()
        this.synchronized(notifyAll())
    })
    garden += Harvester.forFunction[FruitPart](fruit => 
    {
        collectedFruit :+= fruit
        this.synchronized(notifyAll())
    })
    garden += Researcher.forFunction[RemainsPart](remains => 
    {
        collectedRemains :+= remains
        this.synchronized(notifyAll())
    })
    
    
    // COMPUTED PROPERTIES    -------------
    
    /**
     * Whether all the subprocesses have completed already
     */
    def isReady = collectedFruit.size + collectedRemains.size >= saplings.size
    
    
    // IMPLEMENTED METHODS & PROPS    ----
    
    override def status = 
    {
        val newStatus = cachedStatus.getOrElse(parseStatus(saplings.map(_.status)))
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
	    WaitUtils.waitForever(this)
	    
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
	        Growing(this) // While growing, keeps returning this mutating sapling
	}
	
	
	// OTHER METHODS    ------------------
	
	private def invalidateStatus() = cachedStatus = None
}