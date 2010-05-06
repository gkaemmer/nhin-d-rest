package org.nhindirect.platform.basic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageStatus;
import org.nhindirect.platform.MessageStore;
import org.nhindirect.platform.MessageStoreException;

/**
 * This is a basic implementation of a file based message store. It's goal is
 * not performance, security, or necessarily correctness. It's implemented to
 * have a simple and basic store behind the RESTful proof of concept work. It's
 * not threadsafe or pretty.
 * 
 * Data store directory is PWD/data.
 * 
 * Each health address gets a directory in the datastore /data/{address} where
 * {address} is the email representation of a health address.
 * 
 * Each message is stored as {messageId}.{status}.msg where {messageId} is an
 * number and {status} is the message status.
 * 
 */
public class BasicFileMessageStore implements MessageStore {

    private final String DATA_ROOT = "data";
    private final String MESSAGE_EXTENSION = "msg";
    private final Pattern MESSAGE_FILE_PATTERN = Pattern.compile("([\\w-]*)\\.(.*)\\." + MESSAGE_EXTENSION);

    private Log log = LogFactory.getLog(BasicFileMessageStore.class);

    /**
     * Note that the behavior of this method is inconsistent if the same message
     * id is found twice on the file system with different statuses.
     */
    public Message getMessage(HealthAddress address, UUID messageId) throws MessageStoreException {
        String storePath = getStorePath(address);
        File store = new File(storePath);

        Pattern messagePattern = Pattern.compile(messageId + "\\..*\\." + MESSAGE_EXTENSION);

        Message message = null;

        File[] files = store.listFiles();
        for (File file : files) {
            Matcher m = messagePattern.matcher(file.getName());
            if (m.matches()) {
                message = getMessage(file);
            }
        }

        return message;
    }

    public List<Message> getMessages(HealthAddress address) throws MessageStoreException {
        String storePath = getStorePath(address);

        return getMessages(storePath);
    }

    private List<Message> getMessages(String storePath) throws MessageStoreException {
        File store = new File(storePath);
        File[] files = store.listFiles();
        List<Message> messages = new ArrayList<Message>();

        for (File file : files) {
            Message message = getMessage(file);
            messages.add(message);
        }

        return messages;
    }

    private Message getMessage(File file) throws MessageStoreException {
        Matcher m = MESSAGE_FILE_PATTERN.matcher(file.getName());
        Message message = new Message();
        if (m.matches()) {
            message.setMessageId(UUID.fromString(m.group(1)));
            message.setStatus(MessageStatus.valueOf(m.group(2).toUpperCase()));
            message.setTimestamp(new Date(file.lastModified()));
            

            try {
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                IOUtils.copy(in, out);
                message.setData(out.toByteArray());
                in.close();
            } catch (IOException e) {
                throw new MessageStoreException(e);
            }
        } else {
            throw new MessageStoreException("Message not found.");
        }

        return message;
    }

    public void putMessage(HealthAddress address, Message message) throws MessageStoreException {
        String storePath = getStorePath(address);
        String fileName = getMessageFileName(message.getMessageId(), message.getStatus());

        File messageFile = new File(storePath, fileName);

        try {
            writeMessageToFile(message, messageFile);
        } catch (IOException e) {
            throw new MessageStoreException(e);
        }

    }

    private void writeMessageToFile(Message message, File messageFile) throws FileNotFoundException, IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(messageFile));
        out.write(message.getData());
        out.close();
    }

    private String getMessageFileName(UUID messageId, MessageStatus status) {
        String fileName = messageId + "." + status + "." + MESSAGE_EXTENSION;
        return fileName;
    }

    /**
     * Note that the behavior of this method is inconsistent if the same message
     * id is found twice on the file system with different statuses.
     */
    public void setMessageStatus(HealthAddress address, UUID messageId, MessageStatus status)
            throws MessageStoreException {
        String storePath = getStorePath(address);
        File store = new File(storePath);

        Pattern messagePattern = Pattern.compile(messageId + "\\..*\\." + MESSAGE_EXTENSION);

        File foundFile = null;

        File[] files = store.listFiles();
        for (File file : files) {
            Matcher m = messagePattern.matcher(file.getName());
            if (m.matches()) {
                foundFile = file;
            }
        }

        if (foundFile != null) {
            File newFileName;
            newFileName = new File(foundFile.getParent(), getMessageFileName(messageId, status));
            boolean success = foundFile.renameTo(newFileName);
            if (!success) throw new MessageStoreException("Message Status Update Failed");
        }
    }

    private String getStorePath(HealthAddress address) {
        File storeDirectory = new File(".", DATA_ROOT);
        File addressDirectory = new File(storeDirectory, address.toEmailAddress());

        log.debug("Using address directory " + addressDirectory.getAbsolutePath());

        if (!addressDirectory.exists()) {
            addressDirectory.mkdirs();
        }

        return addressDirectory.getAbsolutePath();
    }

}
