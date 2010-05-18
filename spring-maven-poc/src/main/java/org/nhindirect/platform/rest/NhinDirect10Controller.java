package org.nhindirect.platform.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageService;
import org.nhindirect.platform.MessageServiceException;
import org.nhindirect.platform.MessageStatus;
import org.nhindirect.platform.MessageStoreException;
import org.nhindirect.platform.basic.AbstractUserAwareClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for NHIN Direct REST API as defined here:
 * http://nhindirect.org/REST+Implementation
 * 
 * This is not intended as a secure, reliable, or complete implementation. It's
 * intended as a proof of concept on using Spring MVC 3.0 for the REST
 * implementation.
 * 
 * I was planning on using the @PreAuthorize annotation for declaritive security
 * but I can't seem to get it working, so I've replaced it with some custom code
 * to check roles.
 * 
 */
@Controller
@RequestMapping("/v1/{healthDomain}/{healthEndpoint}/messages")
public class NhinDirect10Controller extends AbstractUserAwareClass {

    @Autowired
    protected MessageService messageService;

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

        response.setContentType("application/atom+xml");

        Writer out = response.getWriter();
        out.write(AtomPublisher.createFeed(request.getRequestURL().toString(), address, messages));
        out.close();
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

        // This is technically incorrect. The if the response is on a remote
        // HISP, the location header should
        // be that resource on the remote HISP?
        response.setHeader("Location", request.getRequestURL().toString() + "/" + message.getMessageId());

    }

    /**
     * Get a specific message that was sent to the specified health address
     */
    @PreAuthorize("hasRole('ROLE_EDGE')")
    @RequestMapping(value = "/{messageId}", method = RequestMethod.GET)
    public void getMessage(HttpServletResponse response, 
                           @PathVariable("healthDomain") String healthDomain,
                           @PathVariable("healthEndpoint") String healthEndpoint,
                           @PathVariable("messageId") String messageId) throws MessageStoreException,
            MessageServiceException, IOException {

        
        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageService.getMessage(address, UUID.fromString(messageId));
        
        if (message == null) {
            response.setStatus(404);
            
            PrintWriter out = new PrintWriter(response.getWriter());            
            out.print("Message " + messageId + " not found");
            out.close();
        } else {
            response.setContentType("message/rfc822");
            
            Writer out = response.getWriter();
            out.write(new String(message.getData()));
            out.close();
        }
    }

    /**
     * Get the status of a specific message sent to the specified health address
     */
    @PreAuthorize("hasRole('ROLE_EDGE')")
    @RequestMapping(value = "/{messageId}/status", method = RequestMethod.GET)
    @ResponseBody
    public String getMessageStatus(@PathVariable("healthDomain") String healthDomain,
                                   @PathVariable("healthEndpoint") String healthEndpoint,
                                   @PathVariable("messageId") String messageId) throws MessageStoreException,
            MessageServiceException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageService.getMessage(address, UUID.fromString(messageId));

        return message.getStatus().toString();
    }

    /**
     * Set the status of a specific message sent to the specified health address
     */
    @PreAuthorize("hasRole('ROLE_EDGE')")
    @RequestMapping(value = "/{messageId}/status", method = RequestMethod.PUT)
    @ResponseBody
    public String setMessageStatus(@PathVariable("healthDomain") String healthDomain,
                                   @PathVariable("healthEndpoint") String healthEndpoint,
                                   @PathVariable("messageId") String messageId, @RequestBody String status)
            throws NumberFormatException, MessageStoreException, MessageServiceException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        messageService.setMessageStatus(address, UUID.fromString(messageId), MessageStatus
                .valueOf(status.toUpperCase()));
        
        Message message = messageService.getMessage(address, UUID.fromString(messageId));

        return message.getStatus().toString();
    }

    /**
     * Process MessageServiceException as an HTTP 400 error and return simple explanation to client.
     */    
    @ExceptionHandler(MessageServiceException.class)
    public void handleMessageServiceException(Exception e, HttpServletResponse response) throws IOException {
        sendSimpleErrorResponse(e, response, 400);
    }
    
    /**
     * Process MessageStoreException as an HTTP 500 error and return simple explanation to client.
     */
    @ExceptionHandler(MessageStoreException.class)
    public void handleMessageStoreException(Exception e, HttpServletResponse response) throws IOException {
        sendSimpleErrorResponse(e, response, 500);
    }
    
    private void sendSimpleErrorResponse(Exception e, HttpServletResponse response, int status) throws IOException {
        response.setStatus(400);
        
        PrintWriter out = new PrintWriter(response.getWriter());
        
        out.print(e.getMessage());
        out.close();
    }
}