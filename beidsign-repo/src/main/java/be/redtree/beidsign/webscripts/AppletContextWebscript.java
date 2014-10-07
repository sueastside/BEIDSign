package be.redtree.beidsign.webscripts;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WebScriptSession;

import be.redtree.beidsign.action.executer.PDFSignActionExecuter;
import be.redtree.beidsign.signature.SignatureToImage;
import be.redtree.beidsign.signature.UserSignature;
import be.redtree.beidsign.signature.UserSignatureFactory;

import com.google.gson.Gson;

public class AppletContextWebscript extends AbstractWebScript 
{
	private ServiceRegistry registry;
	private Repository repository;
	private UserSignatureFactory userSignatureFactory;
	private String methodType;

	// for Spring injection
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	// for Spring injection
	public void setServiceRegistry(ServiceRegistry registry) {
		this.registry = registry;
	}
	
	// for Spring injection
	public void setUserSignatureFactory(UserSignatureFactory userSignatureFactory) {
		this.userSignatureFactory = userSignatureFactory;
	}
	
	// for Spring injection
	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException 
	{
		System.out.println("AppletContextWebscript::execute: ");
		
		Properties properties = getproperties();
		
		String serviceURL = properties.getProperty("serviceURL", "http://localhost:8081/beidsign-service/");
		String serviceURLHttps = properties.getProperty("serviceURLHttps", "https://localhost:8444/beidsign-service/");
		
		WebScriptSession session = req.getRuntime().getSession();
		JSONObject obj = new JSONObject();
		if ("GET".equalsIgnoreCase(methodType))
		{
			System.out.println("AppletContextWebscript::execute: GET");
			try {
				obj.put("serviceUrl", serviceURLHttps);
				obj.put("sessionId", session.getValue("sessionId"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if ("POST".equalsIgnoreCase(methodType))
		{
			NodeRef workingCopy = null;
			try
			{
				System.out.println("AppletContextWebscript::execute: POST");
				Gson gson = new Gson();
				Map<String, Object> map = new HashMap<>();
				map = gson.fromJson(req.getContent().getReader(), map.getClass());
				System.out.println("MAP: "+map);
				
				
				//Save Signature
				int width = 350;
		        int height = 75;
		        String signatureJson = (String)map.get("prop_"+PDFSignActionExecuter.PARAM_SIGNATURE_JSON);
				BufferedImage sigImage = SignatureToImage.convertJsonToImage(signatureJson, width, height);
	       		// save the signature image back to the signatureProvider
				String user = AuthenticationUtil.getRunAsUser();
				UserSignature signatureProvider = userSignatureFactory.getUserSignature(user);
	       		signatureProvider.saveSignatureImage(sigImage, signatureJson);
				
				Map<String,Object> params = new LinkedHashMap<>();
				for (String param: PDFSignActionExecuter.PARAMS)
		        {
		        	Object ser = map.get("prop_"+param);
		        	if (ser instanceof String)
		        		params.put(param, (String)ser);
		        	else if (ser instanceof Boolean)
		        		params.put(param, ((Boolean)ser)?"true":"false");
		        	else if (ser!=null)
		        		System.out.println("PDFSignActionExecuter::executeImpl: Ignored "+param+" : "+ser+" ("+ser.getClass().getName()+")");
		        }
				
				NodeRef actionedUponNodeRef = new NodeRef((String)map.get("alf_destination"));
				
				workingCopy = registry.getCheckOutCheckInService().getWorkingCopy(actionedUponNodeRef);
		        if (workingCopy == null)
		        {
		        	workingCopy = registry.getCheckOutCheckInService().checkout(actionedUponNodeRef);
		        }
		        
		        params.put("nodeRef", actionedUponNodeRef.toString());
		        params.put("workingCopy", workingCopy.toString());
		        
	
		        AuthenticationService  authenticationService = registry.getAuthenticationService();
			    String generatedTicketId = authenticationService.getCurrentTicket(); 
		        params.put("ticket", generatedTicketId);
				
		        StringBuilder postData = new StringBuilder();
		        for (Map.Entry<String,Object> param : params.entrySet()) {
		            if (postData.length() != 0) postData.append('&');
		            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
		            postData.append('=');
		            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		        }
		        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		        
		        URL url = new URL(serviceURL+"start");
		
		        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		        conn.setRequestMethod("POST");
		        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		        conn.setFollowRedirects(false);
		        conn.setUseCaches(false);
		        conn.setDoOutput(true);
		        conn.getOutputStream().write(postDataBytes);
		        
		        try
		        {
		        	IOUtils.copy(conn.getInputStream(), System.out);
		        } catch (IOException e)
		        {
		        	e.printStackTrace();
		        	if (conn.getErrorStream()!=null)
		        		IOUtils.copy(conn.getErrorStream(), System.err);
		        }
		        
		        boolean sessionIdFound = false;
		        String header = conn.getHeaderField("Set-Cookie");
		        if (header != null)
		        {
		        	String[] cookies = header.split(";");
		        	for (String cookie: cookies)
		        	{
		        		String[] entry = cookie.split("=");
		        		if (entry.length==2)
		        		{
		        			if ("JSESSIONID".equalsIgnoreCase(entry[0]))
		        			{
		        				obj.put("serviceUrl", serviceURLHttps);
		        				obj.put("sessionId", entry[1]);
		        				session.setValue("sessionId", entry[1]);
		        				System.out.println("JSESSIONID: "+entry[1]);
		        				sessionIdFound = true;
		        			}
		        		}
		        	}
		        }
		        
		        if (!sessionIdFound)
		        {
		        	throw new RuntimeException("No JSESSIONID was returned!!!");
		        }
				
				try {
					obj.put("alf_redirect", map.get("alf_redirect")); //Handled by ShareFormManager onFormSubmitSuccess
					//obj.put("alf_redirect", "mypgae");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				if (workingCopy != null)
					registry.getCheckOutCheckInService().cancelCheckout(workingCopy);
			}
		}
		else 
		{
			throw new WebScriptException(Status.STATUS_METHOD_NOT_ALLOWED,
					"Method "+methodType+" not supported!");
		}
		
		// build a JSON string and send it back
		String jsonString = obj.toString();
		res.getWriter().write(jsonString);
	}
	
	protected Properties getproperties() {
		
		final ServiceRegistry serviceRegistry = this.registry;
		
		Properties properties = AuthenticationUtil.runAs(
				new AuthenticationUtil.RunAsWork<Properties>() {
					@Override
					public Properties doWork() throws Exception {
						
						Properties properties = new Properties();

						NodeRef companyHomeRef = repository.getCompanyHome();
						List<String> pathElements = Arrays.asList(StringUtils.split("beid-config.cfg", '/'));
						 
						NodeRef propertiesNodeRef = null;
						try {
							propertiesNodeRef = serviceRegistry.getFileFolderService().resolveNamePath(companyHomeRef, pathElements).getNodeRef();
						} catch(FileNotFoundException e) {
							return properties;
						}

						System.out.println("propertiesNodeRef: "+ propertiesNodeRef);

						ContentReader reader = serviceRegistry.getContentService().getReader(propertiesNodeRef, ContentModel.PROP_CONTENT);
						
						try {
							properties.load(reader.getContentInputStream());
							return properties;
						} catch (ContentIOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							return properties;
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							return properties;
						}
					}
				}, AuthenticationUtil.getAdminUserName());
		
		return properties;
	}

}