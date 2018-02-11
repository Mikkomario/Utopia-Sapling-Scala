package utopia.sapling.garden

object Researcher
{
    /**
     * Creates a new researcher that handles the incoming remains with a simple function
     */
    def forFunction[Remains](f: Remains => Unit): Researcher[Remains] = new FunctionResearcher(f)
    
    private class FunctionResearcher[-Remains](val f: Remains => Unit) extends Researcher[Remains] 
    {
        def observe(remains: Remains) = f(remains)
    }
}

/**
 * A researcher is interested in failed sapling growth processes
 * @author Mikko Hilpinen
 * @since 7.2.2018
 */
trait Researcher[-Remains]
{
    /**
     * Informes this researhcer about a failed growth process
     * @param remains the remains of the growth process
     */
    def observe(remains: Remains)
}