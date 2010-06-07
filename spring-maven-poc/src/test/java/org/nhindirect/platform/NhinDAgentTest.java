package org.nhindirect.platform;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.nhindirect.stagent.NHINDAgent;
import org.nhindirect.stagent.cert.X509CertificateEx;
import org.nhindirect.stagent.cert.impl.UniformCertificateService;
import org.nhindirect.stagent.cert.impl.UniformCertificateStore;
import org.nhindirect.stagent.trust.impl.UniformTrustSettings;

@RunWith(MockitoJUnitRunner.class)
public class NhinDAgentTest extends TestCase {

    @Test
    public void testAgent() throws Exception {

        /*
         * This test is an end to end test of the java nhin direct agent. This test should catch
         * breakages in that library.
         */

        URL chrisStore = this.getClass().getResource("/chris.jks");
        URL bobStore = this.getClass().getResource("/bob.jks");
        URL trustStore = this.getClass().getResource("/user-truststore.jks");

        try {

            X509CertificateEx senderCert = null;
            X509CertificateEx receiverCert = null;
            X509Certificate trustCert = null;

            // Load internal cert (chris@nhin.moehisp.com)

            senderCert = loadCertPair(chrisStore);

            // Load external cert (bob@nhin.uberhisp.com)

            receiverCert = loadCertPair(bobStore);

            // Load trust settings

            trustCert = loadCert(trustStore);

            // encrypt message to bob

            NHINDAgent agent = new NHINDAgent("nhin.moehisp.com", new UniformCertificateService(senderCert),
                    new UniformCertificateStore(receiverCert), new UniformTrustSettings(trustCert));

            URL url = this.getClass().getClassLoader().getResource("MultipartMimeMessage.txt");
            InputStream messageIn = url.openStream();
            String messageString = IOUtils.toString(messageIn);

            System.out.println("---------!!!---");

            System.out.println(messageString);

            System.out.println("---------!!!---");

            String encrypted = agent.processOutgoing(messageString);

            System.out.println(encrypted);

            System.out.println("---------!!!---");

            // decrypt message from chris

            NHINDAgent agent2 = new NHINDAgent("nhin.uberhisp.com", new UniformCertificateService(receiverCert),
                    new UniformCertificateStore(senderCert), new UniformTrustSettings(trustCert));

            String decrypted = agent2.processIncoming(encrypted);

            System.out.println(decrypted);

            System.out.println("---------!!!---");

            assertTrue(messageString.equals(decrypted));

        } catch (Exception e) {
            e.printStackTrace();

            throw e;
        }
    }

    private X509CertificateEx loadCertPair(URL storeUrl) throws IOException, NoSuchAlgorithmException,
            CertificateException, KeyStoreException, UnrecoverableKeyException {

        InputStream in = storeUrl.openStream();
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        X509CertificateEx cert = null;

        ks.load(in, "".toCharArray());
        in.close();

        Enumeration<String> aliases = ks.aliases();

        if (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            System.out.println("is cert: " + ks.isCertificateEntry(alias));
            System.out.println("is key: " + ks.isKeyEntry(alias));

            System.out.println(alias);

            Certificate[] certs = ks.getCertificateChain(alias);

            System.out.println(certs);

            cert = X509CertificateEx.fromX509Certificate((X509Certificate) ks.getCertificate(alias), (PrivateKey) ks
                    .getKey(alias, "".toCharArray()));
        }
        return cert;
    }

    private X509Certificate loadCert(URL storeUrl) throws IOException, NoSuchAlgorithmException, CertificateException,
            KeyStoreException, UnrecoverableKeyException {

        InputStream in = storeUrl.openStream();
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        X509Certificate cert = null;

        ks.load(in, "".toCharArray());
        in.close();

        Enumeration<String> aliases = ks.aliases();

        if (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            System.out.println("is cert: " + ks.isCertificateEntry(alias));
            System.out.println("is key: " + ks.isKeyEntry(alias));

            System.out.println(alias);

            Certificate[] certs = ks.getCertificateChain(alias);

            System.out.println(certs);

            cert = (X509Certificate) ks.getCertificate(alias);
        }
        return cert;
    }
}
