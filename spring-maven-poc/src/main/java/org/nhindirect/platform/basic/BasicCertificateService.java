package org.nhindirect.platform.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.nhindirect.platform.CertificateException;
import org.nhindirect.platform.CertificateService;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.MessageServiceException;
import org.nhindirect.platform.rest.RestClient;
import org.springframework.beans.factory.annotation.Autowired;

public class BasicCertificateService implements CertificateService {


    private final String ENDPOINT_CERTS_FILE_NAME = "certs";
    private final String TRUST_INCOMING_FILE_NAME = "trust-in";
    private final String TRUST_OUTGOING_FILE_NAME = "trust-out";
    private final String STORE_EXTENSION = ".jks";
    private final String DEFAULT_STORE_PASSWORD = "";
    private final String DEFAULT_KEY_PASSWORD = "";

    protected String certRoot = "data";
    protected boolean generateEndpointCerts = false;
    protected String caKeystoreFilename;
    protected String caKeystorePassword;
    protected String caPrivateKeyPassword;

    public Log log = LogFactory.getLog(BasicCertificateService.class);

    @Autowired
    private RestClient restClient;


    public X509Certificate getLocalCertificate(HealthAddress address) {
        
        log.debug("getLocalCertificate called for address: " + address);

        File userKeystore = getUserKeystore(address);

        if (!userKeystore.exists()) {
            // The keystore doesn't exist.
            if (generateEndpointCerts) {
                // Generate a key, sign by CA, save as users keystore
                generateCertStore(address);
            }
        }

        X509Certificate cert;

        try {
            cert = loadCert(userKeystore, DEFAULT_STORE_PASSWORD);
        } catch (Exception e) {
            log.error("Unable to load cert for address: " + address.toEmailAddress() + " caused by " + e.getMessage());
            throw new CertificateException("Unable to load cert for address: " + address.toEmailAddress()
                    + " caused by " + e.getMessage());
        }

        return cert;
    }

    public PrivateKey getLocalPrivateKey(HealthAddress address) {
        
        log.debug("getLocalPrivateKey called for address: " + address);
        
        File userKeystore = getUserKeystore(address);
        PrivateKey key;

        try {
            key = loadKey(userKeystore, DEFAULT_STORE_PASSWORD, DEFAULT_KEY_PASSWORD);
        } catch (Exception e) {
            log.error("Unable to load cert for address: " + address.toEmailAddress() + " caused by " + e.getMessage());

            throw new CertificateException("Unable to load cert for address: " + address.toEmailAddress()
                    + " caused by " + e.getMessage());
        }

        return key;
    }

    public X509Certificate getRemoteCertificate(HealthAddress address) {
        
        log.debug("getRemoteCertificate called for address: " + address);
        
        Collection<X509Certificate> certs = null;

        try {
            certs = restClient.getRemoteCerts(address);

            return (X509Certificate) certs.toArray()[0];
        } catch (MessageServiceException e) {
            // TODO: do something better here

            throw new CertificateException(e);
        }
    }

    public Collection<X509Certificate> getInboundTrustAnchors(HealthAddress address) {
        File anchorsFile = getTrustInboundKeystore(address);

        try {
            return loadAllCerts(anchorsFile, DEFAULT_STORE_PASSWORD);
        } catch (Exception e) {
            log.error("Unable to trust anchors caused by " + e.getMessage());
            throw new CertificateException("Unable to trust anchors caused by " + e.getMessage());
        }
    }

    private File getTrustInboundKeystore(HealthAddress address) {
        File anchorsFile = new File(certRoot + "/" + address.toEmailAddress() + "/" + TRUST_INCOMING_FILE_NAME
                + STORE_EXTENSION);
        return anchorsFile;
    }

    public Collection<X509Certificate> getOutboundTrustAnchors(HealthAddress address) {
        File anchorsFile = getTrustOutboundKeystore(address);

        try {
            return loadAllCerts(anchorsFile, DEFAULT_STORE_PASSWORD);
        } catch (Exception e) {
            log.error("Unable to trust anchors caused by " + e.getMessage());
            throw new CertificateException("Unable to trust anchors caused by " + e.getMessage());
        }
    }

    private File getTrustOutboundKeystore(HealthAddress address) {
        File anchorsFile = new File(certRoot + "/" + address.toEmailAddress() + "/" + TRUST_OUTGOING_FILE_NAME
                + STORE_EXTENSION);
        return anchorsFile;
    }

    private File getUserKeystore(HealthAddress address) {
        File userKeystore = new File(certRoot + "/" + address.toEmailAddress() + "/" + ENDPOINT_CERTS_FILE_NAME
                + STORE_EXTENSION);
        return userKeystore;
    }

    private X509Certificate loadCert(File certFile, String password) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, java.security.cert.CertificateException {
        InputStream in = new FileInputStream(certFile);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        X509Certificate cert = null;

        ks.load(in, password.toCharArray());
        in.close();

        Enumeration<String> aliases = ks.aliases();

        if (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            cert = (X509Certificate) ks.getCertificate(alias);
        }
        return cert;
    }

