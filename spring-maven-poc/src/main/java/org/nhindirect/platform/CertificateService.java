package org.nhindirect.platform;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;

public interface CertificateService {
    public X509Certificate getLocalCertificate(HealthAddress address);
    public X509Certificate getRemoteCertificate(HealthAddress address);
    public PrivateKey getLocalPrivateKey(HealthAddress address);
    public Collection<X509Certificate> getInboundTrustAnchors(HealthAddress address);
    public Collection<X509Certificate> getOutboundTrustAnchors(HealthAddress address);
}
