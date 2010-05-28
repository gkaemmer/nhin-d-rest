/*
 * Copyright (c) 2003-2010 VisionShare, Inc. All Rights Reserved.
 * 
 * This computer program is CONFIDENTIAL and a TRADE SECRET of VisionShare, Inc.
 * The receipt or possession of this program does not convey any rights to use,
 * reproduce or disclose its contents in whole or in part, without the specific
 * written consent of VisionShare, Inc. Any use, reproduction or disclosure of
 * this program without the express written consent of VisionShare, Inc. is a
 * violation of the copyright laws and may subject you to criminal prosecution.
 */

package org.nhin.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.mailet.GenericMailet;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.MailetConfig;

public class NhinMailet extends GenericMailet {
	protected SSLSocketFactory sslSocketFactory;
	
    protected Properties props;
    protected MailStore store;
    protected String truststoreFile;
    protected String truststorePassword;
    protected String baseURL;

    public void init(MailetConfig config) throws MessagingException {
        super.init(config);

        baseURL = getInitParameter("nhinURL");

        String propertiesFile = getInitParameter("nhindprops");
        props = new Properties();
        try {
            BufferedReader in = new BufferedReader(new FileReader(propertiesFile));
            props.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            log("Unable to find properties file: '" + propertiesFile + "' - " +  e);
        } catch (IOException e) {
            log("Unable to read from properties file: '" + propertiesFile + "' - " +  e);
        }

        String folder = getInitParameter("storefolder");
        store = new MailStore(folder);
        log("Init NHIN mailet");

        // Load the trust store into a KeyStore.
        truststoreFile = getInitParameter("truststore");
        truststorePassword = getInitParameter("truststorePassword");
        
		try {
	        KeyStore trustStore;
			trustStore = KeyStore.getInstance("jks");
//	      	File cacertsFile = new File("C:/NHIND/nhin-d-rest/spring-maven-poc/etc/stores/sunnystore");
	        File cacertsFile = new File(truststoreFile);
	        trustStore.load(new FileInputStream(cacertsFile), truststorePassword.toCharArray());

	        // Initialize factory objects.
	        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	        tmf.init(trustStore);

	        // Specify TLS as the required protocol.
	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        TrustManager[] trustManagers = tmf.getTrustManagers();
	        sslContext.init(null, trustManagers, null);

	        // Finally get the socket factory.
	        sslSocketFactory = sslContext.getSocketFactory();
		} catch (Exception e) {
            log("An an exception occurred while trying to setup trust store and SSL message: " + e);
            throw new MessagingException("ERROR trying to create trust store and SSL.", e);
		} 
    }

    protected Address[] toAddressArray(MailAddress address) throws AddressException {
        InternetAddress[] array = new InternetAddress[1];
        array[0] = address.toInternetAddress();
        return array;
    }

    protected MailAddress getFirstAddress(Collection<MailAddress> list) {
        Iterator<MailAddress> iterator = list.iterator();
        return iterator.next();
    }

    protected Collection<MailAddress> addressAsCollection(MailAddress address) {
        Collection<MailAddress> list = new Vector<MailAddress>();
        list.add(address);
        return list;
    }

    @Override
    public void service(Mail mail) throws MessagingException {
        MailAddress sender = mail.getSender();
        MailAddress address = getFirstAddress(mail.getRecipients());
        try {
            log("Writing out mail message");
            MimeMessage msg = (MimeMessage) mail.getMessage();
            store.storeMessage(sender, msg);
            mail.setState(Mail.GHOST);
            // test url: "https://nhin.sunnyfamilypractices.example.org:8443/nhin/v1/"
            postRESTMessage(sender.toString(), address.toString(), baseURL, msg);
        } catch (IOException e) {
            log("Unable to store user message", e);
        } catch (Exception e) {
            log("ERROR when attempting to store user message", e);
        }
    }

    protected void postRESTMessage(String from, String to, String baseHISPURL, MimeMessage msg) throws Exception {
        String hispURL = "";
        String authorization = "anonymous:";

        String[] toAddrParts = to.split("@");
        if (toAddrParts.length > 1) {
            hispURL = baseHISPURL + toAddrParts[1] + "/" + toAddrParts[0] + "/messages";
        } else {
            // we have a problem
            log("Invalid 'to' address - unable to parse out sender and domain: '" + to + "'.");
            return;
        }

        String[] fromAddrParts = from.split("@");
        if (fromAddrParts.length > 1) {
            String propValue = props.getProperty(fromAddrParts[0]);
            if (propValue != null && !propValue.isEmpty()) {
                authorization = fromAddrParts[0] + ":" + propValue;
            } else {
                log("Unable to find authentication for user '" + fromAddrParts[0] + "'. Using default of anonymous.");
            }
        }

        log("Sending to: " + to + " from: " + from + " URL: " + hispURL);

        // Example URL formats:
        // "https://nhin.sunnyfamilypractices.example.org:8443/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/messages"
        // "http://localhost:8080/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/messages"
        final URL url = new URL(hispURL);

        try {
            HttpsURLConnection connect = (HttpsURLConnection) url.openConnection();

            String encoding = Base64.encode(authorization.getBytes());
            log("Encoded authorization: '" + encoding + "'");

            connect.setRequestProperty("Authorization", "Basic " + encoding);

            connect.setRequestMethod("POST");
            connect.setSSLSocketFactory(sslSocketFactory);
            connect.setDoOutput(true);
            connect.setRequestProperty("ContentType", "message/rfc822");

            OutputStream os = connect.getOutputStream();
            msg.writeTo(os);

            log("Connecting to " + url.toString());
            connect.connect();

            log("Result:");
            BufferedReader bis = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            String line;
            while ((line = bis.readLine()) != null) {
                log(line);
            }
        } catch (Exception e) {
            log("An an exception occurred while posting a REST message: " + e);
        }

    }
}
