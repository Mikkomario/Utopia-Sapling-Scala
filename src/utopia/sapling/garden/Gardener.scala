package utopia.sapling.garden

object Gardener
{
    /**
     * Creates a gardener that handles the status with a simple function
     */
    def forFunction[Status](f: Status => Unit): Gardener[Status] = new FunctionGardener(f)
    
    private class FunctionGardener[-Status](val f: Status => Unit) extends Gardener[Status]
    {
        def observeGrowth(status: Status) = f(status)
    }
}

/**
 * A gardener is informed and keeps track of the growth of a sapling
 * @author Mikko Hilpinen
 */
trait Gardener[-Status]
{
    /**
     * This method will be called as the sapling grows and it's status changes
     * @param status the current status of the sapling
     */
    def observeGrowth(status: Status)
}