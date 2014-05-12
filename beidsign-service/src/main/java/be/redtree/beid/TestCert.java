package be.redtree.beid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateInfo.X500Name;
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.SignaturePermissions;
import com.itextpdf.text.pdf.security.SignaturePermissions.FieldLock;
import com.itextpdf.text.pdf.security.VerificationException;

public class TestCert {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InvalidNameException 
	 */
	public static String getFullName(X509Certificate cert)
	{
		X500Name n = CertificateInfo.getSubjectFields(cert);
		String name = n.getField("GIVENNAME");
		if (n.getField("INITIALS")!=null)
			name += " " + n.getField("INITIALS");
		
		return name+" "+n.getField("SURNAME");
	}
	
	public static void main(String[] args) throws IOException, InvalidNameException 
	{
		//FileInputStream is = new FileInputStream(new File("/tmp/test899.pdf"));
		FileInputStream is = new FileInputStream(new File("/tmp/stop.pdf"));
		PdfReader reader = new PdfReader(is);
		AcroFields af = reader.getAcroFields();

		// Search of the whole signature
		ArrayList names = af.getSignatureNames();

		// For every signature :
		for (int k = 0; k < names.size(); ++k) {
			String name = (String) names.get(k);

			Security.addProvider(new BouncyCastleProvider());

			System.out.println("Signature name: " + name);
			System.out.println("Signature covers whole document: "
					+ af.signatureCoversWholeDocument(name));

			System.out.println("Document revision: " + af.getRevision(name)
					+ " of " + af.getTotalRevisions());

			KeyStore ks;
			try {
				ks = KeyStore.getInstance(KeyStore.getDefaultType());
				ks.load(null, null);
				// adobe root ce
				String filename = "/tmp/belgiumrca2.crt";
				FileInputStream is1 = new FileInputStream(filename);
				CertificateFactory cf = CertificateFactory.getInstance("X509");
				X509Certificate cert1 = (X509Certificate) cf
						.generateCertificate(is1);
				//System.out.println(cert1);
				ks.setCertificateEntry("cacert", cert1);

				PdfPKCS7 pk = af.verifySignature(name);
				Calendar cal = pk.getSignDate();
				Certificate[] pkc = pk.getCertificates();
				
				System.out.println("getSubjectDN1 "+getFullName(pk.getSigningCertificate()));
				
				System.out.println("Subject: "
						+ CertificateInfo.getSubjectFields(pk
								.getSigningCertificate()));
				System.out.println("Revision modified: " + !pk.verify());
				List<VerificationException> errors = CertificateVerification
						.verifyCertificates(pkc, ks, null, cal);
				if (errors.size() == 0)
					System.out
							.println("Certificates verified against the KeyStore");
				else
					System.out.println(errors);
				
				System.out.println("Digest algorithm: " + pk.getHashAlgorithm());
				System.out.println("Encryption algorithm: " + pk.getEncryptionAlgorithm());
				System.out.println("Filter subtype: " + pk.getFilterSubtype());

				PdfDictionary sigDict = af.getSignatureDictionary(name);
				SignaturePermissions perms = new SignaturePermissions(sigDict, null);
				System.out.println("Signature type: " +
				(perms.isCertification() ? "certification" : "approval"));
				System.out.println("Filling out fields allowed: " +
				perms.isFillInAllowed());
				System.out.println("Adding annotations allowed: " +
				perms.isAnnotationsAllowed());
				for (FieldLock lock : perms.getFieldLocks()) {
					System.out.println("Lock: " + lock.toString());
				}


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

	}

}
