require 'mime_package'

class Message < ActiveRecord::Base
  attr_readonly :uuid
  has_one :status, :autosave => true
  validates_presence_of :raw_message
  
  def self.find_by_address_and_status(domain, endpoint, status, address)
    current_user_endpoint, current_user_domain = address.split('@')
    find(:all, :include => :status, :conditions => ["to_domain = ? AND to_endpoint = ? AND statuses.status = ? AND " +
      " ((to_domain = ? and to_endpoint = ? ) OR (from_domain = ? and from_domain = ?))",
      domain, endpoint, status, current_user_domain, current_user_endpoint, current_user_domain, current_user_endpoint])
  end
  
  def before_create
    self.uuid = UUIDTools::UUID.random_create.to_s
    self.status = Status.new
    self.status.status = 'NEW'
    self.status.message_uuid = self.uuid
  end
  
  def assign_header_fields
    # TODO: handle multiple to/from
    self.to_domain = Mail::Address.new(self.parsed_message.to[0]).domain if self.parsed_message.to
    self.to_endpoint = Mail::Address.new(self.parsed_message.to[0]).local if self.parsed_message.to
    self.from_domain = Mail::Address.new(self.parsed_message.from[0]).domain if self.parsed_message.from
    self.from_endpoint = Mail::Address.new(self.parsed_message.from[0]).local if self.parsed_message.from
  end
  
  def before_save
    assign_header_fields
  end
  
  def signed_mime_package
    from_cert, from_key, to_certs = Cert.find_mutually_trusted_cred_set_for_send(parsed_message.to, parsed_message.from)
    p7sig = OpenSSL::PKCS7::sign(from_cert, from_key, parsed_message.mime_package, [], OpenSSL::PKCS7::DETACHED)
    smime = OpenSSL::PKCS7::write_smime(p7sig, parsed_message.mime_package)
    smime.gsub("MIME-Version: 1.0\n", '')
  end
    
  def signed_and_encrypted
    from_cert, from_key, to_certs = Cert.find_mutually_trusted_cred_set_for_send(parsed_message.to, parsed_message.from)
    encrypted = OpenSSL::PKCS7::encrypt(to_certs, self.signed_mime_package)
    smime = OpenSSL::PKCS7::write_smime(encrypted)
    parsed_message.non_mime_headers + smime
  end
  
  def self.decrypt(text)
    message = Mail.new(text)
    key_cert_pairs = Cert.find_key_cert_pairs_for_address(message.to)
    p7enc = OpenSSL::PKCS7::read_smime(text)
    for p in key_cert_pairs
      decrypted = p7enc.decrypt(p[:key], p[:cert])
      m = Mail.new(decrypted)
      return Message.new(:raw_message => message.non_mime_headers + decrypted) if m.content_type
    end
    return nil
  end
    
  # 
  # def self.new_from_encrypted(text)
  #   if m
  #     message.mime_package = m
  #     return Message.new(:raw_message => decrypted) if !m.to.nil?
  #   end
  #   return nil
  # end
      
  def signature_verified?
    store = Cert.trust_store
    Cert.add_sender_certs(store, self.from)
    begin
      p7enc = OpenSSL::PKCS7::read_smime(self.raw_source)
    rescue OpenSSL::PKCS7::PKCS7Error
      return false
    end
    return p7enc.verify([], store)
  end
     
  def to_param
    self.uuid
  end
  
  def owned_by(address)
    parsed_message.to.detect { |i| i == address } || parsed_message.from.detect { |i| i == address }
  end  
  
  def parsed_message
    @parsed_object ||= Mail.new(self.raw_message)
  end
  
  def validate
    errors.add('to', 'can not be missing') unless parsed_message.to
    errors.add('from', 'can not be missing') unless parsed_message.from
    errors.add('date', 'can not be missing') unless parsed_message.date
    errors.add('message id', 'can not be missing') unless parsed_message.message_id
  end
end    
    
