package utopia.sapling.util

/**
 * WaitUtils contains a number of utility tools for waiting on a thread. This utility object handles 
 * possible interruptedExceptions as well as synchronization
 * @author Mikko Hilpinen
 * @since 8.2.2018
 */
@deprecated("This object has been moved to Flow", "after v1")
object WaitUtils
{
    /**
     * Waits for a certain amount of milliseconds (blocking), then releases the lock
     */
    def wait(lock: AnyRef, millis: Long) = waitUntil(lock, System.currentTimeMillis() + millis)
    
    /**
     * Waits until the lock is notified
     */
    def waitForever(lock: AnyRef) = 
    {
        lock.synchronized
        {
            var waiting = true
            while (waiting)
            {
                try
                {
                    lock.wait()
                    waiting = false
                }
                catch
                {
                    // InterrupredExceptions are ignored
                    case _: InterruptedException => Unit
                }
            }
        }
    }
    
    private def waitUntil(lock: AnyRef, targetTimeMillis: Long) = 
    {
        lock.synchronized
        {
            var currentTimeMillis = System.currentTimeMillis()
            while (currentTimeMillis < targetTimeMillis)
            {
                try
                {
                    lock.wait(targetTimeMillis - currentTimeMillis)
                }
                catch
                {
                    case _: InterruptedException => Unit
                }
                
                currentTimeMillis = System.currentTimeMillis()
            }
        }
    }
}