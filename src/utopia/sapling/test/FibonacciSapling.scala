package utopia.sapling.test

import utopia.sapling.garden.Grown
import utopia.sapling.util.WaitUtils
import utopia.sapling.garden.Growing
import scala.util.Random
import utopia.sapling.garden.Sapling

/**
* This is a simple test implementation of the Sapling trait which counts fibonacci numbers
* @author Mikko
* @since 12.2.2018
**/
class FibonacciSapling(val targetNumberAmount: Int, 
        val collectedNumbers: Seq[Int] = Vector(1, 2))(implicit val random: Random) extends Sapling[Double, Seq[Int], Unit]
{
    def status = collectedNumbers.size.toDouble / targetNumberAmount
    
    def grow() = 
    {
        if (collectedNumbers.size >= targetNumberAmount)
        {
            println("\tFibonacci sapling full grown")
            Grown(collectedNumbers)
        }
        else
        {
            println("Fibonacci sapling growing")
            WaitUtils.wait(this, random.nextInt(2000))
            Growing(new FibonacciSapling(targetNumberAmount, 
                    collectedNumbers :+ collectedNumbers.takeRight(2).reduce(_ + _)))
        }
    }
}