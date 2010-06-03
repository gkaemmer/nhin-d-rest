package org.nhin.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.mailet.MailAddress;

public class MailStore {
    protected File home;

    public MailStore(String folder) {
        home = new File(folder);
        if (!home.exists()) {
            home.mkdirs();
        }
    }

    public File getUserMessageFile(MailAddress address) {
        String user = address.getUser();
        File file = new File(home, user + ".txt");
        return file;
    }

    public boolean userMessageExists(MailAddress address) {
        return getUserMessageFile(address).exists();
    }

    public void deleteMessage(MailAddress address) {
        getUserMessageFile(address).delete();
    }

    public void storeMessage(MailAddress address, MimeMessage msg) throws MessagingException, IOException {
        File file = getUserMessageFile(address);
        FileOutputStream out = new FileOutputStream(file);
        msg.writeTo(out);
        out.close();
    }

    public MimeMessage getMessage(MailAddress address) throws MessagingException, IOException {
        File file = getUserMessageFile(address);
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props);
        FileInputStream in = new FileInputStream(file);
        MimeMessage msg = new MimeMessage(session, in);
        in.close();
        return msg;
    }
}
