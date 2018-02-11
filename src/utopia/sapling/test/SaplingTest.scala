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
    implicit val context = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(16))
    implicit val random = new Random()
    
    val garden = new Garden[Double, Seq[Int], Unit]()
    garden += Gardener.forFunction(println)
    garden += Harvester.forFunction(println)
    
    println("Starting...")
    garden.plant(new FibonacciSapling(10))
}

private class FibonacciSapling(val targetNumberAmount: Int, 
        val collectedNumbers: Seq[Int] = Vector(1, 2))(implicit val random: Random) extends Sapling[Double, Seq[Int], Unit]
{
    def status = collectedNumbers.size.toDouble / targetNumberAmount
    
    def grow() = 
    {
        if (collectedNumbers.size >= targetNumberAmount)
        {
            Grown(collectedNumbers)
        }
        else
        {
            WaitUtils.wait(this, random.nextInt(2000))
            Growing(new FibonacciSapling(targetNumberAmount, 
                    collectedNumbers :+ collectedNumbers.takeRight(2).reduce(_ + _)))
        }
    }
}