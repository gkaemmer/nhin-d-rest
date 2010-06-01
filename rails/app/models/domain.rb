class Domain < ActiveRecord::Base
  
  def self.local?(domain)
    d = self.find_by_domain(domain)
    d && d.local
  end
  
  def self.remote?(domain)
    !self.local? domain
  end
end
