package org.nhindirect.platform.security.agent;

public class AgentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AgentException(Exception e) {
        super(e);
    }

    public AgentException(String message) {
        super(message);
    }
}
