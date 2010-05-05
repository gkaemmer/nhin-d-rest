class Status < ActiveRecord::Base
  belongs_to :message
  
  def before_create
    self.status = 'NEW'
  end
  
end
