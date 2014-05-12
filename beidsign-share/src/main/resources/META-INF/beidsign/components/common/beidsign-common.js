function hideDependentControls(element)
{
	// get the field html id
	var fieldHtmlId = element.id;
	// set the value of the hidden field
	var value = YAHOO.util.Dom.get(fieldHtmlId).checked;
	YAHOO.util.Dom.get(fieldHtmlId + "-hidden").value = value;
	// find and hide the dependent controls
	var controls = YAHOO.util.Dom.get(fieldHtmlId + "-tohide").value.split(",");

	for(index in controls)
	{
		var module = new YAHOO.widget.Module((controls[index] + "-control"));
		if(value == true)
		{
			module.show();
		}
		else
		{
			module.hide();
		}
	}
}
