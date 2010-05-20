require 'net/http'

class RemoteHISP
  attr_reader :version_path, :domain, :from_health_domain, :from_health_endpoint, :pw, :port
  attr_accessor :message_box
  
  def initialize(hisp_domain, from_user_domain, from_user_endpoint, pw, port = 80)
    @domain = hisp_domain
    @version_path = '/nhin/v1'
    @from_health_domain = from_user_domain
    @from_health_endpoint = from_user_endpoint
    @pw = pw
    @port = port
  end
    
  def health_address_path
    @from_health_domain + '/' + @from_health_endpoint
  end
  
  def messages_path
    @version_path + '/' + message_box + '/messages'
  end
  
  def user
    from_health_endpoint + '@' + from_health_domain
  end
  
  def get (path, accept)
    req = Net::HTTP::Get.new(path)
    req.basic_auth(user, pw)
    req['Accept'] = accept
    res = Net::HTTP.new(@domain, @port).start { |http| http.request(req) }
    res.body
  end
      
  
  def messages
    get(messages_path, 'application/atom+xml')
  end
  
  def create_message(message)
    req = Net::HTTP::Post.new(messages_path)
    req.basic_auth(user, pw)
    req.content_type = 'message/rfc822'
    req.body = message
    res = Net::HTTP.new(@domain, @port).start { |http| http.request(req) }
    res['Location']
  end
  
  def message_path(mid)
    messages_path + '/' + mid
  end
  
  def message(mid)
    get(message_path(mid), 'message/rfc822')
  end
  
  def status_path(mid)
    message_path(mid) + '/status'
  end
  
  def status(mid)
    get(status_path(mid), 'text/plain')
  end
  
  def update_status(mid, status)
    req = Net::HTTP::Put.new(status_path(mid))
    req.basic_auth(user, pw)
    req.content_type = 'text/plain'
    req['Accept'] = 'text/plain'
    req.body = status
    res = Net::HTTP.new(@domain, @port).start { |http| http.request(req) }
    res.body
  end
end