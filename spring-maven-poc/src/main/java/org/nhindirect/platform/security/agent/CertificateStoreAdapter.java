package org.nhindirect.platform.security.agent;

import java.security.cert.X509Certificate;

import javax.mail.internet.InternetAddress;

import org.nhindirect.platform.CertificateService;
import org.nhindirect.platform.DomainService;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.stagent.cert.ICertificateStore;
import org.springframework.beans.factory.annotation.Autowired;

public class CertificateStoreAdapter implements ICertificateStore {

    @Autowired
    protected CertificateService certificateService;
    
    @Autowired
    protected DomainService domainService;
    
    public X509Certificate getCertificate(InternetAddress address) {
        
        HealthAddress ha = HealthAddress.parseEmailAddress(address.getAddress());
        
        if (domainService.isLocalAddress(ha)) {
            return certificateService.getLocalCertificate(HealthAddress.parseEmailAddress(address.getAddress()));
        } else {
            return certificateService.getRemoteCertificate(HealthAddress.parseEmailAddress(address.getAddress()));
        }
    }

}
