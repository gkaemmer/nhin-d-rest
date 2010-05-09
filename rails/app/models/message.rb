class Message < ActiveRecord::Base
  attr_readonly :uuid
  has_one :status, :autosave => true
  
  def self.find_by_address_and_status(domain, endpoint, status)
    find(:all, :include => :status, :conditions => ["to_domain = ? AND to_endpoint = ? AND statuses.status = ?",
      domain, endpoint, status])
  end
  
  def before_create
    self.uuid = UUIDTools::UUID.random_create.to_s
    self.status = Status.new
    self.status.status = 'NEW'
    self.status.message_uuid = self.uuid
  end
  
  def before_save
    self.to_domain = Mail::Address.new(self.parsed_message.to[0]).domain if self.parsed_message.to
    self.to_endpoint = Mail::Address.new(self.parsed_message.to[0]).local if self.parsed_message.to
  end
  
  def to_param
    self.uuid
  end
  
  def parsed_message
    @parsed_object ||= Mail.new(self.raw_message)
    @parsed_object
  end
end
