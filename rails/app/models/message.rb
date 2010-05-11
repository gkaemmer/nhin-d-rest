class Message < ActiveRecord::Base
  attr_readonly :uuid
  has_one :status, :autosave => true
  validates_presence_of :raw_message
  
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
  
  def owned_by(address)
    parsed_message.to.detect { |i| i == address } || parsed_message.from.detect { |i| i == address }
  end  
  
  def parsed_message
    @parsed_object ||= Mail.new(self.raw_message)
    @parsed_object
  end
  
  def validate
    errors.add('to', 'can not be missing') unless parsed_message.to
    errors.add('from', 'can not be missing') unless parsed_message.from
    errors.add('date', 'can not be missing') unless parsed_message.date
    errors.add('message id', 'can not be missing') unless parsed_message.message_id
  end
end
