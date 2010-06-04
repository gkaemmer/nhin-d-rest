require 'test_helper'
require 'authlogic/test_case'

class MessagesTest < ActionController::IntegrationTest
  fixtures :all
  
  def setup
    @auth = ActionController::HttpAuthentication::Basic.encode_credentials('drjones@nhin.happyvalleypractice.example.org', 'drjones_secret')
    @strange_auth = ActionController::HttpAuthentication::Basic.encode_credentials('strange@stranger.example.org', 'strange_secret')
    @drj_root = '/nhin/v1/nhin.happyvalleypractice.example.org/drjones/messages'
    activate_authlogic
  end
  
  should 'be able to route to the messages resource' do
    assert_routing @drj_root, {:controller => 'messages', :action => 'index', :domain => 'nhin.happyvalleypractice.example.org', :endpoint => 'drjones'}
  end
  
  should 'be able to route to the message resource for a specific message id' do 
    assert_routing @drj_root + "/176b4be7-3e9b-4a2d-85b7-25a1cd089877",
      {:controller => 'messages', :action => 'show', :domain => 'nhin.happyvalleypractice.example.org',
       :endpoint => 'drjones', :id => '176b4be7-3e9b-4a2d-85b7-25a1cd089877'}
  end
  
  should 'be able to route to the status resource for a specific message id' do
    assert_routing @drj_root + "/176b4be7-3e9b-4a2d-85b7-25a1cd089877/status",
      {:controller => 'statuses', :action => 'show', :domain => 'nhin.happyvalleypractice.example.org',
       :endpoint => 'drjones', :message_id => '176b4be7-3e9b-4a2d-85b7-25a1cd089877'}
  end
  
  should 'return 401 when viewing messages without authorization' do
    get @drj_root, nil, {:accept => 'application/atom+xml'}
    assert_response :unauthorized
  end 
  
  should 'return 401 when viewing a message without authorization' do
    get @drj_root + "/176b4be7-3e9b-4a2d-85b7-25a1cd089877", nil, {:accept => 'message/rfc822'}
    assert_response :unauthorized
  end
  
  should 'return 401 when viewing a message status without authorization' do
    get @drj_root + "/176b4be7-3e9b-4a2d-85b7-25a1cd089877/status", nil, {:accept => 'text/plain'}
    assert_response :unauthorized
  end

  should 'have an Atom representation of the messages resource' do
    get @drj_root, nil, {:authorization => @auth, :accept => 'application/atom+xml'}
    assert_response :success
    assert_equal response.content_type, "application/atom+xml"
  end
  
  should 'have an Atom representation of the messages resource with links to the individual message URIs' do
    auth = ActionController::HttpAuthentication::Basic.encode_credentials('drjones@nhin.happyvalleypractice.example.org', 'drjones_secret')
    get @drj_root ,nil, {:authorization => @auth, :accept => 'application/atom+xml'}
    feed = Feedzirra::Feed.parse(response.body)
    entry = feed.entries.first
    assert_equal URI::split(entry.url)[5], @drj_root + '/176b4be7-3e9b-4a2d-85b7-25a1cd089877'
  end
  
  should 'have an Atom represetentation of the messages resource with two entries for the specified test data' do
    get @drj_root, nil, {:authorization => @auth, :accept => 'application/atom+xml'}
    feed = Feedzirra::Feed.parse(response.body)
    assert_equal feed.entries.length, 2
  end
   
   should 'have an Atom representation of the messages resource with zero entries for an authenticated user with no access to the messages' do
     get @drj_root, nil, {:authorization => @strange_auth, :accept => 'application/atom+xml'}
     feed = Feedzirra::Feed.parse(response.body)
     assert_equal feed.entries.length, 0   
   end
   
   should 'be able to POST a new message at the messages resource and retrieve the resulting message resource' do
     # Rails integration testing requires the inclusion of the Accept header, but this is not required
     # in real life (just can't set one of the HTML accept types)
     post @drj_root, SAMPLE_MESSAGE, {:authorization => @auth, :content_type => 'message/rfc822', :accept => 'message/rfc822'}
     assert_response :created
     loc = @response.location
     get loc, nil, {:authorization => @auth, :accept => 'message/rfc822'}
     assert_equal SAMPLE_MESSAGE, @response.body
   end

   def create_message(message)
     post @drj_root, message, {:authorization => @auth, :content_type => 'message/rfc822', :accept => 'message/rfc822'}
     return @response.location
   end
   
   def status_uri(message_uri)
     return message_uri + '/status'
   end
   
   should 'have status NEW upon message creation' do
     loc = create_message(SAMPLE_MESSAGE)
     get status_uri(loc), nil, {:authorization => @auth, :accept => 'text/plain'}
     assert_response :success
     assert_equal 'NEW', @response.body
   end
   
   should 'be able to update message status to ACK' do
     loc = create_message(SAMPLE_MESSAGE)
     put status_uri(loc), 'ACK', {:authorization => @auth, :content_type => 'text/plain', :accept => 'text/plain'}
     assert_response :success
     get status_uri(loc), nil, {:authorization => @auth, :accept => 'text/plain'}
     assert_equal 'ACK', @response.body
   end
   
   def create_message_and_update_status(message, status)
     loc = create_message(SAMPLE_MESSAGE)
     put status_uri(loc), status, {:authorization => @auth, :content_type => 'text/plain', :accept => 'text/plain'}
     return loc
   end
   
   should 'be able to update message status to NACK' do
     loc = create_message_and_update_status(SAMPLE_MESSAGE, 'NACK')
     get status_uri(loc), nil, {:authorization => @auth, :accept => 'text/plain'}
     assert_equal 'NACK', @response.body
   end
   
   should 'not be able to update message status to an invalid status' do
     create_message_and_update_status(SAMPLE_MESSAGE, 'UCK')
     assert_response :not_acceptable
   end
   
   should 'be able to query messages by status' do
     create_message_and_update_status(SAMPLE_MESSAGE, 'ACK')
     drj_status_root = @drj_root + '?status='
     get drj_status_root + 'NEW', nil, {:authorization => @auth, :accept => 'application/atom+xml'}
     feed = Feedzirra::Feed.parse(response.body)
     assert_equal feed.entries.length, 2
     get drj_status_root + 'ACK', nil, {:authorization => @auth, :accept => 'application/atom+xml'}
     feed = Feedzirra::Feed.parse(response.body)
     assert_equal feed.entries.length, 1
   end
   
   should 'not be able to create message if not the owner' do
     post @drj_root, SAMPLE_MESSAGE, {:authorization => @strange_auth, :content_type => 'message/rfc822', :accept => 'message/rfc822'}
     assert_response :forbidden
   end
   
   should 'not be able to view message if not the owner' do
     get @drj_root + '/176b4be7-3e9b-4a2d-85b7-25a1cd089877', nil, {:authorization => @strange_auth, :accept => 'message/rfc822'}
     assert_response :forbidden
   end
   
   should 'not be able to view or update status if not the owner' do
     loc = @drj_root + '/176b4be7-3e9b-4a2d-85b7-25a1cd089877'
     get status_uri(loc), nil, {:authorization => @strange_auth, :accept => 'text/plain'}
     assert_response :forbidden
     put status_uri(loc), 'ACK', {:authorization => @strange_auth, :content_type => 'text/plain', :accept => 'text/plain'}
     assert_response :forbidden
   end
   
   should 'be able to test for local vs remote domain' do
     assert Domain.local? 'nhin.happyvalleypractice.example.org'
     assert Domain.local? 'nhin.sunnyfamilypractice.example.org'
     assert !(Domain.local? 'nhin.prettyvalleycare.example.org')
     assert !(Domain.local? 'a.domain.i.dont.recognize.example.org')
     assert !(Domain.remote? 'nhin.happyvalleypractice.example.org')
     assert !(Domain.remote? 'nhin.sunnyfamilypractice.example.org')
     assert Domain.remote? 'nhin.prettyvalleycare.example.org'
     assert Domain.remote? 'a.domain.i.dont.recognize.example.org'
   end
   
   should 'create messge at remote HISP if domain is not local' do
     remote_path = '/nhin/v1/nhin.prettyvalleycare.example.org/remote/messages'
     remote_message_path = remote_path + '/99ff140c-25e4-4a71-819b-f49420d36deb'
     RemoteHISP.any_instance.stubs(:create_message).returns remote_message_path
     post remote_path, REMOTE_MESSAGE, {:authorization => @auth, :content_type => 'message/rfc822', :accept => 'message/rfc822'}
     assert_response :success
     assert_equal remote_message_path, @response.location
   end
   
   should 'update status at remote HISP if domain is not local' do
     remote_path = '/nhin/v1/nhin.prettyvalleycare.example.org/remote/messages'
     remote_message_path = remote_path + '/99ff140c-25e4-4a71-819b-f49420d36deb'
     RemoteHISP.any_instance.stubs(:create_message).returns remote_message_path
     RemoteHISP.any_instance.stubs(:update_status).returns 'ACK'
     post remote_path, REMOTE_MESSAGE, {:authorization => @auth, :content_type => 'message/rfc822', :accept => 'message/rfc822'}
     put status_uri(@response.location), 'ACK', {:authorization => @auth, :content_type => 'text/plain', :accept => 'text/plain'}
     assert_response :success
     assert_equal 'ACK', @response.body
   end
   
   should 'get status at remote HISP if domain is not local' do
     remote_path = '/nhin/v1/nhin.prettyvalleycare.example.org/remote/messages'
     remote_message_path = remote_path + '/99ff140c-25e4-4a71-819b-f49420d36deb'
     RemoteHISP.any_instance.stubs(:create_message).returns remote_message_path
     RemoteHISP.any_instance.stubs(:status).returns 'NEW'
     post remote_path, REMOTE_MESSAGE, {:authorization => @auth, :content_type => 'message/rfc822', :accept => 'message/rfc822'}
     get status_uri(@response.location), nil, {:authorization => @auth, :accept => 'text/plain'}
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

--8837833223134.12.9837473322--

MESSAGE_END


REMOTE_MESSAGE = <<MESSAGE_END
From: drsmith@nhin.sunnyfamilypractice.example.org
To: remote@nhin.prettyvalleycare.example.org
Date: Thu, 08 Apr 2010 20:53:17 -0400
Message-ID: <db00ed94-951b-4d47-8e86-585b31fe01be@nhin.sunnyfamilypractice.example.org>
MIME-Version: 1.0
Content-Type: multipart/mixed; boundary="8837833223134.12.9837473322"

This text is traditionally ignored but can
help non-MIME compliant readers provide
information.
--8837833223134.12.9837473322
Content-Type: text/plain

This message is being sent to a remote HISP.

--8837833223134.12.9837473322--

MESSAGE_END
