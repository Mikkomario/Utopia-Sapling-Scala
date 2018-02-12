package utopia.sapling.test

import utopia.sapling.util.ActionBuffer
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import utopia.sapling.util.WaitUtils

/**
 * This is a simple implementation test for the action buffer class
 * @author Mikko Hilpinen
 * @since 11.2.2018
 */
object ActionBufferTest extends App
{
    val service = Executors.newFixedThreadPool(16)
    implicit val context = ExecutionContext.fromExecutor(service)
    
    val buffer = new ActionBuffer()
    context.execute(buffer)
    
    println("Adding first print to buffer...")
    buffer += (() => println("First buffer print"))
    
    println("Adding second print to buffer...")
    buffer += (() => println("Second buffer print"))
    
    println("Done, closing buffer")
    buffer.close()
    
    service.shutdown()
}