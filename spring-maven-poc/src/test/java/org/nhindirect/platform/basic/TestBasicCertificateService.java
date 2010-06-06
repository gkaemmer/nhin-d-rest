package org.nhindirect.platform.basic;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nhindirect.platform.HealthAddress;

public class TestBasicCertificateService extends TestCase {

    private BasicCertificateService service; 
    
    @Before
    public void setUp() {
        // Doesn't need to be re-created per test.
        service = new BasicCertificateService();
    }
    
    @After
    public void tearDown() throws IOException {
        try {
            File testdata = new File("testdata");
            FileUtils.deleteDirectory(testdata);
        } catch (IOException e) {
            
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    public void testGetLocalCertificate_generateCert() {
        service.setGenerateEndpointCerts(true);
        service.setCaKeystoreFilename("etc/endpoint-ca.jks");
        service.setCaKeystorePassword("");
        service.setCaPrivateKeyPassword("");
        service.setCertRoot("testdata");
        
        HealthAddress address = new HealthAddress("testdomain", "testendpoint");
        
        X509Certificate cert = service.getLocalCertificate(address);
        
        assertTrue(cert.getSubjectDN().getName().equals("CN=testendpoint@testdomain"));
    }
}
