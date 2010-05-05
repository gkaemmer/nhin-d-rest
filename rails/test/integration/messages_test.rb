require 'test_helper'

class MessagesTest < ActionController::IntegrationTest
  fixtures :messages
  
  def setup
    @auth = ActionController::HttpAuthentication::Basic.encode_credentials('drjones@nhin.happyvalleypractice.example.org', 'drjones_secret')
    @drj_root = '/nhin/v1/nhin.happyvalleypractice.example.org/drjones/messages'
  end
  
  # There should be a messages resource corresponding to the requirements in the specification
  def test_should_route_to_index
    assert_routing @drj_root, {:controller => 'messages', :action => 'index', :domain => 'nhin.happyvalleypractice.example.org', :endpoint => 'drjones'}
  end
  
  # There should be a message resource corresponding to the requirements in the specification
  def test_should_route_to_message
    assert_routing @drj_root + "/176b4be7-3e9b-4a2d-85b7-25a1cd089877",
      {:controller => 'messages', :action => 'show', :domain => 'nhin.happyvalleypractice.example.org',
       :endpoint => 'drjones', :id => '176b4be7-3e9b-4a2d-85b7-25a1cd089877'}
  end
  
  # There should be a message status resource corresponding to the requirements in the specification
  def test_should_route_to_message_status
    assert_routing @drj_root + "/176b4be7-3e9b-4a2d-85b7-25a1cd089877/status",
      {:controller => 'statuses', :action => 'show', :domain => 'nhin.happyvalleypractice.example.org',
       :endpoint => 'drjones', :message_id => '176b4be7-3e9b-4a2d-85b7-25a1cd089877'}
  end
  
  # NHIN-D resources should require authorization
  def test_messages_should_401_when_not_auth    
    get @drj_root
    assert_response :unauthorized
  end  

  # The messages resource should return an atom feed if requested with the proper accept header  
  def test_messages_should_return_atom 
    get @drj_root, nil, {:authorization => @auth, :accept => 'application/atom+xml'}
    assert_response :success
    assert_equal response.content_type, "application/atom+xml"
  end
  
  # The messages resource Atom feed entries should contain links to the individual message resources
  def test_atom_feed_includes_url_to_message
    auth = ActionController::HttpAuthentication::Basic.encode_credentials('drjones@nhin.happyvalleypractice.example.org', 'drjones_secret')
    get @drj_root ,nil, {:authorization => @auth, :accept => 'application/atom+xml'}
    feed = Feedzirra::Feed.parse(response.body)
    entry = feed.entries.first
    assert_equal URI::split(entry.url)[5], @drj_root + '/176b4be7-3e9b-4a2d-85b7-25a1cd089877'
  end
  
  # The messages atom feed should only display messages sent to
  # the logged in user
  def test_atom_feed_has_two_entries
     get @drj_root, nil, {:authorization => @auth, :accept => 'application/atom+xml'}
     feed = Feedzirra::Feed.parse(response.body)
     assert_equal feed.entries.length, 2
   end
   
   # Should be able to create a message by POSTing to the messages resource with the following conditions:
   # * Authenticated
   # * POST is in valid message/rfc822 format
   # * Message contains to header
   # * Content-Type is message/rfc822
   # TODO: remove this * Accepts: is message/rfc822
   # * The response return status code 200, and contains a reference to the newly created message
   # * The newly created message is idetical to the message we POSTed
   def test_create_and_retrieve_message
     post @drj_root, SAMPLE_MESSAGE, {:authorization => @auth, :content_type => 'message/rfc822', :accept => 'message/rfc822'}
     assert_response :success
     assert_equal @response.content_type, 'message/rfc822' 
     loc = @response.location
     get loc, nil, {:authorization => @auth, :accept => 'message/rfc822'}
     assert_equal SAMPLE_MESSAGE, @response.body
   end
   
   def test_initial_message_status
     post @drj_root, SAMPLE_MESSAGE, {:authorization => @auth, :content_type => 'message/rfc822', :accept => 'message/rfc822'}
     loc = @response.location
     get loc + '/status', nil, {:authorization => @auth, :accept => 'text/plain'}
     assert_equal 'NEW', @response.body
   end
      
end

SAMPLE_MESSAGE = <<MESSAGE_END
From: drsmith@nhin.sunnyfamilypractice.example.org
To: drjones@nhin.happyvalleypractice.example.org
Date: Thu, 08 Apr 2010 20:53:17 -0400
Message-ID: <db00ed94-951b-4d47-8e86-585b31fe01be@nhin.sunnyfamilypractice.example.org>
MIME-Version: 1.0
Content-Type: multipart/mixed; boundary="8837833223134.12.9837473322"

This text is traditionally ignored but can
help non-MIME compliant readers provide
information.
--8837833223134.12.9837473322
Content-Type: text/plain

This is the third document I am sending you

--8837833223134.12.9837473322

MESSAGE_END
