package utopia.sapling.test

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import utopia.sapling.garden.Sapden
import scala.util.Random
import utopia.sapling.garden.Garden
import utopia.sapling.garden.Gardener
import utopia.sapling.garden.Harvester
import utopia.sapling.util.WaitUtils
import scala.concurrent.ExecutionContext

/**
 * This app tests the Sapden class
 * @author Mikko Hilpinen
 * @since 12.2.2018
 */
object SapdenTest extends App
{
    // Creates the context
    val service = Executors.newCachedThreadPool()
    implicit val context = ExecutionContext.fromExecutor(service)
    implicit val random = new Random()
    
    // Creates the saplings
    val sapling1 = new FibonacciSapling(5)
    val sapling2 = new FibonacciSapling(5, Vector(5, 10))
    val sapling3 = new FibonacciSapling(5, Vector(4, 5))
    
    // Reduce functions
    // def parseStatus(s: Seq[Double]) = s.reduce(_ + _) / s.size
    def parseStatus(s: Any) = s.toString()
    def parseResults(successes: Vector[Seq[Int]], failures: Vector[Unit]) = 
    {
        if (successes.isEmpty)
        {
            Left("No data found")
        }
        else
        {
            Right(successes.flatten.sorted)
        }
    }
    
    // Creates the sapden
    val sapden = new Sapden(Vector(sapling1, sapling2, sapling3), parseStatus, parseResults)
    
    // Sets up the garden
    val garden = new Garden[String, Vector[Int], String]()
    garden += Gardener.forFunction((_, status: String) => println(status))
    garden += Harvester.forFunction((_, fruit: Vector[Int]) => println(fruit))
    
    // Starts the process
    garden.plant(sapden)
    
    // The context will be terminated when the process is completed
    // service.shutdown()
    WaitUtils.wait(this, 30000)
}