package utopia.sapling.util

/**
 * Action buffers are used for running actions / operations synchronously back to back in an 
 * asynchronouse environment
 * @author Mikko Hilpinen
 */
class ActionBuffer extends Runnable
{
    // ATTRIBUTES    --------------------------
    
    private var buffer = Vector[() => Any]()
    private var ended = false
    
    
    // IMPLEMENTED METHODS    -----------------
    
    override def run() = 
    {
        while (!ended || !buffer.isEmpty)
        {
            // Waits until the next action is received
            while (buffer.isEmpty && !ended)
            {
                WaitUtils.waitForever(this)
            }
            
            // Performs the action
            if (!buffer.isEmpty)
            {
                buffer.headOption.foreach(_())
                buffer.synchronized(buffer = buffer.tail)
            }
        }
    }
    
    
    // OTHER METHODS    ----------------------
    
    /**
     * Adds a new action to this buffer. The action will be run once the buffer is empty
     */
    def +=(action: () => Any) = 
    {
        buffer.synchronized(buffer :+= action)
        this.synchronized(notifyAll())
    }
    
    /**
     * Closes this buffer so that it will end once empty. This buffer shouldn't be used after this 
     * method has been called.
     */
    def close() = 
    {
        ended = true
        this.synchronized(notifyAll())
    }
}