    public PrivateKey loadKey(File keyFile, String storePassword, String keyPassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException, UnrecoverableKeyException, java.security.cert.CertificateException {
        InputStream in = new FileInputStream(keyFile);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        PrivateKey key = null;

        ks.load(in, storePassword.toCharArray());
        in.close();
        Enumeration<String> aliases = ks.aliases();

        if (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            key = (PrivateKey) ks.getKey(alias, keyPassword.toCharArray());
        }

        return key;
    }

    public Collection<X509Certificate> loadAllCerts(File certFile, String storePassword) throws KeyStoreException, NoSuchAlgorithmException,
            java.security.cert.CertificateException, IOException {

        ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>();

        InputStream in = new FileInputStream(certFile);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        X509Certificate cert = null;

        ks.load(in, storePassword.toCharArray());
        in.close();

        Enumeration<String> aliases = ks.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            cert = (X509Certificate) ks.getCertificate(alias);
            certs.add(cert);
        }

        return certs;
    }
    
    // This method generates a certificate appropriate for S/MIME encryption from a CA keystore.
    private void generateCertStore(HealthAddress address) {

        log.debug("Generating key/cert/trust files for user: " + address);
        
        try {
            
            Security.addProvider(new BouncyCastleProvider());

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            SecureRandom random = new SecureRandom(); //SecureRandom.getInstance("SHA1PRNG", "BC");
            keyGen.initialize(1024, random);

            KeyPair keyPair = keyGen.genKeyPair();

            BigInteger serialNumber = new BigInteger("1");

            Calendar startDate = new GregorianCalendar();
            Calendar endDate = new GregorianCalendar();
            endDate.roll(Calendar.YEAR, 2);

            PrivateKey caKey = loadKey(new File(caKeystoreFilename), caKeystorePassword, caPrivateKeyPassword);
            X509Certificate caCert = loadCert(new File(caKeystoreFilename), caKeystorePassword);

            X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
            X500Principal subjectName = new X500Principal("CN=" + address.toEmailAddress());

            certGen.setSerialNumber(serialNumber);
            certGen.setIssuerDN(caCert.getSubjectX500Principal());
            certGen.setNotBefore(startDate.getTime());
            certGen.setNotAfter(endDate.getTime());
            certGen.setSubjectDN(subjectName);
            certGen.setPublicKey(keyPair.getPublic());
            certGen.setSignatureAlgorithm("SHA1withRSA");

            certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(
                    caCert));
            certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(keyPair
                    .getPublic()));
            
            X509Certificate cert = certGen.generate(caKey, "BC");   // note: private key of CA

            Certificate[] certs = new Certificate[1];
            certs[0] = cert;
            
            // Save the generated cert to a user specific keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, DEFAULT_STORE_PASSWORD.toCharArray());
            keyStore.setKeyEntry(address.toEmailAddress(), keyPair.getPrivate(), "".toCharArray(), certs);
            
            File userKeystore = getUserKeystore(address);
            userKeystore.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(userKeystore);
            
            keyStore.store(out, DEFAULT_STORE_PASSWORD.toCharArray());
            out.close();
            
            
            // Now write the inbound trust anchors
            KeyStore trustInStore = KeyStore.getInstance("JKS");
            trustInStore.load(null, DEFAULT_STORE_PASSWORD.toCharArray());
            trustInStore.setCertificateEntry("ca", caCert);

            File trustInKeystore = getTrustInboundKeystore(address);
            out = new FileOutputStream(trustInKeystore);
            trustInStore.store(out, DEFAULT_STORE_PASSWORD.toCharArray());
            out.close();
            
            // Now write out the outbound trust anchors
            KeyStore trustOutStore = KeyStore.getInstance("JKS");
            trustOutStore.load(null, DEFAULT_STORE_PASSWORD.toCharArray());
            trustOutStore.setCertificateEntry("ca", caCert);

            File trustOutKeystore = getTrustOutboundKeystore(address);
            out = new FileOutputStream(trustOutKeystore);
            trustOutStore.store(out, DEFAULT_STORE_PASSWORD.toCharArray());
            out.close();

            
            
        } catch (Exception e) {
            log.error("Unable to generate key/certificate/trust for user: " + address); 
            log.error(e);
            e.printStackTrace();
        }

    }
    
    public boolean isGenerateEndpointCerts() {
        return generateEndpointCerts;
    }

    public void setGenerateEndpointCerts(boolean generateEndpointCerts) {
        this.generateEndpointCerts = generateEndpointCerts;
    }

    public String getCaKeystoreFilename() {
        return caKeystoreFilename;
    }

    public void setCaKeystoreFilename(String caKeystoreFilename) {
        this.caKeystoreFilename = caKeystoreFilename;
    }

    public String getCaKeystorePassword() {
        return caKeystorePassword;
    }

    public void setCaKeystorePassword(String caKeystorePassword) {
        this.caKeystorePassword = caKeystorePassword;
    }

    public void setCertRoot(String certRoot) {
        this.certRoot = certRoot;
    }    

    public void setCaPrivateKeyPassword(String caPrivateKeyPassword) {
        this.caPrivateKeyPassword = caPrivateKeyPassword;
    }
    



}
