package org.nhindirect.platform;

public class MessageServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    public MessageServiceException(Exception e) {
        super(e);
    }

    public MessageServiceException(String message) {
        super(message);
    }
}
