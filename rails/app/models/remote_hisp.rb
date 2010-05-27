require 'net/http'
require 'net/https'
require 'feedzirra'

class RemoteHISP
  attr_reader :version_path, :domain, :from_health_domain, :from_health_endpoint, :pw, :port, :response
  attr_accessor :message_box
  
  def initialize(hisp_domain, from_user_domain, from_user_endpoint, pw, port = 443, ssl = true, client_cert = nil, client_key = nil)
    @domain = hisp_domain
    @version_path = '/nhin/v1'
    @from_health_domain = from_user_domain
    @from_health_endpoint = from_user_endpoint
    @pw = pw
    @port = port
    @http = Net::HTTP.new(@domain, @port)
    @http.use_ssl = ssl
    if client_cert && client_key then
      @http.use_ssl = true
      @http.cert = client_cert
      @http.key = client_key
      @http.verify_mode = OpenSSL::SSL::VERIFY_PEER
    end
  end
    
  def health_address_path
    @from_health_domain + '/' + @from_health_endpoint
  end
  
  def messages_path
    @version_path + '/' + message_box + '/messages'
  end
  
  def certs_path
    @version_path + '/' + message_box + '/certs'
  end
  
  def user
    from_health_endpoint + '@' + from_health_domain
  end
  
  def get (path, accept)
    @http.start do |http|
      req = Net::HTTP::Get.new(path)
      req.basic_auth(user, pw)
      req['Accept'] = accept
      res = http.request(req)
      res.body
    end
  end
      
  
  def messages
    begin
      feed = Feedzirra::Feed.parse(get(messages_path, 'application/atom+xml'))
    rescue Feezirra::NoParserAvailable
      return nil
    end
    feed.entries.collect { |entry|  URI::split(entry.url)[5]}
  end
  
  def certs
    begin
      feed = Feedzirra::Feed.parse(get(certs_path, 'application/atom+xml'))
    rescue Feedzirra::NoParserAvailable
      return nil
    end
    feed.entries.collect { |entry| OpenSSL::X509::Certificate.new(Base64.decode64(entry.content))}
  end
  
  def create_message(message)
    @http.start do | http|
      req = Net::HTTP::Post.new(messages_path)
      req.basic_auth(user, pw)
      req.content_type = 'message/rfc822'
      req.body = message
      res = http.request(req)
      case res
      when Net::HTTPSuccess then res['Location']
      else
        @response = res
        nil
      end
    end
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
    @http.start do |http|
      req = Net::HTTP::Put.new(status_path(mid))
      req.basic_auth(user, pw)
      req.content_type = 'text/plain'
      req['Accept'] = 'text/plain'
      req.body = status
      res = http.request(req)
      res.body
    end
  end
end