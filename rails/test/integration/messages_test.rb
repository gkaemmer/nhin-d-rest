require 'test_helper'

class MessagesTest < ActionController::IntegrationTest
  fixtures :messages
  
  def test_should_route_to_index
    assert_routing '/nhin.happyvalleypractice.example.org/drjones/messages', {:controller => 'messages', :action => 'index', :domain => 'nhin.happyvalleypractice.example.org', :endpoint => 'drjones'}
  end
  
  def test_messages_should_401_when_not_auth    
    get '/nhin.happyvalleypractice.example.org/drjones/messages'
    assert_response :unauthorized
  end  
  
  def test_messages_should_return_atom 
    auth = ActionController::HttpAuthentication::Basic.encode_credentials('drjones@nhin.happyvalleypractice.example.org', 'drjones_secret')
    get '/nhin.happyvalleypractice.example.org/drjones/messages',nil, {:authorization => auth, :accept => 'application/atom+xml'}
    assert_response :success
    assert_equal response.content_type, "application/atom+xml"
  end
    
  def test_atom_feed_includes_url_to_message
    auth = ActionController::HttpAuthentication::Basic.encode_credentials('drjones@nhin.happyvalleypractice.example.org', 'drjones_secret')
    get '/nhin.happyvalleypractice.example.org/drjones/messages.atom',nil, {:authorization => auth, :accept => 'application/atom+xml'}
    feed = Feedzirra::Feed.parse(response.body.to_s)
    entry = feed.entries.first
    assert_equal URI::split(entry.url)[5], '/nhin.happyvalleypractice.example.org/drjones/messages/176b4be7-3e9b-4a2d-85b7-25a1cd089877'
  end
    
end
