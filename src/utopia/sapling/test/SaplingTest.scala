package utopia.sapling.test

import scala.util.Random
import utopia.sapling.garden.Sapling
import utopia.sapling.garden.Grown
import utopia.sapling.garden.Growing
import utopia.sapling.util.WaitUtils
import utopia.sapling.garden.Garden
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import utopia.sapling.garden.Gardener
import utopia.sapling.garden.Harvester

/**
 * This test creates a simple implementation of the sapling interface
 * @author Mikko Hilpinen
 * @since 11.2.2018
 */
object SaplingTest extends App
{
    val service = Executors.newFixedThreadPool(16)
    implicit val context = ExecutionContext.fromExecutor(service)
    implicit val random = new Random()
    
    val garden = new Garden[Double, Seq[Int], Unit]()
    garden += Gardener.forFunction((_, status: Any) => println(status))
    garden += Harvester.forFunction((_, status: Any) => println(status))
    
    println("Starting...")
    garden.plant(new FibonacciSapling(10))
    
    service.shutdown()
}