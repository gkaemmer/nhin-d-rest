require 'test_helper'

class MessagesControllerTest < ActionController::TestCase

  should 'forbid access to GET /messages for hisp role' do
     @request.env['HTTP_SSL_CLIENT_VERIFY'] = 'SUCCESS'
     @request.accept = 'application/atom+xml'
     get :index, {:domain => 'nhin.happyvalleypractice.example.org', :endpoint => 'drjones' }
     assert_response :forbidden
  end
   
  should 'allow access to POST /messages for hisp role' do
    @request.env['HTTP_SSL_CLIENT_VERIFY'] = 'SUCCESS'
    @request.headers['Content-Type'] = 'message/rfc822'
    @request.accept = 'message/rfc822'
    post :create, {:domain => 'nhin.happyvalleypractice.example.org', :endpoint => 'drjones', :message => {:raw_message =>  SAMPLE_MESSAGE} }
    assert_response :created
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
