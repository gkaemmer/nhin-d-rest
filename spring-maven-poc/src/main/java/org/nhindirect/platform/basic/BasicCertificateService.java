package org.nhindirect.platform.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nhindirect.platform.CertificateException;
import org.nhindirect.platform.CertificateService;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.MessageServiceException;
import org.nhindirect.platform.rest.RestClient;
import org.springframework.beans.factory.annotation.Autowired;

public class BasicCertificateService implements CertificateService {

    private final String CERT_ROOT = "data";
    private final String USER_CERTS_FILE_NAME = "certs";
    private final String TRUST_INCOMING_FILE_NAME = "trust-in";
    private final String TRUST_OUTGOING_FILE_NAME = "trust-out";
    private final String STORE_EXTENSION = ".jks";

    public Log log = LogFactory.getLog(BasicCertificateService.class);
    
    @Autowired
    private RestClient restClient;

    public X509Certificate getLocalCertificate(HealthAddress address) {

        File userKeystore = getUserKeystore(address);
        X509Certificate cert;

        try {
            cert = loadCert(userKeystore);
        } catch (Exception e) {
            log.error("Unable to load cert for address: " + address.toEmailAddress() + " caused by " + e.getMessage());
            throw new CertificateException("Unable to load cert for address: " + address.toEmailAddress()
                    + " caused by " + e.getMessage());
        }

        return cert;
    }

    public PrivateKey getLocalPrivateKey(HealthAddress address) {
        File userKeystore = getUserKeystore(address);
        PrivateKey key;

        try {
            key = loadKey(userKeystore);
        } catch (Exception e) {
            log.error("Unable to load cert for address: " + address.toEmailAddress() + " caused by " + e.getMessage());

            throw new CertificateException("Unable to load cert for address: " + address.toEmailAddress()
                    + " caused by " + e.getMessage());
        }

        return key;
    }

    public X509Certificate getRemoteCertificate(HealthAddress address) {        
        Collection<X509Certificate> certs = null;
        
        try {
            certs = restClient.getRemoteCerts(address);
            
            return (X509Certificate)certs.toArray()[0]; 
        } catch (MessageServiceException e) {
            // TODO: do something better here
            
            throw new CertificateException(e);
        }
    }

    public Collection<X509Certificate> getInboundTrustAnchors(HealthAddress address) {
        File anchorsFile = new File(CERT_ROOT + "/" + address.toEmailAddress() + "/" + TRUST_INCOMING_FILE_NAME + STORE_EXTENSION);

        try {
            return loadAllCerts(anchorsFile);
        } catch (Exception e) {
            log.error("Unable to trust anchors caused by " + e.getMessage());
            throw new CertificateException("Unable to trust anchors caused by " + e.getMessage());
        }
    }
    
    public Collection<X509Certificate> getOutboundTrustAnchors(HealthAddress address) {
        File anchorsFile = new File(CERT_ROOT + "/" + address.toEmailAddress() + "/" + TRUST_OUTGOING_FILE_NAME + STORE_EXTENSION);

        try {
            return loadAllCerts(anchorsFile);
        } catch (Exception e) {
            log.error("Unable to trust anchors caused by " + e.getMessage());
            throw new CertificateException("Unable to trust anchors caused by " + e.getMessage());
        }
    }

    
    private File getUserKeystore(HealthAddress address) {
        File userKeystore = new File(CERT_ROOT + "/" + address.toEmailAddress() + "/" + USER_CERTS_FILE_NAME + STORE_EXTENSION);
        return userKeystore;
    }
    
    private X509Certificate loadCert(File certFile) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, java.security.cert.CertificateException {
        InputStream in = new FileInputStream(certFile);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        X509Certificate cert = null;

        ks.load(in, "".toCharArray());
        in.close();

        Enumeration<String> aliases = ks.aliases();

        if (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            cert = (X509Certificate) ks.getCertificate(alias);
        }
        return cert;
    }

    public PrivateKey loadKey(File keyFile) throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException, UnrecoverableKeyException, java.security.cert.CertificateException {
        InputStream in = new FileInputStream(keyFile);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        PrivateKey key = null;

        ks.load(in, "".toCharArray());
        in.close();        
        Enumeration<String> aliases = ks.aliases();

        if (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            key = (PrivateKey) ks.getKey(alias, "".toCharArray());
        }

        return key;
    }

    public Collection<X509Certificate> loadAllCerts(File certFile) throws KeyStoreException, NoSuchAlgorithmException, java.security.cert.CertificateException, IOException {

        ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>();
        
        InputStream in = new FileInputStream(certFile);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        X509Certificate cert = null;

        ks.load(in, "".toCharArray());
        in.close();

        Enumeration<String> aliases = ks.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            cert = (X509Certificate) ks.getCertificate(alias);
            certs.add(cert);
        }
        
        return certs;
    }


}
