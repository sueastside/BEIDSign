<html>
<head>
	<style>
	input { display: block; }
	</style>
	<link rel="stylesheet" href="resources/jquery.signaturepad.css">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
	<script src="resources/jquery.signaturepad.min.js"></script>
</head>
	<body>

	
<form class="sigPad" name="default" method="post" accept-charset="utf-8" action="/beidsign-service/start" >
 <input name="nodeRef" value="workspace://SpacesStore/fe9e4866-2823-4c5e-b1cd-58e4f4898456">
 <input name="ticket" value="TICKET_622e540e4e6a980d6985a781e9bfbbb028730c6e">
 
 
 location:<input name="location" tabindex="0" type="text" value="" title="location">
 
 reason:<input name="reason" tabindex="0" type="text" value="" title="reason">
 <input name="visible" type="hidden"  value="true">
 <input name="graphic" type="hidden" value="true">

 
<input name="position" type="hidden" value="{&quot;type&quot;:&quot;predefined&quot;,&quot;position&quot;:&quot;center&quot;,&quot;page&quot;:2,&quot;box&quot;:{&quot;startX&quot;:464,&quot;startY&quot;:27,&quot;endX&quot;:588,&quot;endY&quot;:58}}" class="" title="" alf-validation-msg="">

<div class="sig sigWrapper current">
    <div class="typed" style="display: none; font-size: 30px;"></div>
    <canvas class="pad" width="198" height="55"></canvas>
    <input type="hidden" name="signature-json" class="output">
  </div>
  
<script>
  $(document).ready(function () {
    $('.sigPad').signaturePad({defaultAction:'drawIt'});
  });
</script>


<div class="form-field">
	<input type="hidden0" name="geolocation" value="">
	<script type="text/javascript">//<![CDATA[
    var geolocation = function()
    {
    	var field = document.forms.default.geolocation;
    	
    	function callback(position)
      	{
    		field.value = position.coords.latitude + "," + position.coords.longitude
      	}
    	
    	function error(error)
    	{
    	  	switch(error.code) 
    	  	{
    	    	case error.PERMISSION_DENIED:
    	      		field.value = "NOLOCATION: User denied the request for geolocation."
    	      		break;
    	    	case error.POSITION_UNAVAILABLE:
    	    		field.value = "NOLOCATION: Location information is unavailable."
    	    	  	break;
    	    	case error.TIMEOUT:
    	    		field.value = "NOLOCATION: The request to get user location timed out."
    	      		break;
    	    	case error.UNKNOWN_ERROR:
    	    		field.value = "NOLOCATION: An unknown error occurred."
    	      		break;
    	    }
    	 }
    	 
		
      	if (navigator.geolocation)
        {
          console.log("test");
        	navigator.geolocation.getCurrentPosition(callback, error);
        }
      	else
      	{
      	console.log("woof");
      		field.value = "NOLOCATION: Geolocation is not supported by this browser.";
      	}
      	console.log(field);
    }();
		//]]></script>
</div>
<input type="submit" value="Submit" />
</form>   


	</body>
</html>