package org.nhindirect.platform.rest;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.nhindirect.platform.basic.AbstractUserAwareClass;

public abstract class AbstractNhinDirectController extends AbstractUserAwareClass {

    public AbstractNhinDirectController() {
        super();
    }

    /**
     * Sends a simple HTTP response with a specified content type.
     */
    protected void sendSimpleResponse(HttpServletResponse response, String message, String contentType) throws IOException {
        sendSimpleResponse(response, message, contentType, -1);
    }

    /**
     * Sends a simple HTTP response with a specified status code.
     */
    protected void sendSimpleResponse(HttpServletResponse response, String message, int status) throws IOException {
        sendSimpleResponse(response, message, null, status);
    }

    /**
     * Sends a simple HTTP response.
     */
    protected void sendSimpleResponse(HttpServletResponse response, String message) throws IOException {
        sendSimpleResponse(response, message, null, 200);
    }

    /**
     * Sends a simple HTTP response with a specified content type and and status
     * code.
     */
    private void sendSimpleResponse(HttpServletResponse response, String message, String contentType, int status) throws IOException {
    
        if (status > -1) {
            response.setStatus(status);
        }
    
        if (contentType != null) {
            response.setContentType(contentType);
        }
    
        PrintWriter out = new PrintWriter(response.getWriter());
    
        out.print(message);
        out.close();
    }

}