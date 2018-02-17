package utopia.sapling.garden

object Gardener
{
    /**
     * Creates a gardener that handles the status with a simple function
     */
    def forFunction[Status](f: (Int, Status) => Unit): Gardener[Status] = new FunctionGardener(f)
    
    private class FunctionGardener[-Status](val f: (Int, Status) => Unit) extends Gardener[Status]
    {
        def observeGrowth(index: Int, status: Status) = f(index, status)
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
     * @param index the index of the sapling the event concerns
     * @param status the current status of the sapling
     */
    def observeGrowth(index: Int, status: Status)
}