package utopia.sapling.garden

object Harvester
{
    /**
     * Creates a harvester that handles the received fruit with a simple function
     */
    def forFunction[Fruit](f: (Int, Fruit) => Unit): Harvester[Fruit] = new FunctionHarvester(f)
    
    private class FunctionHarvester[-Fruit](val f: (Int, Fruit) => Unit) extends Harvester[Fruit]
    {
        def receive(index: Int, fruit: Fruit) = f(index, fruit)
    }
}

/**
 * A harvester is informed of final results on a sapling
 * @author Mikko Hilpinen
 * @since 8.2.2018
 */
trait Harvester[-Fruit]
{
    /**
     * Receives a grown fruit from a sapling growth process
     * @param index the index of the sapling the fruit is from
     * @fruit the results of the growth process
     */
    def receive(index: Int, fruit: Fruit)
}