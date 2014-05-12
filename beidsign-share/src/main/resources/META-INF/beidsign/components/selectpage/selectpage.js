function getPageCount(nodeRef, success, failure)
{
	Alfresco.util.Ajax.jsonGet(
		{
			url: (Alfresco.constants.PROXY_URI + "beidsign/pagecount?nodeRef=" + nodeRef),
			successCallback:
			{
				fn: function(response)
				{
					success(response);
				}
			},
			failureCallback:
			{
				fn: function(response)
				{
					failure(response);
				}
			}
		});
}