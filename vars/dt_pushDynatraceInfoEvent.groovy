@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' )

import groovyx.net.http.HTTPBuilder
import groovy.json.JsonOutput
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

/***************************\
  This function assumes we run on a standard Jenkins Agent.

  Returns either 0(=no errors), 1(=pushing event failed)
\***************************/
@NonCPS
def call( Map args ) {
  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""

  String description = args.containsKey("description") ? args.description : ""
  String source = args.containsKey("source") ? args.deploymentVersion : "Jenkins"
  String title = args.containsKey("title") ? args.deploymentProject : ""


  def customProperties = args.containsKey("customProperties") ? args.customProperties : [ ]

  // check minimum required params
  if(tagRule == "" ) {
    echo "tagRule is a mandatory parameter!"
    return 1
  }

  String eventType = "CUSTOM_INFO"

  def postBody = new Map([
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    description: description,
    customProperties: customProperties,
    tags: tagRule[0].tags,
    source: source
  ])


  def http = new HTTPBuilder( dtTenantUrl + '/api/v1/events' );

  http.request( POST, JSON ) { req ->
    headers.'Authorization' = "Api-Token ${dtApiToken}"
    headers.'Content-Type' = 'application/json'

    body = postBody

    response.success = { resp, json ->
      echo "Event Posted Successfully! ${resp.status}"
    }
    response.failure = { resp, json ->
      echo "[dt_pushDynatraceInfoEvent] Failed To Post Event: " + resp.toMapString()
      return 1
    }
  }
  return 0
}
