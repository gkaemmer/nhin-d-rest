package org.nhindirect.platform.mailutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * NhinSSLSocketFactory - has the trust store setup to trust the JAMES SMTP server connection
 */
public class NhinSSLSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory factory;

    public NhinSSLSocketFactory() {
        try {
            // Load the trust store into a KeyStore. Change the password below as needed.
            KeyStore trustStore = KeyStore.getInstance("jks");
            File cacertsFile = new File("etc/stores/nhintestca/truststore");
            trustStore.load(new FileInputStream(cacertsFile), "password".toCharArray());

            // Initialize factory objects.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, trustManagers, null);
            factory = (SSLSocketFactory) sslcontext.getSocketFactory();
        } catch (Exception ex) {
            // ignore
            System.out.println("ERROR attempting to initialize SSL Socket factory:");
            ex.printStackTrace();
        }
    }

    public static SocketFactory getDefault() {
        return new NhinSSLSocketFactory();
    }

    @Override
    public Socket createSocket() throws IOException {
        return factory.createSocket();
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException {
        return factory.createSocket(socket, s, i, flag);
    }

    @Override
    public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1, int j) throws IOException {
        return factory.createSocket(inaddr, i, inaddr1, j);
    }

    @Override
    public Socket createSocket(InetAddress inaddr, int i) throws IOException {
        return factory.createSocket(inaddr, i);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inaddr, int j) throws IOException {
        return factory.createSocket(s, i, inaddr, j);
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException {
        return factory.createSocket(s, i);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }
}
