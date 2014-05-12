package be.redtree.beid.services;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import be.redtree.beid.CMISHelper;
import be.redtree.beid.SignatureToImage;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;

public class SignatureRequest 
{
	public static final String			  POSITION_TYPE_DRAWN	   = "drawn";
    public static final String			  POSITION_TYPE_PREDEFINED = "predefined";
    public static final String			  POSITION_TYPE_FIELD	   = "field";
    
    public static final String            POSITION_CENTER          = "center";
    public static final String            POSITION_TOPLEFT         = "topleft";
    public static final String            POSITION_TOPRIGHT        = "topright";
    public static final String            POSITION_BOTTOMLEFT      = "bottomleft";
    public static final String            POSITION_BOTTOMRIGHT     = "bottomright";
    
	private String nodeRef;
	private String workingCopy;
	private String ticket;
	
	private String location;
	private String geolocation;
	private String reason;
	private String position;
	private String signatureJson;
	
	private boolean visible;
	private boolean graphic;
	
	private String positionType = "predefined";
	private String positionLoc = "center";
	private JSONObject box;
	private int page = -1;
	
	private int width = 350;
	private int height = 75;
	private BufferedImage sigImage;
	
	
	public SignatureRequest(HttpServletRequest request)
	{
		this.nodeRef = request.getParameter("nodeRef");
		this.workingCopy = request.getParameter("workingCopy");
		this.ticket = request.getParameter("ticket");
		
		this.location = request.getParameter("location");
		this.geolocation = request.getParameter("geolocation");
		this.reason = request.getParameter("reason");
		this.position = request.getParameter("position");
		this.signatureJson = request.getParameter("signature-json");
		
		this.visible = "true".equalsIgnoreCase(request.getParameter("visible"));
		this.graphic = "true".equalsIgnoreCase(request.getParameter("graphic"));
		
		System.out.println("SignatureRequest graphic="+request.getParameter("graphic"));
		
		// parse out the position JSON
        JSONObject positionObj = null;
        
        try {
        	JSONParser parser = new JSONParser();
        	positionObj = (JSONObject)parser.parse(position);
        } catch (ParseException e) {
			throw new RuntimeException("Could not parse position JSON");
		}
        
        // get the page
        page = Integer.parseInt(String.valueOf(positionObj.get("page")));
        
        // get the positioning type
        positionType = String.valueOf(positionObj.get("type"));
        
        // get the position (field or predefined)
        positionLoc = String.valueOf(positionObj.get("position"));
        
        // get the box (if required)
        box = (JSONObject)positionObj.get("box");
        
       
        sigImage = SignatureToImage.convertJsonToImage(signatureJson, width, height);
	}
	
	public void validate()
	{
		Session cmisSession = CMISHelper.getCMISSession(ticket);
		
		cmisSession.getDefaultContext().setCacheEnabled(false);
		
		CmisObject object = cmisSession.getObject(nodeRef);;
		
		Document doc = (Document) object;
		
		
		//TODO validate other params
	}
	
	public Document getDocument() 
	{
		Session cmisSession = CMISHelper.getCMISSession(ticket);
		
		CmisObject object = cmisSession.getObject(nodeRef);
		Document doc = (Document) object;
		
		return doc;
	}
	
	public Document getDocument(String id) 
	{
		Session cmisSession = CMISHelper.getCMISSession(ticket);
		
		CmisObject object = cmisSession.getObject(id);
		Document doc = (Document) object;
		
		return doc;
	}
	
	public BufferedImage getSignatureImage()
	{
		return sigImage;
	}
	
	public void fillAppearance(PdfSignatureAppearance sap, PdfReader reader) throws BadElementException, IOException
	{
		sap.setReason(reason);
        sap.setLocation(location);
        sap.setLocationCaption(geolocation);
        
        if(visible)
   		{	
   			//if this is a graphic sig, set the graphic here
        	System.out.println("fillAppearance: visible!   graphic="+graphic);
       		if(graphic)
       		{
       			System.out.println("fillAppearance: graphic!");
       			sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
           		sap.setSignatureGraphic(Image.getInstance(sigImage, Color.WHITE));
       		}
       		else 
       		{
       			sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.NAME_AND_DESCRIPTION);
       		}
       		
       		// either insert the sig at a defined field or at a defined position / drawn loc
           	if(positionType.equalsIgnoreCase(POSITION_TYPE_PREDEFINED))
           	{
           		System.out.println("fillAppearance: graphic!"+POSITION_TYPE_PREDEFINED);
           		Rectangle pageRect = reader.getPageSizeWithRotation(page);
           		sap.setVisibleSignature(positionBlock(positionLoc, pageRect, width, height), page, null);
           	}
           	else if(positionType.equalsIgnoreCase(POSITION_TYPE_DRAWN))
           	{
           		System.out.println("fillAppearance: graphic!"+POSITION_TYPE_DRAWN);
           		Rectangle pageRect = reader.getPageSizeWithRotation(page);
           		sap.setVisibleSignature(positionBlock(pageRect, box), page, null);
           	}
           	else
           	{
           		System.out.println("fillAppearance: graphic! something else");
	           	sap.setVisibleSignature(positionLoc);
           	}
   		}
	}
	
	/**
     * Get the signature block position, using the provided JSON for the box coordinates
     * and the selected page
     * 
     * @param pageRect
     * @param box
     * @return
     */
    protected Rectangle positionBlock(Rectangle pageRect, JSONObject box)
    {
    	float startX = Float.parseFloat(String.valueOf(box.get("startX")));
    	float startY = Float.parseFloat(String.valueOf(box.get("startY")));
    	float endX = Float.parseFloat(String.valueOf(box.get("endX")));
    	float endY = Float.parseFloat(String.valueOf(box.get("endY")));
    	
    	// make sure that the ll and ur coordinates match iText's expectations
    	startY = pageRect.getHeight() - startY;
    	endY = pageRect.getHeight() - endY;
    	
    	// create the rectangle to contain the signature from the corrected coordinates
    	Rectangle r = new Rectangle(startX, startY, endX, endY);
    	
    	return r;
    }
    
    /**
     * Create a rectangle for the visible stamp using the selected position and block size
     * 
     * @param position
     * @param width
     * @param height
     * @return
     */
    protected Rectangle positionBlock(String position, Rectangle pageRect, int width, int height)
    {

    	float pageHeight = pageRect.getHeight();
    	float pageWidth = pageRect.getWidth();
    	
    	Rectangle r = null;
    	//Rectangle constructor(float llx, float lly, float urx, float ury)
    	if (position.equals(POSITION_BOTTOMLEFT))
    	{
    		r = new Rectangle(0, height, width, 0);
    	}
    	else if (position.equals(POSITION_BOTTOMRIGHT))
    	{
    		r = new Rectangle(pageWidth - width, height, pageWidth, 0);
    	}
    	else if (position.equals(POSITION_TOPLEFT))
    	{
    		r = new Rectangle(0, pageHeight, width, pageHeight - height);
    	}
    	else if (position.equals(POSITION_TOPRIGHT))
    	{
    		r = new Rectangle(pageWidth - width, pageHeight, pageWidth, pageHeight - height);
    	}
    	else if (position.equals(POSITION_CENTER))
    	{
    		r = new Rectangle((pageWidth / 2) - (width / 2), (pageHeight / 2) - (height / 2),
    				(pageWidth / 2) + (width / 2), (pageHeight / 2) + (height / 2));
    	}

    	return r;
    }
}
