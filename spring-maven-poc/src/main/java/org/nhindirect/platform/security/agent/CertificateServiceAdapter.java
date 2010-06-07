package org.nhindirect.platform.security.agent;

import java.security.cert.X509Certificate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.nhindirect.platform.CertificateService;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.stagent.cert.ICertificateService;
import org.nhindirect.stagent.cert.X509CertificateEx;
import org.springframework.beans.factory.annotation.Autowired;

public class CertificateServiceAdapter implements ICertificateService {

    @Autowired
    protected CertificateService certificateService;

    public X509CertificateEx getPrivateCertificate(InternetAddress address) {
        try {
            return X509CertificateEx.fromX509Certificate(getCertificate(address), certificateService
                    .getLocalPrivateKey(HealthAddress.parseEmailAddress(address.getAddress())));
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }

    public X509Certificate getCertificate(InternetAddress address) {
        try {
            return certificateService.getLocalCertificate(HealthAddress.parseEmailAddress(address.getAddress()));
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }

}
