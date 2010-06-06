package org.nhindirect.platform.mailutil;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nhindirect.platform.Message;

public class MailClient extends Authenticator {
    protected String from;
    protected Session session;
    protected PasswordAuthentication authentication;

    private Log log = LogFactory.getLog(MailClient.class);

    public MailClient(String user, String pwd, String host) {
        this(user, pwd, host, false);
    }

    public MailClient(String user, String pwd, String host, boolean debug) {
        from = user + '@' + host;
        authentication = new PasswordAuthentication(user, pwd);
        Properties props = new Properties();
        props.put("mail.user", user);
        props.put("mail.host", host);
        props.put("mail.debug", debug ? "true" : "false");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", "465");

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "org.nhindirect.platform.mailutil.NhinSSLSocketFactory");

        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getInstance(props, this);
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return authentication;
    }

    public void sendMailMessage(Message message) throws MessagingException {
        log.debug("SENDING SMTP message from: " + message.getFrom().toString() + " to: " + message.getTo());
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getData());
        MimeMessage msg = new MimeMessage(session, bais);
        // need to add this "junk" header to allow the SMTP server to process as regular mail
        // and not forward on to NHIN. This may not be the best way of doing it but it works
        // for now. As a note the mail server removes this before delivering...
        msg.addHeader("NHIN Internal", "true");
        Transport.send(msg);
    }

}
