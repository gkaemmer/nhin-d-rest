package org.nhindirect.platform.security.agent;

import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.mail.internet.InternetAddress;

import org.nhindirect.platform.CertificateService;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.stagent.trust.ITrustSettingsStore;
import org.springframework.beans.factory.annotation.Autowired;

public class TrustSettingsStoreAdapter implements ITrustSettingsStore {

    @Autowired
    protected CertificateService certificateService;
    
    public Collection<X509Certificate> getTrustAnchorsIncoming(InternetAddress address) {
        return certificateService.getInboundTrustAnchors(HealthAddress.parseEmailAddress(address.getAddress()));
    }

    public Collection<X509Certificate> getTrustAnchorsOutgoing(InternetAddress address) {
        return certificateService.getOutboundTrustAnchors(HealthAddress.parseEmailAddress(address.getAddress()));
    }

}
