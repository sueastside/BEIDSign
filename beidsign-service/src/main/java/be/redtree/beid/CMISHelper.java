package be.redtree.beid;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class CMISHelper 
{
	static public Session getCMISSession(String ticket) 
	{
		disableSslVerification();
		
		
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		
		System.out.println("Logging in with: "+ ticket);
	
		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/alfresco/cmisatom");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
		//parameter.put(SessionParameter.USER, "");
		//parameter.put(SessionParameter.PASSWORD, ticket);//TODO: fails in community?!
		
		parameter.put(SessionParameter.USER, "admin");
		parameter.put(SessionParameter.PASSWORD, "admin");
		
		parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl.class.getName());
	
		List<Repository> repositories = factory.getRepositories(parameter);
		
		Repository defaultRepository = repositories.get(0);
		System.out.println(defaultRepository.getName());
	
		return defaultRepository.createSession();
	}
	
	private static void disableSslVerification() 
	{
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
	        }
	        };

	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };

	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (KeyManagementException e) {
	        e.printStackTrace();
	    }
	}

}
