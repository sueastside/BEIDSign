package be.redtree.beid.servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;

import be.redtree.beid.BaseServlet;
import be.redtree.beid.BeidConstants;
import be.redtree.beid.CMISHelper;
import be.redtree.beid.services.SignatureRequest;

public class InitServlet extends BaseServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException 
	{
		System.out.println("doPost... ");
		System.out.println("doPost... " +req.getParameter("prop_location"));
		System.out.println("doPost... " +req.getAttribute("prop_location"));
		
		SignatureRequest signatureRequest = new SignatureRequest(req);
		signatureRequest.validate();
		
		HttpSession session = req.getSession();
		
		session.setAttribute(BeidConstants.SIGNATUREREQUEST_SESSION_NAME, signatureRequest);
		
		RequestDispatcher dispatcher = req.getRequestDispatcher("applet.jsp");
		dispatcher.forward(req, resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException 
	{
		String ticket = (String) req.getParameter("ticket");
		// workspace://SpacesStore/32547632-805a-4b2b-8894-edfabe5ca7d7
		String nodeId = (String) req.getParameter("nodeId");
		Session cmisSession = CMISHelper.getCMISSession(ticket);

		System.out.println("Fetching... " + nodeId);

		CmisObject object = cmisSession.getObject(nodeId);

		System.out.println(object.getName());

		Document doc = (Document) object;
		
		HttpSession session = req.getSession();
		
		session.setAttribute(BeidConstants.SIGNATUREREQUEST_SESSION_NAME, doc);
		
		RequestDispatcher dispatcher = req.getRequestDispatcher("applet.jsp");
		dispatcher.forward(req, resp);
		
		/*
		SignatureServiceImpl impl = new SignatureServiceImpl();
		
		
		DigestInfo digestInfo = null;
		try {
			List<X509Certificate> certificates = new LinkedList<X509Certificate>();
			{
				String filename = "/tmp/belgiumrs.crt";
				FileInputStream is1 = new FileInputStream(filename);
				CertificateFactory cf = CertificateFactory.getInstance("X509");
				X509Certificate cert1 = (X509Certificate) cf.generateCertificate(is1);
				certificates.add(cert1);
			}
			digestInfo = impl.preSign(null, certificates);
			
			impl.postSign(digestInfo.digestValue, certificates);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 for (Enumeration<?> e = session.getAttributeNames(); e.hasMoreElements();)
		 {
			 String name = (String) e.nextElement();
			 System.out.println("session: "+name+": "+session.getAttribute(name));
		 }
		       

		resp.setContentType("application/octet-stream");
		OutputStream os = resp.getOutputStream();
		os.write(digestInfo.digestValue, 0, digestInfo.digestValue.length);
		os.flush();
		os.close();
		*/
	}
}
