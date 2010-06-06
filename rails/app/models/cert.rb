class Cert < ActiveRecord::Base
  
  
  def self.get_remote_certs(addr, hisp)
    address = Mail::Address.new(addr)
    hisp.address = "#{address.local}@#{address.domain}"
    hisp.certs
  end
  
  def self.find_mutually_trusted_cred_set_for_send(to, from, hisp)
    to = to[0]
    from = from[0]
    #TODO: handle multiple to, from
    addr = Mail::Address.new(from)
    from_certs = Cert.find_by_address(addr.domain, addr.local)
    to_certs = Cert.get_remote_certs(to, hisp)
    #TODO: filter TO certs by trust anchors
    #TODO: filter FROM certs by TO trust anchor
    from_cert = from_certs.first
    to_cert = to_certs.first
    return OpenSSL::X509::Certificate.new(from_cert.cert), OpenSSL::PKey::RSA.new(from_cert.key),
      [OpenSSL::X509::Certificate.new(to_cert)]
  end
  
  def self.find_key_cert_pairs_for_address(addrs)
    #TODO: include organization and user certificates as well by configuration
    certs = self.find_by_scope(:hisp)
    certs.collect { |c| {:key => OpenSSL::PKey::RSA.new(c.key), :cert => OpenSSL::X509::Certificate.new(c.cert)}}
  end
  
  def self.find_mutually_trusted_certs_for_receipt(to_addrs, from)
    # TODO: Verify and filter by trust anchors
    Cert.get_remote_certs(from_addr)
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
    certs = self.find_by_scope(:hisp)
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