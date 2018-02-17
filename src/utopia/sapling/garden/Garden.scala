package utopia.sapling.garden

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import utopia.sapling.util.ActionBuffer
import utopia.sapling.util.Counter

/**
 * A yard can hold a single sapling. The yard has personnel observing the growth of the sapling.
 * @author Mikko Hilpinen
 * @since 7.2.2018
 */
class Garden[Status, Fruit, Remains](val indexCounter: Counter = new Counter())
        (implicit private val context: ExecutionContext)
{
    // ATTRIBUTES    ------------------
    
    private var _gardeners = Vector[Gardener[Status]]()
    private var _harvesters = Vector[Harvester[Fruit]]()
    private var _researchers = Vector[Researcher[Remains]]()
    
    
    // COMPUTED PROPERTIES    ---------
    
    /**
     * The current gardeners inside the garden (handle status updates)
     */
    def gardeners = _gardeners
    /**
     * The current harvesters inside the garden (handle results)
     */
    def harvesters = _harvesters
    /**
     * The current researchers in the garden (handle errors)
     */
    def researchers = _researchers
    
    
    // OPERATORS    -------------------
    
    def +=(gardener: Gardener[Status]) = assignGarderer(gardener)
    def +=(harvester: Harvester[Fruit]) = assignHarvester(harvester)
    def +=(researcher: Researcher[Remains]) = assignResearcher(researcher)
    
    /**
     * Removes a worker from this garden
     */
    def -=(worker: AnyRef) = dismiss(worker)
    
    
    // OTHER METHODS    ---------------
    
    /**
     * Plants a new seed to the garden. The progress is monitored by the gardeners
     * @param seed a seed that will produce a sapling. The sapling will start to grow immediately
     * @return A generated index for the newly created sapling
     */
    def plant(seed: => Sapling[Status, Fruit, Remains]) = 
    {
        println("\tGarden planting a new seed")
        // The workers are informed in a separate thread
        val buffer = new ActionBuffer()
        context.execute(buffer)
        
        println("\tNew seed planted")
        val index = indexCounter.next()
        Future(resultFromSapling(seed, buffer, index))
        
        index
    }
    
    /**
     * Assigns a new gardener to handle status updates
     */
    def assignGarderer(g: Gardener[Status]) = _gardeners :+= g
    /**
     * Assigns a new harvester to handle results updates
     */
    def assignHarvester(h: Harvester[Fruit]) = _harvesters :+= h
    /**
     * Assigns a new researcher to handle failed results
     */
    def assignResearcher(r: Researcher[Remains]) = _researchers :+= r
    
    /**
     * Dismisses a worker of any position from the garden
     */
    def dismiss(worker: AnyRef) = 
    {
        _gardeners = _gardeners.filterNot(_ == worker)
        _harvesters = _harvesters.filterNot(_ == worker)
        _researchers = _researchers.filterNot(_ == worker)
    }
    
    /**
     * Clears the garden of any workers
     */
    def clear() = 
    {
        _gardeners = Vector()
        _harvesters = Vector()
        _researchers = Vector()
    }
    
    private def resultFromSapling(sapling: Sapling[Status, Fruit, Remains], 
            actionBuffer: ActionBuffer, index: Int): Either[Remains, Fruit] = 
    {
        // Grows the sapling forward and informs the workers
        actionBuffer += (() => gardeners.foreach(_.observeGrowth(index, sapling.status)))
        
        sapling.grow() match 
        {
            case Growing(next) => resultFromSapling(next, actionBuffer, index)
            case Grown(fruit) => 
                actionBuffer += (() => harvesters.foreach(_.receive(index, fruit)))
                actionBuffer.close()
                Right(fruit)
            case Wilted(remains) => 
                actionBuffer += (() => researchers.foreach(_.observe(index, remains)))
                actionBuffer.close()
                Left(remains)
        }
    }
    
    // Check: https://stackoverflow.com/questions/4511078/how-can-i-execute-multiple-tasks-in-scala
}