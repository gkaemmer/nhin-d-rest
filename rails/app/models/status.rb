class Status < ActiveRecord::Base
  belongs_to :message
  validates_presence_of :status
  
  def before_create
    self.status = :new
  end
  
  def status=(status)
    txt = NHIND_STATUSES[status]
    txt ||= status if NHIND_STATUSES.value? status
    write_attribute :status, txt
  end
    
end

NHIND_STATUSES = {
  :new => 'NEW',
  :ack => 'ACK',
  :nack => 'NACK'
}