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

package be.redtree.beidsign.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.redtree.beidsign.model.BEIDSignModel;
import be.redtree.beidsign.signature.UserSignatureFactory;

import com.itextpdf.text.pdf.PdfReader;

public class ScriptBeidSignService extends BaseProcessorExtension {

	private static final Log 			logger 						= LogFactory.getLog(ScriptBeidSignService.class);
	private ServiceRegistry				serviceRegistry;
	private UserSignatureFactory 	userSignatureFactory;
	private BeidSignService			beidSignService;
	
	public static final String			signatureValidName			= "signatureValid";
	public static final String			hashValidName				= "hashValid";
	
    public String getClassName()
    {
        return "ScriptBeidSignService";
    }
    
    public String getSignatureSource(String user)
    {
    	return userSignatureFactory.getUserSignature(user).getSignatureSource();
    }
    
    /**
     * Get a nodeRef string for the user's signature image
     * 
     * @param user
     * @return
     */
    public ScriptNode getSignatureImageNode(String user)
    {
    	NodeRef image = beidSignService.getUserSignatureNode(user, BEIDSignModel.ASSOC_SIGNERSIGNATUREIMAGE);
    	if(image != null)
    	{
    		return new ScriptNode(image, serviceRegistry);
    	}
    	else
    	{
    		return null;
    	}
    }
    
    
	/**
	 * Gets the page count for a PDF document
	 * 
	 * @param nodeRef
	 * @return
	 */
	public int getPageCount(String nodeRef){
		try
		{
			ContentReader reader = serviceRegistry
					.getContentService().getReader(new NodeRef(nodeRef), ContentModel.PROP_CONTENT);
			PdfReader pdfReader = new PdfReader(reader.getContentInputStream());
			int count = pdfReader.getNumberOfPages();
			pdfReader.close();
			return count;
			
		}
		catch(IOException ioex)
		{
			return -1;
		}
	}
	
    /**
     * Returns "true" if this user has all of the required bits and pieces (keystore) to
     * apply a digital signature.
     * 
     * @param user
     */
    public boolean getSignatureAvailable(String user)
    {
    	return userSignatureFactory.getUserSignature(user).signatureAvailable();
    }
    
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}

	public void setUserSignatureFactory(UserSignatureFactory userSignatureFactory) {
		this.userSignatureFactory = userSignatureFactory;
	}

	public void setBeidSignService(BeidSignService beidSignService) {
		this.beidSignService = beidSignService;
	}
}
