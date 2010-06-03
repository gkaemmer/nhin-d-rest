package org.nhindirect.platform;

public class CertificateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CertificateException(Exception e) {
        super(e);
    }

    public CertificateException(String message) {
        super(message);
    }
}
