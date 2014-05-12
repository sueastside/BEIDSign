package be.redtree.beid.servlets;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.VerificationException;

import be.redtree.beid.BaseServlet;
import be.redtree.beid.CMISHelper;

public class ExtractServlet extends BaseServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		//curl -XPOST  -H "Content-Type: application/json" -d '{"username":"jelle.hellemans@redtree.be","password":""}' https://alfresco.redtree.be:443/alfresco/s/api/login
		// create session
		
		//TICKET_8bcf5592114b4858b44847252c1e537cb40d0430
		String ticket = (String) request.getParameter("ticket");
		//workspace://SpacesStore/32547632-805a-4b2b-8894-edfabe5ca7d7
		String nodeId = (String) request.getParameter("nodeId");
		Session session = CMISHelper.getCMISSession(ticket);
		
		System.out.println("Fetching... "+nodeId);
		
		CmisObject object = session.getObject(nodeId);
		
		System.out.println(object.getName());
		
		Document doc = (Document)object;
		
		ContentStream content = doc.getContentStream();
		
		//FileInputStream is = new FileInputStream(new File("/tmp/SampleSignedPDFDocument.pdf"));
		PdfReader reader = new PdfReader(content.getStream());
		AcroFields af = reader.getAcroFields();
		 
		// Search of the whole signature
		ArrayList names = af.getSignatureNames();
 
		// For every signature :
		for (int k = 0; k < names.size(); ++k) {
		   String name = (String)names.get(k);
		   
		   Security.addProvider(new BouncyCastleProvider());

		   System.out.println("Signature name: " + name);
		   System.out.println("Signature covers whole document: "
                                + af.signatureCoversWholeDocument(name));

		   System.out.println("Document revision: " + af.getRevision(name) + " of "
                                + af.getTotalRevisions());
		   
			KeyStore ks;
			try {
				ks = KeyStore.getInstance(KeyStore.getDefaultType());
				ks.load(null, null);
				// adobe root ce
				String filename = "/tmp/belgiumrs.crt";
				FileInputStream is1 = new FileInputStream(filename);
				CertificateFactory cf = CertificateFactory.getInstance("X509");
				X509Certificate cert1 = (X509Certificate) cf.generateCertificate(is1);
				System.out.println(cert1);
				ks.setCertificateEntry("cacert", cert1);

				PdfPKCS7 pk = af.verifySignature(name);
				Calendar cal = pk.getSignDate();
				Certificate[] pkc = pk.getCertificates();
				System.out.println("Subject: "
						+ CertificateInfo.getSubjectFields(pk
								.getSigningCertificate()));
				System.out.println("Revision modified: " + !pk.verify());
				List<VerificationException> errors = CertificateVerification
						.verifyCertificates(pkc, ks, null, cal);
				if (errors.size() == 0)
					System.out.println("Certificates verified against the KeyStore");
				else
					System.out.println(errors);

			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>eID Applet Service</title></head>");
		out.println("<body>");
		out.println(getSession());
		out.println("</body></html>");
		out.close();
	}
}
