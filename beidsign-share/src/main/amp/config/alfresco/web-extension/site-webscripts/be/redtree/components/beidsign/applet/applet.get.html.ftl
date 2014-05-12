<#include "/org/alfresco/include/alfresco-macros.lib.ftl" />
<script type="text/javascript" src="${appletcontext['serviceUrl']}resources/jquery.beid.js"></script>
<link rel="stylesheet" type="text/css" href="${appletcontext['serviceUrl']}resources/jquery.beid.css" />

<script>
	QUEUE = [];
	
	function MessageCallbackEx (status, messageId, message) {
		//console.log(status);
		//console.log(messageId);
		//console.log(message);
		
		var msg = {status: status, messageId: messageId, message: message};
		if (QUEUE) QUEUE.push(msg);
		else handleMessage(msg);
	}
	
	function RemoveCardCallback () {
		console.log('RemoveCardCallback');
	}
	
	// Applet fires DETECTING_CARD, READING_IDENTITY, DETECTING_CARD, ...
	// for some reason when inserting an e-id, so let's try to ignore 
	// these last DETECTING_CARD messages, cause they mess up my flow yo!
	IDENTITY_LOADED = false;
	
	function handleMessage(msg) {
		if (msg.messageId == 'CONNECT_READER') {
			$('#beidicon').beidicon('unplugged');
		}
		else if (msg.messageId == 'DETECTING_CARD' && !IDENTITY_LOADED) {
			$('#beidicon').beidicon('pluggedin');
			$('#beidicon').beidicon('card-missing');
		}
		else if (msg.messageId == 'INSERT_CARD_QUESTION' && !IDENTITY_LOADED) {
			$('#beidicon').beidicon('pluggedin');
			$('#beidicon').beidicon('card-missing');
		}
		else if (msg.messageId == 'SIGNING') {
			$('#beidicon').beidicon('pluggedin');
			$('#beidicon').beidicon('card-inserted');
		}
		else if (msg.messageId == 'READING_IDENTITY') {
			$('#beidicon').beidicon('pluggedin');
			$('#beidicon').beidicon('card-inserted');
			IDENTITY_LOADED = true;
		}
		else if (msg.messageId == 'SECURITY_ERROR') {
			var handleOk = function() {
			    //this.hide();
			    window.location = '${siteURL("document-details?nodeRef=" + context.properties.nodeRef?url)}';
			};
		    
		    Alfresco.util.PopupManager.displayPrompt(
			{
			    text: "Something went wrong: \n"+msg.message,
			    close: true,
			    buttons: [{ text: "Ok", handler: handleOk }]
			});
		}
		$("#beidicon").beidicon('blink');
	}
</script>

<div id="beidicon"></div>
<script>
  $(document).ready(function () {
    $('#beidicon').beidicon();

    var queue = QUEUE;
    QUEUE = null;
    for (var i = 0; i < queue.length; i++) {
      var msg = queue[i];
      handleMessage(msg)
    }
  });
</script>
<script src="https://www.java.com/js/deployJava.js"></script>
<script>
    var attributes = {
        code :'be.fedict.eid.applet.Applet.class',
        archive :'${appletcontext['serviceUrl']}eid-applet-package-1.2.0.Beta2.jar',
        width :320,
        height :400
    };
    var parameters = {
        TargetPage :'${siteURL("document-details?nodeRef=" + context.properties.nodeRef?url)}',
        AppletService : '${appletcontext['serviceUrl']}applet-service-sign;jsessionid=${appletcontext['sessionId']}',
        BackgroundColor : '#ffffff',
        MessageCallbackEx : 'MessageCallbackEx',
        RemoveCardCallback: 'RemoveCardCallback',
        HideDetailsButton: false
    };
    var version = '1.6';
    deployJava.runApplet(attributes, parameters, version);
</script>
