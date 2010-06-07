package org.nhindirect.platform.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageServiceException;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

public class RestClient {
    private String keyStoreFilename;
    private String trustStoreFilename;

    private String keyStorePassword;
    private String trustStorePassword;

    private SSLSocketFactory sslSocketFactory;
    private HttpClient httpClient;

    private Log log = LogFactory.getLog(RestClient.class);

    public void init() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(loadKeyManagers(), loadTrustManagers(), null);
        sslSocketFactory = new SSLSocketFactory(sslContext);
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        httpClient = new DefaultHttpClient(params);
        Scheme scheme = new Scheme("https", sslSocketFactory, 8443);
        httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
    }

    public void destroy() {
        httpClient.getConnectionManager().shutdown();
    }

    public void setKeyStoreFilename(String keyStoreFilename) {
        this.keyStoreFilename = keyStoreFilename;
    }

    public void setTrustStoreFilename(String trustStoreFilename) {
        this.trustStoreFilename = trustStoreFilename;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    private TrustManager[] loadTrustManagers() throws Exception {

        KeyStore trustStore = KeyStore.getInstance("jks");
        InputStream is = new FileInputStream(new File(trustStoreFilename));
        try {
            trustStore.load(is, trustStorePassword.toCharArray());
        } finally {
            is.close();
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustStore);

        return tmf.getTrustManagers();
    }

    private KeyManager[] loadKeyManagers() throws Exception {

        KeyStore keyStore = KeyStore.getInstance("jks");
        InputStream is = new FileInputStream(new File(keyStoreFilename));
        try {
            keyStore.load(is, keyStorePassword.toCharArray());
        } finally {
            is.close();
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keyStorePassword.toCharArray());

        return kmf.getKeyManagers();
    }

    /**
     * Returns value of Location HTTP response header.
     */
    public String postMessage(Message message) throws MessageServiceException {

        try {
            String url = "https://" + message.getTo().getDomain() + "/nhin/v1/" + message.getTo().getDomain() + "/"
                    + message.getTo().getEndpoint() + "/messages";
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "message/rfc822");
            request.setEntity(new ByteArrayEntity(message.getData()));

            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new MessageServiceException("Error returned from destination HISP: "
                        + response.getStatusLine().toString());
            }

            Header locationHeader = response.getFirstHeader("Location");
            if (locationHeader == null) {
                return null;
            }

            // release the connection
            response.getEntity().consumeContent();

            return locationHeader.getValue();
        } catch (Exception e) {
            // TODO: Better error handling
            throw new MessageServiceException(e);
        }
    }

    public Collection<X509Certificate> getRemoteCerts(HealthAddress address) throws MessageServiceException {

        ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>();

        try {

            String url = "https://" + address.getDomain() + "/nhin/v1/" + address.getDomain() + "/"
                    + address.getEndpoint() + "/certs";

            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(reader);

            reader.close();
            response.getEntity().consumeContent();

            @SuppressWarnings("unchecked")
            List<SyndEntry> entries = feed.getEntries();
            for (SyndEntry entry : entries) {
                @SuppressWarnings("unchecked")
                List<SyndContent> contents = (List<SyndContent>) entry.getContents();
                for (SyndContent content : contents) {
                    if (content.getType().equals("application/pkix-cert")) {
                        String encodedCert = content.getValue();
                        byte[] rawCert = Base64.decodeBase64(encodedCert);

                        ByteArrayInputStream bis = new ByteArrayInputStream(rawCert);
                        CertificateFactory cf;
                        cf = CertificateFactory.getInstance("X.509");
                        X509Certificate cert = (X509Certificate) cf.generateCertificate(bis);

                        certs.add(cert);
                    }

                }
            }
        } catch (Exception e) {
            // TODO: Better error handling
            throw new MessageServiceException(e);
        }

        log.debug("Successfully retrieved " + certs.size() + " certs for " + address);

        return certs;

    }
}
