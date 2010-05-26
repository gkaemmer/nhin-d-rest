class Cert < ActiveRecord::Base
  
  
  def self.find_mutually_trusted_cred_set_for_send(to_addrs, from_addrs)
    certs = from_addrs.collect { |a| Cert.find_by_address(a) }
    certs.flatten!
    #TODO: get TO certs
    #TODO: filter TO certs by trust anchors
    #TODO: filter FROM certs by TO trust anchor
    from_cert = certs.first
    return OpenSSL::X509::Certificate.new(from_cert.cert), OpenSSL::PKey::RSA.new(from_cert.key),
      [OpenSSL::X509::Certificate.new(TO_CRT)]
  end
  
  def self.find_key_cert_pairs_for_address(addrs)
    #TODO: include organization and user certificates as well by configuration
    certs = self.find_by_scope(:hisp)
    certs.collect { |c| {:key => OpenSSL::PKey::RSA.new(c.key), :cert => OpenSSL::X509::Certificate.new(c.cert)}}
  end
  
  def self.find_mutually_trusted_certs_for_receipt(to_addrs, from_addrs)
    [OpenSSL::X509::Certificate.new(FROM_CRT)]
  end
  
  def self.trust_store
    store = OpenSSL::X509::Store.new
    certs = self.find_by_scope(:trusted_ca)
    certs.each do |cert|
      store.add_cert OpenSSL::X509::Certificate.new(cert.cert)
    end
    store
  end
  
  def self.find_by_address(domain, endpoint)
    # TODO: include org and user certs as well by configuration
    #u = Users.find_by_login(endpoint '@' domain)
    certs = self.find_by_scope(:hisp)
  end    
  
  def self.add_sender_certs(store, from_addrs)
    store.add_cert OpenSSL::X509::Certificate.new(FROM_CRT)
  end
    
  def self.find_by_scope(symbol)
    self.find(:all, :conditions => ['scope = ?', @@SCOPES[symbol]])
  end
  
  private
  
  @@SCOPES = {
      :trusted_ca => 'TRUSTED_CA',
      :hisp => 'HISP',
      :health_domain => 'HEALTH_DOMAIN',
      :health_endpoint => 'ENDPOINT'
    }  
  
end

FROM_CRT = <<FROM_CRT
-----BEGIN CERTIFICATE-----
MIICfjCCAecCAQEwDQYJKoZIhvcNAQEFBQAwgYMxCzAJBgNVBAYTAlVTMQswCQYD
VQQIEwJDQTEQMA4GA1UEBxMHT2FrbGFuZDEUMBIGA1UEChMLTkhJTiBEaXJlY3Qx
FDASBgNVBAMTC05ISU4gRGlyZWN0MSkwJwYJKoZIhvcNAQkBFhphcmllbi5tYWxl
Y0BuaGluZGlyZWN0Lm9yZzAeFw0xMDA1MjAxNTI2MjNaFw0yMDA1MTcxNTI2MjNa
MIGKMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExEDAOBgNVBAcTB09BS0xBTkQx
EzARBgNVBAoTClN1bm55IEhJU1AxHDAaBgNVBAMTE1N1bm55IEhJU1AgT3BlcmF0
b3IxKTAnBgkqhkiG9w0BCQEWGmFyaWVuLm1hbGVjQG5oaW5kaXJlY3Qub3JnMIGf
MA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDmLiItYNu5tyyPUt2xD/mWXKunc7SM
plEuqoCNBCGiZdfqSnRR9oxvE1wlWwtU/CJPGEGDPFWBuLpba2jOoHDBekUdPSpS
DVCWdRS/8dbYmkfnhfIAssQk8owdLq0VOJH50AWDxoFGBtPz06xdUjjKC7S/a0fv
2FUp4rC/3l/61wIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAJylVySgzDraeKAiyEeP
yZ1qw6zTgKjPa1eIXp7JhVH9N1VITwy2mL3rguvzHdj4eaxW+sMvgPn4MBOGla5d
iJZnnYf9i9+/wofRQa0SHchVL2Lyi/Y92rpDRMQzalUzaweZ9EAOjI4nTywunNNw
e8mxXLZSwyOeiVdty9D0+CpI
-----END CERTIFICATE-----
FROM_CRT


