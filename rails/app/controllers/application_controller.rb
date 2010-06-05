# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  helper :all # include all helpers, all the time
  protect_from_forgery # See ActionController::RequestForgeryProtection for details
  helper_method :current_user_session, :current_user, :current_role

  #filter_parameter_logging :password, :password_confirmation
  
  @@hisp_actions = []
  def self.hisp_actions(*actions)
    @@hisp_actions.concat actions
  end
  
  def remote_hisp(domain, endpoint)
    client_certs = Cert.find_by_scope(:hisp)
    cert = client_certs && client_certs[0]
    hisp = RemoteHISP.new(params[:domain],
      :cert => {:cert => OpenSSL::X509::Certificate.new(cert.cert), :key => OpenSSL::PKey::RSA.new(cert.key) },
      :basic => {:user => "#{params[:endpoint]}@#{params[:domain]}", :pw => 'anything'})
    hisp.address = "#{endpoint}@#{domain}"
    hisp
  end
  
  private
  
  def hisp_allowed_action?
    @@hisp_actions.include? params[:action].intern
  end
  
  def validate_ownership(message)
    address = current_user && current_user.login
    return true if current_role == :hisp
    if !message.owned_by(address) then
      head :status => :forbidden
      return false
    end
    return true
  end
  
  def current_user_session
    return @current_user_session if defined?(@current_user_session)
    @current_user_session = UserSession.find
  end
  
  def client_certificate?
    request.env['HTTP_SSL_CLIENT_VERIFY'] == 'SUCCESS'
  end
  
  def basic_auth_user
    if ActionController::HttpAuthentication::Basic.authorization(request) then
      username, _pw = ActionController::HttpAuthentication::Basic.user_name_and_password(request)
      return User.new(:login => username)
    end
    return nil
  end
  
  def current_role
    @current_user_role
  end
  
  def current_user
    if client_certificate? then
      @current_user = basic_auth_user
      if @current_user
        @current_user_role = :edge
      else
        @current_user_role = :hisp
        @current_user = User.new(:login => 'HISP') #TODO: Client CN
      end
    else
      return @current_user if defined?(@current_user)
      @current_user = current_user_session && current_user_session.record
      @current_user_role = :edge if @current_user
    end
    @current_user
  end
  
  def redirect_user
    store_location
    flash[:notice] = "You must be logged in to access this page"
    redirect_to new_user_session_url
    return false
  end
  
  def require_user
    user = current_user
    if current_role == :hisp
      if hisp_allowed_action?
        return true
      else
        head :status => :forbidden
        return false
      end
    end
    unless current_user
      respond_to do |format|
        format.html { redirect_user }
        format.all { head :status => :unauthorized }
      end
    end
  end

  def require_no_user
    if current_user
      store_location
      flash[:notice] = "You must be logged out to access this page"
      redirect_to account_url
      return false
    end
  end
  
  def store_location
    session[:return_to] = request.request_uri
  end
  
  def redirect_back_or_default(default)
    redirect_to(session[:return_to] || default)
    session[:return_to] = nil
  end
  
end
