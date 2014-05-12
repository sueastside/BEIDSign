<#include "/org/alfresco/include/alfresco-macros.lib.ftl" />
<script type="text/javascript">//<![CDATA[
   var mymanager = new Alfresco.component.ShareFormManager("${args.htmlid}").setOptions(
   {
      failureMessage: "signature-form-mgr.update.failed",
      submitButtonMessageKey: "signature-form-mgr.button.sign"
   }).setMessages(${messages});
   
   mymanager.onFormSubmitFailure = function (response) { console.log(response); };
   mymanager.onFormSubmitSuccess = function (response) { window.location = response.json.alf_redirect; };
//]]></script>
<div style="display:block">&nbsp</div>