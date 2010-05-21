package org.nhindirect.platform.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageServiceException;

public class RestClient {
    private String keyStoreFilename;
    private String trustStoreFilename;

    private String keyStorePassword;
    private String trustStorePassword;

    private SSLSocketFactory sslSocketFactory;
    private HttpClient httpClient;

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
        
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        httpClient = new DefaultHttpClient(params);
        Scheme scheme = new Scheme("https", sslSocketFactory, 8443);
        httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
        
        
        try {
            String url = "https://" + message.getTo().getDomain() + "/nhin/v1/" + message.getTo().getDomain() + "/"
                    + message.getTo().getEndpoint() + "/messages";
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "message/rfc822");
            request.setEntity(new ByteArrayEntity(message.getData()));

            HttpResponse response = httpClient.execute(request);
            
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new MessageServiceException("Error returned from destination HISP: " + response.getStatusLine().toString());
            }

            Header locationHeader = response.getFirstHeader("Location");
            if (locationHeader == null) {
                return null;
            }
                                   
            return locationHeader.getValue();
        } catch (Exception e) {
            // TODO: Better error handling
            throw new MessageServiceException(e);
        }
    }
}
