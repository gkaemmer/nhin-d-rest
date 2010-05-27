package org.nhindirect.platform;

public class MessageStoreException extends Exception {

    private static final long serialVersionUID = 1L;

    public MessageStoreException(Exception e) {
        super(e);
    }

    public MessageStoreException(String message) {
        super(message);
    }

}
