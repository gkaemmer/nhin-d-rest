package org.nhindirect.platform;

import java.io.IOException;

public class MessageStoreException extends Exception {

    private static final long serialVersionUID = 1L;

    public MessageStoreException(IOException e) {
        super(e);
    }

    public MessageStoreException(String message) {
        super(message);
    }

}
