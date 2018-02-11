package utopia.sapling.http

import utopia.access.http.Status
import utopia.access.http.Headers

/**
 * Responses are sent by a server. Responses have a specific status and may contain a body 
 * section. Context and content type determine, how the response body is parsed.
 * @author Mikko Hilpinen
 * @since 30.11.2017
 */
class Response(val status: Status, val headers: Headers)
{
    
}