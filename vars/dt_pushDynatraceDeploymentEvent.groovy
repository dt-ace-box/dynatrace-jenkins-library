@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' )

import groovyx.net.http.HTTPBuilder
import groovy.json.JsonOutput
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

/***************************\
  This function assumes we run on a standard Jenkins Agent.

  Returns either 0(=no errors), 1(=pushing event failed)
\***************************/
def call( Map args )

    /*  String dtTenantUrl,
        String dtApiToken
        def tagRule

        String deploymentName
        String deploymentVersion
        String deploymentProject
        String ciBackLink
        String remediationAction

        def customProperties
    */
{
    // check input arguments
    String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
    String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
    def tagRule = args.containsKey("tagRule") ? args.tagRule : ""

    String deploymentName = args.containsKey("deploymentName") ? args.deploymentName : "${env.JOB_NAME}"
    String deploymentVersion = args.containsKey("deploymentVersion") ? args.deploymentVersion : "${env.VERSION}"
    String deploymentProject = args.containsKey("deploymentProject") ? args.deploymentProject : ""
    String ciBackLink = args.containsKey("ciBackLink") ? args.ciBackLink : "${env.BUILD_URL}"
    String remediationAction = args.containsKey("remediationAction") ? args.remediationAction : "null"


    def customProperties = args.containsKey("customProperties") ? args.customProperties : [ ]

    // check minimum required params
    if(tagRule == "" ) {
        echo "tagRule is a mandatory parameter!"
        return 1
    }

    String eventType = "CUSTOM_DEPLOYMENT"

    def http = new HTTPBuilder( dtTenantUrl + '/api/config/v1/events' )
    http.request( POST, JSON ) { req ->
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      headers.'Content-Type' = 'application/json'
      body = [
        eventType: eventType,
        attachRules: {
          tagRule: [{
            meTypes: [
              tagRule[0].meTypes[0].meType
            ]
          }]
        }
        deploymentName: deploymentName,
        deploymentVersion: deploymentVersion,
        deploymentProject: deploymentProject,
        ciBackLink: ciBackLink,
        remediationAction: remediationAction,
        tags: tagRule[0].tags,
        description: description,
        source: "Jenkins",
        configuration: configuration,
        customProperties: customProperties
      ]
      response.success = { resp, json ->
        println "Config Event Posted Successfully! ${resp.status}"
      }
      response.failure = { resp, json ->
        throw new Exception("Failed to POST Configuration Event. \nargs: \n${args.toMapString()}")
      }
    }
    return 0
}