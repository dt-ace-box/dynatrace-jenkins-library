import groovy.json.JsonOutput

// Why does this look so strange...?
import ../src/dt_eventAPI

/***************************\
  This Function should run on a standard Jenkins Agent.

  Returns either 0(=no errors), 1(=pushing event failed)
\***************************/
def call( Map args )
    /*  String dtTenantUrl,
        String dtApiToken
        def tagRule

        String description
        String source
        String configuration

        def customProperties
    */
{
    String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
    String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
    def tagRule = args.containsKey("tagRule") ? args.tagRule : ""

    String description = args.containsKey("description") ? args.description : ""
    String source = args.containsKey("source") ? args.source : ""
    String configuration = args.containsKey("configuration") ? args.configuration : ""

    def customProperties = args.containsKey("customProperties") ? args.customProperties : [ ]

    // Assert mandatory parameters.
    if(tagRule == "" ) {
        echo "tagRule is a mandatory parameter!"
        return 1
    }

    String eventType = "CUSTOM_CONFIGURATION"

    return DynatraceAPICall([
        'dtTenantUrl': dtTenantUrl,
        'dtApiToken': dtApiToken,
        'httpMethod': 'POST',
        'urlPath': 'events',
        'payload': [
            eventType: eventType,
            attachRules: {
                tagRule: [{
                    meTypes: [tagRule[0].meTypes[0].meType]
                }]
            },
            tags: tagRule[0].tags,
            description: description,
            source: source,
            configuration: configuration,
            customProperties: customProperties
        ],
        'optionalParameters': []
    ]);
}
