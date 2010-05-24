package org.nhindirect.platform.rest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageService;
import org.nhindirect.platform.MessageServiceException;
import org.nhindirect.platform.MessageStatus;
import org.nhindirect.platform.MessageStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for NHIN Direct REST API as defined here: http://nhindirect.org/REST+Implementation
 * 
 * This is not intended as a secure, reliable, or complete implementation. It's intended as a proof
 * of concept on using Spring MVC 3.0 for the REST implementation.
 * 
 * 
 */
@Controller
@RequestMapping("/v1/{healthDomain}/{healthEndpoint}/messages")
public class NhinDirect10Controller extends AbstractNhinDirectController {

    @Autowired
    protected MessageService messageService;

    private Log log = LogFactory.getLog(NhinDirect10Controller.class);

    /**
     * Get messages addressed to a specified health address.
     */
    @PreAuthorize("hasRole('ROLE_EDGE')")
    @RequestMapping(method = RequestMethod.GET)
    public void getMessages(HttpServletRequest request, HttpServletResponse response,
                            @PathVariable("healthDomain") String healthDomain,
                            @PathVariable("healthEndpoint") String healthEndpoint) throws MessageStoreException,
            MessageServiceException, IOException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        List<Message> messages = messageService.getNewMessages(address);

        String atomFeed = AtomPublisher.createFeed(request.getRequestURL().toString(), address, messages);

        sendSimpleResponse(response, atomFeed, "application/atom+xml");
    }

    /**
     * Post a message to a specified health address.
     */
    @PreAuthorize("hasRole('ROLE_EDGE') or hasRole('ROLE_HISP')")
    @RequestMapping(method = RequestMethod.POST)
    public void postMessage(HttpServletRequest request, HttpServletResponse response,
                            @PathVariable("healthDomain") String healthDomain,
                            @PathVariable("healthEndpoint") String healthEndpoint, @RequestBody String rawMessage)
            throws MessageStoreException, MessageServiceException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageService.handleMessage(address, rawMessage);
        response.setHeader("Location", request.getRequestURL().toString() + "/" + message.getMessageId());
    }

    /**
     * Get a specific message that was sent to the specified health address
     */
    @PreAuthorize("hasRole('ROLE_EDGE')")
    @RequestMapping(value = "/{messageId}", method = RequestMethod.GET)
    public void getMessage(HttpServletResponse response, @PathVariable("healthDomain") String healthDomain,
                           @PathVariable("healthEndpoint") String healthEndpoint,
                           @PathVariable("messageId") String messageId) throws MessageStoreException,
            MessageServiceException, IOException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageService.getMessage(address, UUID.fromString(messageId));

        if (message == null) {
            sendSimpleResponse(response, "Message " + messageId + " not found", 404);
        } else {
            sendSimpleResponse(response, new String(message.getData()), "message/rfc822");
        }
    }

    /**
     * Get the status of a specific message sent to the specified health address
     */
    @PreAuthorize("hasRole('ROLE_EDGE')")
    @RequestMapping(value = "/{messageId}/status", method = RequestMethod.GET)
    public void getMessageStatus(HttpServletResponse response, @PathVariable("healthDomain") String healthDomain,
                                 @PathVariable("healthEndpoint") String healthEndpoint,
                                 @PathVariable("messageId") String messageId) throws MessageStoreException,
            MessageServiceException, IOException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageService.getMessage(address, UUID.fromString(messageId));

        if (message == null) {
            sendSimpleResponse(response, "Message " + messageId + " not found", 404);
        } else {
            sendSimpleResponse(response, message.getStatus().toString());
        }

    }

    /**
     * Set the status of a specific message sent to the specified health address. If it's the edge
     * recipient changing the status, then store the change and PUT that status back to the sender
     * HISP. If if the's destination HISP pushing the status back, then store the status change.
     */
    @PreAuthorize("hasRole('ROLE_EDGE') or hasRole('ROLE_HISP')")
    @RequestMapping(value = "/{messageId}/status", method = RequestMethod.PUT)
    public void setMessageStatus(HttpServletResponse response, @PathVariable("healthDomain") String healthDomain,
                                 @PathVariable("healthEndpoint") String healthEndpoint,
                                 @PathVariable("messageId") String messageId, @RequestBody String status)
            throws NumberFormatException, MessageStoreException, MessageServiceException, IOException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        messageService.setMessageStatus(address, UUID.fromString(messageId), MessageStatus
                .valueOf(status.toUpperCase()));

        sendSimpleResponse(response, status);
    }

    /**
     * Process MessageServiceException as an HTTP 400 error and return simple explanation to client.
     */
    @ExceptionHandler(MessageServiceException.class)
    public void handleMessageServiceException(Exception e, HttpServletResponse response) throws IOException {
        log.error(e);
        sendSimpleResponse(response, e.getMessage(), 400);
    }

    /**
     * Process MessageStoreException as an HTTP 500 error and return simple explanation to client.
     */
    @ExceptionHandler(MessageStoreException.class)
    public void handleMessageStoreException(Exception e, HttpServletResponse response) throws IOException {
        log.error(e);
        sendSimpleResponse(response, e.getMessage(), 500);
    }
}