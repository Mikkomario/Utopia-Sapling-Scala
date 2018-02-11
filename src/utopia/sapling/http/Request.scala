package utopia.sapling.http

import utopia.flow.datastructure.template
import utopia.access.http.Method
import utopia.access.http.Method._
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import utopia.access.http.Headers
import java.io.File
import scala.collection.immutable.HashMap
import scala.collection.immutable.Map
import utopia.flow.datastructure.immutable.Value

/**
 * Requests are used for requesting certain data and for performing certain actions on server side 
 * over a http connection
 * @author Mikko Hilpinen
 * @since 26.11.2017
 */
class Request(val requestUri: String, val method: Method = Get, 
        val params: Model[Constant] = Model(Vector()), 
        val headers: Headers = Headers.currentDateHeaders, val uploads: Map[String, File] = HashMap())
{
    // OPERATORS    --------------------
    
    def +(parameter: Constant) = new Request(requestUri, method, params + parameter, headers, uploads)
    
    def +(parameter: Tuple2[String, Value]): Request = this + new Constant(parameter._1, parameter._2)
    
    def +(uploadName: String, uploadFile: File) = new Request(requestUri, method, params, headers, 
            uploads + (uploadName -> uploadFile))
    
    def ++(params: template.Model[Constant]) = new Request(requestUri, method, 
            this.params ++ params, headers, uploads)
    
    def ++(uploads: Map[String, File]) = new Request(requestUri, method, params, headers, 
            this.uploads ++ uploads)
    
    
    // OTHER METHODS    ----------------
    
    def withModifiedHeaders(mod: Headers => Headers) = new Request(requestUri, method, params, 
            mod(headers), uploads)
}