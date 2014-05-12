/**
 * PDF digital signature position component.
 *
 * @namespace BeidSign
 * @class BeidSign.Signatures
 */
if(typeof BeidSign == "undefined" || !BeidSign)
{
	var BeidSign = {};
}

(function()
{
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom,
	Event = YAHOO.util.Event,
	Selector = YAHOO.util.Selector;
	
	/**
	 * SignatureView constructor.
	 *
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {BeidSign.Signatures} The new component instance
	 * @constructor
	 */
	BeidSign.SignaturePosition = function SignaturePosition_constructor(htmlId)
	{
		BeidSign.SignaturePosition.superclass.constructor.call(this, "BeidSign.SignaturePosition", htmlId, []);
		return this;
	};

	YAHOO.extend(BeidSign.SignaturePosition, Alfresco.component.Base,
	{
		/**
		 * Object container for initialization options
		 *
		 * @property options
		 * @type {object} object literal
		 */
		options:
		{
			/**
			 * Reference to the signed document
			 *
			 * @property nodeRef
			 * @type string
			 */
			nodeRef: null,
			
			/**
			 * Only use as a placeholder for drawn position?
			 *
			 * @property placeholderOnly
			 * @type boolean
			 */
			placeholderOnly: false
		},

		position:
		{
			type:"predefined",
			position:"center",
			page:"1",
			box: {startX:"0",startY:"0",endX:"0",endY:"0"}
		},
		
		// set up YUI modules for the three types of positions
		signatureFieldModule: null,
		predefinedPositionModule: null,
		drawnPositionModule: null,
		
		onReady: function SignauturePosition_onReady()
		{
			if(!this.options.placeholderOnly)
			{
				// get the page count for this doc
				this.getPageCount(this.options.nodeRef);
				
				// set up YUI modules for the 2 types of positions
				this.predefinedPositionModule = new YAHOO.widget.Module(this.id + "-predefinedPositions");
				this.drawnPositionModule = new YAHOO.widget.Module(this.id + "-drawnPosition");
				
				// set listeners for radio buttons
				YAHOO.util.Event.addListener([this.id + "-predefinedLocationOption"], "click", this.showPositionOptions, this);
				YAHOO.util.Event.addListener([this.id + "-drawnPositionOption"], "click", this.showPositionOptions, this);
				
				//set listeners for select boxes
				YAHOO.util.Event.addListener([this.id + "-positionSelect"], "change", this.onPositionSelectChange, this);
				YAHOO.util.Event.addListener([this.id + "-pageSelect"], "change", this.onPageSelectChange, this);	
			}
			else
			{
				this.position.type = "drawn";
			}
		},
		
		// function to show or hide position options based on radiobutton values
		showPositionOptions: function SignaturePosition_showPositionOptions(event, that)
		{
			var target = event.target || event.srcElement;
			if (event.target.value == "drawnPosition")
			{
				that.predefinedPositionModule.hide();
				that.drawnPositionModule.show();
				that.onDrawnPositionSelect(event, that);
			}
			else 
			{
				that.drawnPositionModule.hide();
				that.predefinedPositionModule.show();
				that.onPositionSelectChange(event, that);
			}
		},
	
		onPositionSelectChange: function SignaturePosition_onPositionSelectChange(event, that)
		{
			var selectedPosition = YAHOO.util.Dom.get(that.id + "-positionSelect").value;
			that.position.type = "predefined";
			that.position.position = selectedPosition;
			that.setPosition(that);
		},
		
		onPageSelectChange: function SignaturePosition_onPageSelectChange(event, that)
		{
			var page = YAHOO.util.Dom.get(that.id + "-pageSelect").value;
			that.position.page = page;
			that.setPosition(that);
		},
		
		onDrawnPositionSelect: function SignaturePosition_onDrawnPositionSelect(event, that)
		{
			that.position.type = "drawn";
			that.setPosition(that);
		},
		
		setPosition: function SignaturePosition_setPosition(that)
		{
			YAHOO.util.Dom.get(that.id).value = JSON.stringify(that.position);
		},

		getPageCount: function getPageCount(nodeRef)
		{
			Alfresco.util.Ajax.jsonGet(
				{
					url: (Alfresco.constants.PROXY_URI + "beidsign/pagecount?nodeRef=" + nodeRef),
					successCallback:
					{
						fn: function(response)
						{
							var pageSelect = YAHOO.util.Dom.get(this.id + "-pageSelect");
							var pages = parseInt(response.json.pageCount);
							if(pages > 0)
							{
								for(var i = 1;i < pages + 1; i++)
								{
									var opt = document.createElement("option");
									opt.text = i;
									opt.value = i;
									pageSelect.add(opt, null);
								}
							}
						},
						scope: this
					},
					failureCallback:
					{
						fn: function(response)
						{
							Alfresco.util.PopupManager.displayMessage(
								{
									text: "Could not retreive page count"
								}
							);
						}
					}
				});
		}
	});
})();