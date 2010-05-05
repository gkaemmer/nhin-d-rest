package org.nhindirect.platform.rest;

import java.util.List;

import org.nhindirect.platform.Message;

/**
 * Simple publisher to convert a message list to an Atom feed. Things will get
 * complicated when we need to construct URIs. 
 * 
 * Initially just a proof of concept list of message not in Atom format.
 */
public class AtomPublisher {

    public static String createFeed(List<Message> messages) {
        
        StringBuffer out = new StringBuffer();
        out.append(messages.size() + " message found:\n");
        
        
        for (Message message : messages) {
            out.append("Message id: " + message.getMessageId() + " status:" + message.getStatus() + "\n");
        }
        
        return out.toString();
    }
}
