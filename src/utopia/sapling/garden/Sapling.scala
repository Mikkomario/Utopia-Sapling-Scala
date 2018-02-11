package utopia.sapling.garden

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * A sapling represents an ongoing process that either yields successful or failing results. The 
 * status of the process changes as time goes on. In case of very simple asynchronous processes, 
 * a Future may be a better alternative.
 * @param Status the type of measurement used for the sapling's growth
 * @param Fruit the type of results of the sapling's successful growth process
 * @param Remains the type of results of the sapling's failed growth process
 * @author Mikko Hilpinen
 * @since 6.2.2018
 */
trait Sapling[+Status, +Fruit, +Remains]
{
    // ABSTRACT METHODS & PROPS    ------------
    
    /**
     * The current status of the sapling
     */
    def status: Status
    
    /**
     * Calling this method grows the sapling to the next stage. This method may / should block until 
     * the next status is reached
     */
    def grow(): GrowthResult[Status, Fruit, Remains]
    
    
    // OTHER METHODS       -------------------
    
    /**
     * Converts this sapling into a future, starting the growth process
     * @return a future that yields either the grown fruit or the remains of a failed growth process
     */
    def toFuture()(implicit context: ExecutionContext) = Future(getFruit)
    
    private def getFruit(): Either[Remains, Fruit] = 
    {
        grow() match 
        {
            case Growing(_) => getFruit()
            case Grown(fruit) => Right(fruit)
            case Wilted(remains) => Left(remains)
        }
    }
}

/**
 * Different growth results represent different states a sapling may grow into
 */
sealed trait GrowthResult[+Status, +Fruit, +Remains]
/**
 * The sapling is still growing. A still ongoing process with a certain measurable status.
 * @param nextStage the next stage of this sapling with a different status
 */
case class Growing[+Status, +Fruit, +Remains](nextStage: Sapling[Status, Fruit, Remains]) extends 
        GrowthResult[Status, Fruit, Remains]
/**
 * The sapling has reached a successful final state. A result of a successful growth process.
 * @param fruit The results of the growth process
 */
case class Grown[+Status, +Fruit, +Remains](fruit: Fruit) extends GrowthResult[Status, Fruit, Remains]
/**
 * The sapling failed to reach a successful state and wilted. A result of a failed growth process.
 * @param remains the results / description of the failed growth process
 */
case class Wilted[+Status, +Fruit, +Remains](remains: Remains) extends GrowthResult[Status, Fruit, Remains]