package be.redtree.beid.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import be.fedict.eid.applet.service.spi.AddressDTO;
import be.fedict.eid.applet.service.spi.CertificateSecurityException;
import be.fedict.eid.applet.service.spi.DigestInfo;
import be.fedict.eid.applet.service.spi.ExpiredCertificateSecurityException;
import be.fedict.eid.applet.service.spi.IdentityDTO;
import be.fedict.eid.applet.service.spi.RevokedCertificateSecurityException;
import be.fedict.eid.applet.service.spi.SignatureService;
import be.fedict.eid.applet.service.spi.TrustCertificateSecurityException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.CertificateInfo.X500Name;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;

import be.redtree.beid.BeidConstants;


public class SignatureServiceImpl implements SignatureService 
{
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public String getFilesDigestAlgorithm() {
		return null;
	}
	
	public DigestInfo preSign(List<DigestInfo> arg0,
			List<X509Certificate> arg1, IdentityDTO arg2, AddressDTO arg3,
			byte[] arg4) throws NoSuchAlgorithmException {
		return preSign(arg0, arg1);
	}

	
	public DigestInfo preSign(List<DigestInfo> arg0, List<X509Certificate> certificates)
			throws NoSuchAlgorithmException {
		System.out.println("SignatureServiceImpl::preSign");
		
		HttpSession session = getSession();
		SignatureRequest request = (SignatureRequest)session.getAttribute(BeidConstants.SIGNATUREREQUEST_SESSION_NAME);
		ContentStream content = request.getDocument().getContentStream();
		
		try 
		{
			Certificate[] chain = new Certificate[certificates.size()];
			int index = 0;
			for (X509Certificate cert: certificates)
			{
				//System.out.println("CERT: "+cert);
				chain[index++] = cert;
			}
			
			// we create a reader and a stamper
			PdfReader reader = new PdfReader(content.getStream());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfStamper stamper = PdfStamper.createSignature(reader, baos, '\0');
			// we create the signature appearance
			PdfSignatureAppearance sap = stamper.getSignatureAppearance();
			
			request.fillAppearance(sap, reader);
	
			sap.setCertificate(chain[0]);
			// we create the signature infrastructure
			PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
			dic.setReason(sap.getReason());
			dic.setLocation(sap.getLocation());
			dic.setContact(sap.getContact());
			dic.setDate(new PdfDate(sap.getSignDate()));
			sap.setCryptoDictionary(dic);
			HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
			exc.put(PdfName.CONTENTS, new Integer(8192 * 2 + 2));
			sap.preClose(exc);
			ExternalDigest externalDigest = new ExternalDigest() {
				public MessageDigest getMessageDigest(String hashAlgorithm)
						throws GeneralSecurityException {
					return DigestAlgorithms.getMessageDigest(hashAlgorithm,
							null);
				}
			};
			PdfPKCS7 sgn = new PdfPKCS7(null, chain, "SHA256", null, externalDigest, false);
			InputStream data = sap.getRangeStream();
			byte hash[] = DigestAlgorithms.digest(data, externalDigest.getMessageDigest("SHA256"));
			Calendar cal = Calendar.getInstance();
			byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, cal, null, null, CryptoStandard.CMS);
			sh = MessageDigest.getInstance("SHA256", "BC").digest(sh);
			
			// We store the objects we'll need for post signing in a session
			session.setAttribute(BeidConstants.SIGNATURE_SESSION_NAME, sgn);
			session.setAttribute(BeidConstants.HASH_SESSION_NAME, hash);
			session.setAttribute(BeidConstants.CAL_SESSION_NAME, cal);
			session.setAttribute(BeidConstants.SAP_SESSION_NAME, sap);
			session.setAttribute(BeidConstants.BAOS_SESSION_NAME, baos);
			DigestInfo info = new DigestInfo(sh, "SHA-256", "BeidSign");
			
			return info;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	

	public void postSign(byte[] signedBytes, List<X509Certificate> arg1)
			throws ExpiredCertificateSecurityException,
			RevokedCertificateSecurityException,
			TrustCertificateSecurityException, CertificateSecurityException,
			SecurityException 
	{
		System.out.println("SignatureServiceImpl::postSign");
		// we get the objects we need for postsigning from the session
		HttpSession session = getSession();
		PdfPKCS7 sgn = (PdfPKCS7) session.getAttribute(BeidConstants.SIGNATURE_SESSION_NAME);
		byte[] hash = (byte[]) session.getAttribute(BeidConstants.HASH_SESSION_NAME);
		Calendar cal = (Calendar) session.getAttribute(BeidConstants.CAL_SESSION_NAME);
		PdfSignatureAppearance sap = (PdfSignatureAppearance) session.getAttribute(BeidConstants.SAP_SESSION_NAME);
		ByteArrayOutputStream os = (ByteArrayOutputStream) session.getAttribute(BeidConstants.BAOS_SESSION_NAME);
		//session.invalidate();

		// we complete the PDF signing process
		sgn.setExternalDigest(signedBytes, null, "RSA");//SHA256-RSA-PKCS1
		byte[] encodedSig = sgn.getEncodedPKCS7(hash, cal, null, null, null, CryptoStandard.CMS);
		byte[] paddedSig = new byte[8192];

		System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
		PdfDictionary dic2 = new PdfDictionary();
		dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
		try {
			sap.close(dic2);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// we write the signed document to the HttpResponse output stream
		byte[] pdf = os.toByteArray();
		
		
		session.setAttribute("beid.pdf", pdf);
		
		SignatureRequest request = (SignatureRequest)session.getAttribute(BeidConstants.SIGNATUREREQUEST_SESSION_NAME);
		Document doc = request.getDocument();
		
		ContentStream oldStream = doc.getContentStream();
		ContentStream newStream = new ContentStreamImpl(oldStream.getFileName(), 
														BigInteger.valueOf(pdf.length), 
														oldStream.getMimeType(), 
														new ByteArrayInputStream(pdf));
		
		
		if (!doc.isVersionSeriesCheckedOut())
		{
			ObjectId id = doc.checkOut();
			doc = request.getDocument(id.getId());
		}
		else
		{
			doc = request.getDocument(doc.getVersionSeriesCheckedOutId());
		}
		AlfrescoDocument d = (AlfrescoDocument)doc;
		if (d.hasAspect("P:beidsign:signable"))
			d.removeAspect("P:beidsign:signable");
		doc.checkIn(true, null, newStream, "Document was signed by "+getFullName((X509Certificate)sap.getCertificate()));
		
		
		//System.out.println(pdf);
	}
	
	private HttpSession getSession() {
	    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
	    return attr.getRequest().getSession(true); // true == allow create
	}
	
	private String getFullName(X509Certificate cert)
	{
		X500Name n = CertificateInfo.getSubjectFields(cert);
		String name = n.getField("GIVENNAME");
		if (n.getField("INITIALS")!=null)
			name += " " + n.getField("INITIALS");
		
		return name+" "+n.getField("SURNAME");
	}
}
