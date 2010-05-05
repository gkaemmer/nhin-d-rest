package org.nhindirect.platform.rest;

import java.util.List;

import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageStatus;
import org.nhindirect.platform.store.MessageStore;
import org.nhindirect.platform.store.MessageStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for NHIN Direct REST API as defined here: http://nhindirect.org/REST+Implementation
 * 
 * This is not intended as a secure, reliable, or complete implementation. It's intended
 * as a proof of concept on using Spring MVC 3.0 for the REST implementation.
 *
 */
@Controller
@RequestMapping("/1_0/{address}")
public class NhinDirect10Controller {

    @Autowired
    protected MessageStore messageStore;

    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    @ResponseBody
    public String getMessages(@PathVariable("address") String address) throws MessageStoreException {
        HealthAddress ha = HealthAddress.parseUrnAddress(address);
        List<Message> messages = messageStore.getMessages(ha);
        return AtomPublisher.createFeed(messages);
    }

    @RequestMapping(value = "/messages", method = RequestMethod.POST)
    @ResponseBody
    public String postMessage(@PathVariable("address") String address, @RequestBody String message)
            throws MessageStoreException {

        HealthAddress ha = HealthAddress.parseUrnAddress(address);
        Message m = new Message();
        m.setData(message.getBytes());
        m.setStatus("new");

        long messageId = messageStore.putMessage(ha, m);

        return Long.toString(messageId);
    }

    @RequestMapping(value = "/message/{messageId}", method = RequestMethod.GET)
    @ResponseBody
    public String getMessage(@PathVariable("address") String address, @PathVariable("messageId") String messageId)
            throws MessageStoreException {
        HealthAddress ha = HealthAddress.parseUrnAddress(address);
        Message message = messageStore.getMessage(ha, Long.parseLong(messageId));
        return new String(message.getData());
    }

    @RequestMapping(value = "/status/{messageId}", method = RequestMethod.GET)
    @ResponseBody
    public String getMessageStatus(@PathVariable("address") String address, @PathVariable("messageId") String messageId)
            throws MessageStoreException {

        HealthAddress ha = HealthAddress.parseUrnAddress(address);
        Message message = messageStore.getMessage(ha, Long.parseLong(messageId));
        return message.getStatus();
    }

    @RequestMapping(value = "/status/{messageId}", method = RequestMethod.PUT)
    @ResponseBody
    public String setMessageStatus(@PathVariable("address") String address,
                                   @PathVariable("messageId") String messageId, @RequestBody String status)
            throws NumberFormatException, MessageStoreException {

        HealthAddress ha = HealthAddress.parseUrnAddress(address);
        messageStore.setMessageStatus(ha, Long.parseLong(messageId), new MessageStatus(status));
        return "message status updated to " + status + " for message id " + messageId + " for address " + address;
    }
}