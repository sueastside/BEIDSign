/*
 * Copyright (C) 2014  Jelle Hellemans
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.redtree.beidsign.signature;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.redtree.beidsign.model.BEIDSignModel;
import be.redtree.beidsign.service.BeidSignService;


public class UserSignature
{
    private static Log          logger				= LogFactory.getLog(UserSignature.class);
	private ServiceRegistry 	serviceRegistry;
	private String				user;
	private BeidSignService	 beidSignService;	
    
	public UserSignature(ServiceRegistry serviceRegistry, 
			BeidSignService beidSignService, String user)
	{
		this.serviceRegistry = serviceRegistry;
		this.user = user;
		this.beidSignService = beidSignService;
		
		// check to see if this user has the signer aspect.  If not, add it.
		// this is temporary until I get the management interface sorted out.
		if(!signatureAvailable())
		{
			NodeRef person = serviceRegistry.getPersonService().getPerson(user);
			if(person != null)
			{
				serviceRegistry.getNodeService().addAspect(person, BEIDSignModel.ASPECT_SIGNER, null);
			}
		}
		
	}

	public BufferedImage getSignatureImage() 
	{
		NodeRef person = serviceRegistry.getPersonService().getPerson(user);
		if(person == null)
		{
			return null;
		}
		
		NodeRef sigImage = beidSignService.getUserSignatureNode(person, BEIDSignModel.ASSOC_SIGNERSIGNATUREIMAGE);
		
		if(sigImage != null)
		{
	        ContentReader imageReader = serviceRegistry.getContentService().getReader(sigImage, ContentModel.PROP_CONTENT);
	        
	        try 
	        {
	        	return ImageIO.read(imageReader.getContentInputStream());
	        }
	        catch(IOException ioex)
	        {
	        	logger.warn("Could not retrieve signature image as a child of person: " + ioex);
	        	// generate a default image?
	        }
		}
		
		return null;
	}

	public String getSignatureSource() 
	{
		NodeRef person = serviceRegistry.getPersonService().getPerson(user);
		if(person == null)
		{
			return null;
		}
		
		NodeRef sigImage = beidSignService.getUserSignatureNode(person, BEIDSignModel.ASSOC_SIGNERSIGNATUREIMAGE);
		
		if(sigImage != null)
		{
	        return String.valueOf(
	        	serviceRegistry.getNodeService().getProperty(sigImage, BEIDSignModel.PROP_SIGNATUREJSON));
		}
		
		return null;
	}
	
	public void saveSignatureImage(BufferedImage image, String source) 
	{
		
		// save the signature image as a child of the person
		NodeRef person = serviceRegistry.getPersonService().getPerson(user);
		
		if(person != null)
		{
			
			NodeRef sigNode = beidSignService.getUserSignatureNode(person, BEIDSignModel.ASSOC_SIGNERSIGNATUREIMAGE);
			
			if(sigNode == null)
			{
				// set up JSON source as property
				Map<QName, Serializable> sigProps = new HashMap<QName, Serializable>();
				sigProps.put(BEIDSignModel.PROP_SIGNATUREJSON, source);
				
		    	QName assocQName = QName.createQName(
		    			BEIDSignModel.BEID_MODEL_1_0_URI,
		    			QName.createValidLocalName(user + "-signatureimage"));
		    		
		    	ChildAssociationRef sigChildRef = serviceRegistry.getNodeService().createNode(
		    			person,
		    			BEIDSignModel.ASSOC_SIGNERSIGNATUREIMAGE, 
		    			assocQName, 
		    			BEIDSignModel.TYPE_SIGNATUREIMAGE,
		    			sigProps);
		    	
		    	sigNode = sigChildRef.getChildRef();
			}
			else
			{
				serviceRegistry.getNodeService().setProperty(sigNode, BEIDSignModel.PROP_SIGNATUREJSON, source);
			}
			
			// get a writer, store the image content
			ContentWriter writer = serviceRegistry.getContentService().
					getWriter(sigNode, ContentModel.PROP_CONTENT, true);

			try 
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				writer.putContent(new ByteArrayInputStream(baos.toByteArray()));
				
				PermissionService ps = serviceRegistry.getPermissionService();
				ps.clearPermission(sigNode, PermissionService.ALL_AUTHORITIES);
				ps.setInheritParentPermissions(sigNode, false);
			}
			catch(IOException ioex)
			{
				logger.warn("Could not save signature image as child of person: " + ioex);
			}
		}
	}

	public boolean signatureAvailable() 
	{
		// signature is only available if this user has the "signer" aspect applied.
		NodeRef person = serviceRegistry.getPersonService().getPerson(user);
		if(person != null && serviceRegistry.getNodeService().hasAspect(person, BEIDSignModel.ASPECT_SIGNER))
		{	
			return true;
		}
		
		return false;
	}
}
