package utopia.sapling.util

/**
* A counter is a class that keeps track of a single running number, which can be increased 
* synchronously, without fear of using the same number twice even in a multi threaded environment
* @author Mikko Hilpinen
* @since 17.2.2018
**/
class Counter(val firstNumber: Int = 0, val lastNumber: Int = Int.MaxValue)
{
    // ATTRIBUTES    --------------------
    
    private var number = firstNumber
    
    
    // OTHER METHODS    -----------------
    
    /**
     * Gets a new number from this counter
     */
	def next() = 
	{
	    this.synchronized
	    {
	        if (number == lastNumber)
	            number = firstNumber;
	        else
	            number += 1;
	        
	        number
	    }
	}
    
    /**
     * Resets the counter back to the starting value
     */
    def reset() = this.synchronized(number = firstNumber)
}