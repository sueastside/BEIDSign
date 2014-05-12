<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
	model.nodeRef = AlfrescoUtil.param("nodeRef");
	var json = remote.call("/beid/appletcontext?getSession=true");
    if (json.status == 200) {
    	obj = eval('(' + json + ')');
        model.appletcontext = obj;
    } else {
    	model.appletcontext = {sessionId: 'FAILED', serviceUrl:'FAILED'};
    }
    
   // Need to know what type of node this is - document or folder
	AlfrescoUtil.param("nodeRef");
}

main();