FROM_KEY = <<FROM_KEY
-----BEGIN RSA PRIVATE KEY-----
MIICXwIBAAKBgQDmLiItYNu5tyyPUt2xD/mWXKunc7SMplEuqoCNBCGiZdfqSnRR
9oxvE1wlWwtU/CJPGEGDPFWBuLpba2jOoHDBekUdPSpSDVCWdRS/8dbYmkfnhfIA
ssQk8owdLq0VOJH50AWDxoFGBtPz06xdUjjKC7S/a0fv2FUp4rC/3l/61wIDAQAB
AoGBAM5566aREAjTy2D1kG/YSKcckc4v+HGCb6hQwee9IP6wJLLB/v3XD7pDv5Cy
zsN5OL2CoKG8aWdn8aM4Alf3i6wPFRgzTPsjQKFW9dR610qHQzx3uMoJVj8vWkyf
a5OLhc6r8j5VBHU4uphxE6+vVFhDi6H2MprDHCdIS3NDRClBAkEA9t9H7ZoqCnkx
gBoIO0GkEmeyBm8OcI+mBjtuNCCAsXuQFY4kBDsiegYTHtyHhA0N1psPDxVx3V/c
VcQbrpEDWQJBAO6w26HKcgnuhglYYSPnCAzXU6L80OkJ1ed72MIcvYjpHYPEpm3V
khUxKIHB8wU/gclDQqZ8GT3VJ8SMF6jjGa8CQQDgeWBG3SITCeYHrCQF9YbBsYY/
sWW41fVJv2pSfadnOopl/ywiqL6No85pBm82lEQb+jw0I/S4LT0Ew1/EWFlpAkEA
nsMGI1QrjA2aG8csPZwiz5+9orB0KD0BiQnoQByruNANcKKxbGbc1rmuzrf5c+ks
43iYcXIju+v2mpXv+sarQwJBALDU5rEoNCK1xdv2DWrxbdDvlufnr9LT2jrNwV5l
o+1C7ZleWtiDWVjnQiYTu5DrksXSX8HlCC37RbTgq/DNqZg=
-----END RSA PRIVATE KEY-----
FROM_KEY

TO_CRT = <<TO_CRT
-----BEGIN CERTIFICATE-----
MIIChDCCAe0CAQEwDQYJKoZIhvcNAQEFBQAwgYMxCzAJBgNVBAYTAlVTMQswCQYD
VQQIEwJDQTEQMA4GA1UEBxMHT2FrbGFuZDEUMBIGA1UEChMLTkhJTiBEaXJlY3Qx
FDASBgNVBAMTC05ISU4gRGlyZWN0MSkwJwYJKoZIhvcNAQkBFhphcmllbi5tYWxl
Y0BuaGluZGlyZWN0Lm9yZzAeFw0xMDA1MjAxNzM0MjdaFw0yMDA1MTcxNzM0Mjda
MIGQMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExEDAOBgNVBAcTB09BS0xBTkQx
FjAUBgNVBAoTDUxpdHRsZSBISUUgQ28xHzAdBgNVBAMTFkxpdHRsZSBISUUgQ28g
b3BlcmF0b3IxKTAnBgkqhkiG9w0BCQEWGmFyaWVuLm1hbGVjQG5oaW5kaXJlY3Qu
b3JnMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQClEcq+PnXMMfTKjEXqn1n7
OxyhTxxsjTHPXJ/Mp/uu2tHcrF5zHHs/uRChEP5XODwYyfXjJM5+5IVgJmKEhmai
sxSPA/bOc4UVcLcyvsPr43f30Ua0WKDn30js4UUr+JqBS70yyfqOxWSmZJJo43u4
2q0+AfQQt4dw8tJyzmgE9wIDAQABMA0GCSqGSIb3DQEBBQUAA4GBACEEhfU0ibFM
73emNPpP5sBZ0CSkX535UhBPViVUV5XVQYJ57d3L0yZQRQrSCOSOWQ9bN2eszVsl
h1D33YmonW1npy8W84AshDGYYp4KjHEeQr+pQfoUm46+e1tOC22KNeJi7YhDs2yq
D7b4mDr6WDtMSuewfapVEJdzsTDTRdWz
-----END CERTIFICATE-----
TO_CRT
