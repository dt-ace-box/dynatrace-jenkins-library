@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' )

import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

// Run on a Jenkins agent with minimal resources required.

class param{
  String key
  String value
}

@NonCPS // What is this?
def DynatraceAPICall(Map args)
    /*  String dtTenantUrl,
        String dtApiToken

        String httpMethod
        String urlPath

        Optional Parameters:
        String payload (Required for POST, PUT http methods)
        Map optionalParameters (may contian multiple parameters)
        Closure successFunction
        Closure failureFunction
    */
{
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String httpMethod = args.containsKey("method") ? args.method : "GET"
  String urlPath = args.containsKey("urlPath") ? args.urlPath : null
  String payload = args.containsKey("payload") ? args.payload : null // Not all requests contain a payload
  Map optionalParameters = args.containsKey("options") ? args.options : []

  // Check for pre-populated success/failure functions.
  Closure successFunction = args.containsKey("onSuccess") ? args.onSuccess : null
  Closure failureFunction = args.containsKey("onFailure") ? args.onFailure : null

  // Define base return code.
  int retCode = 0

  String queryParameters = ''

  // Apply all optional parameters to the request.
  for (key in optionalParameters.keySet().sort()) {
    queryParameters += "${key}=${optionalParameters[key]}&"
  }
  // Crop off the extra '&' character at the end of the parameter string.
  optionalParameters = optionalParameters.substring(0, optionalParameters.length() - 1)

  // Build the API call
  String apiCall = "${dtTenantUrl}/api/v1/${urlPath}?${queryParameters}"

  // Make the API call, use a default failure handler.
  def http = new HTTPBuilder(apiCall)
  http.request(POST, JSON) { req ->
    headers.'Authorization' = 'Api-Token ' + dtApiToken
    headers.'Content-Type' = 'application/json' //All of the API uses JSON, AFAIK
    body = payload
    response.success = successFunction ? successFunction : { resp, json -> }
    response.failure = failureFunction ? failureFunction : { resp, json ->
      throw new Exception(
        "HTTP Request failure.\n" +
        "HTTP Endpoint: ${apiCall}\n"
        "Status Code: ${resp.status}\n" +
        "Response body: ${json}"
    }
  }
  return retCode
}
