require 'net/http'
require 'net/https'
require 'feedzirra'

class RemoteHISP
  attr_reader :version_path, :domain, :user, :pw, :cert, :key, :port, :http
  attr_accessor :message_box

  def set_auth_type(sym)
    @use_basic_auth = sym == :basic || sym = :both
    @use_cert_auth = sym == :cert || sym = :both
  end

  def cert_auth?
    @use_cert_auth
  end
  
  def basic_auth?
    @use_basic_auth
  end
  
  
  def auth_options(options)
    basic = options[:basic]
    cert = options[:cert]
    if basic then
      @use_basic_auth = true
      @user = basic[:user]
      @pw = basic[:pw]
    end
    if cert then
      @use_cert_auth = true
      @cert = cert[:cert]
      @key = cert[:key]
    end
  end
  # Options takes a hash
  # RemoteHISP.new('www.example.com', :basic => {:user => 'jsmith@example,com', :pw => 's3cr1t'})
  # RemoteHISP.new('www.example.com', :cert => {:cert => OpenSSL::X509::Certificate.new(cert_pem), OpenSSL:PKey:RSA.new(key_pem)})
  # Basic and certificate based auth can be combined
  # Other options:
  # * :ssl => false (defaults true)
  # * :port => '80' (default 443)
  def initialize(hisp_domain, options={})
    auth_options(options)
    @domain = hisp_domain
    @version_path = '/nhin/v1'
    @port = options[:port] || '433'
    @http = Net::HTTP.new(@domain, @port)
    options[:ssl] = true if options[:ssl].nil?
    @http.use_ssl = options[:ssl]
    if cert_auth? then
      @http.use_ssl = true
      @http.cert = @cert
      @http.key = @key
      @http.verify_mode = OpenSSL::SSL::VERIFY_PEER
    end
  end
  
  def messages_path
    @version_path + '/' + message_box + '/messages'
  end
  
  def certs_path
    @version_path + '/' + message_box + '/certs'
  end
  
  
  def get (path, accept)
    @http.start do |http|
      req = Net::HTTP::Get.new(path)
      req.basic_auth(user, pw) if basic_auth?
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
      req.basic_auth(user, pw) if basic_auth?
      req.content_type = 'message/rfc822'
      req.body = message
      res = http.request(req)
      case res
      when Net::HTTPSuccess, Net::HTTPRedirection then
        res['Location']
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