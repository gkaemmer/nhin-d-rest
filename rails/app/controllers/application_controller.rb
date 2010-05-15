# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  helper :all # include all helpers, all the time
  protect_from_forgery # See ActionController::RequestForgeryProtection for details

  # Scrub sensitive parameters from your log
  # filter_parameter_logging :password
  
  before_filter :authenticate
  
  # TODO: Replace hardcoded security with User model
  USERS = [
    {:user => "drjones@nhin.happyvalleypractice.example.org", :pw => "drjones_secret"},
    {:user => "drsmith@nhin.sunnyfamilypractice.example.org", :pw => "drsmith_secret"},
    {:user => 'strange@stranger.example.org', :pw => 'strange_secret'} # For testing ownership
  ]
  
  private
  
  def validate_ownership(message)
    if !message.owned_by(@current_user) then
      head :status => :forbidden
      return false
    end
    return true
  end
  
  def find_by_user(user)
    USERS.detect {|u| u[:user] == user}
  end
  
  def authenticate
    authenticate_or_request_with_http_basic do |user_name, password|
      @current_user = user_name
      if request.env['HTTP_SSL_CLIENT_VERIFY'] == 'SUCCESS' then # Client cert verified, don't need to check password
        return true 
      else
        u = find_by_user(user_name)
        return false unless u
        u[:user] == user_name && u[:pw] == password
      end
    end
  end
  
end
