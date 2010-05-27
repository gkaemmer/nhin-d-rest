require 'test_helper'
require 'openssl'

class RemoteHISPTest < ActionController::IntegrationTest
  fixtures :all
  
  context 'A RemoteHISP' do
    
    setup do
      @hisp = RemoteHISP.new('localhost', 'nhin.happyvalleypractice.example.org', 'drjones', 'drjones_secret', '3000')
    end
        
    should 'be able to configure the messages address' do
      @hisp.message_box = 'nhin.sunnyfamilypractice.example.org/drsmith'
      assert_equal (@hisp.version_path + '/nhin.sunnyfamilypractice.example.org/drsmith/messages'), @hisp.messages_path
      assert_equal (@hisp.version_path + '/nhin.sunnyfamilypractice.example.org/drsmith/certs'), @hisp.certs_path
    end
    
    should 'have messages method that returns an array of message URIs' do
      @hisp.message_box = 'nhin.happyvalleypractice.example.org/drjones'
      loc = @hisp.create_message(SAMPLE_MESSAGE)
      mid = loc.split('/').last
      @hisp.message_box = 'nhin.happyvalleypractice.example.org/drjones'
      message_locs = @hisp.messages
      assert message_locs.detect { |loc| loc == @hisp.messages_path + '/' + mid }
    end
    
    should 'be able to create a new message and then view it' do
      @hisp.message_box = 'nhin.happyvalleypractice.example.org/drjones'
      loc = @hisp.create_message(SAMPLE_MESSAGE)
      mid = loc.split('/').last
      message = @hisp.message(mid)
      assert_equal SAMPLE_MESSAGE, message
    end
    
    should 'be able to create a message, set status, and retrieve the updated status' do
      @hisp.message_box = 'nhin.happyvalleypractice.example.org/drjones'
      loc = @hisp.create_message(SAMPLE_MESSAGE)
      mid = loc.split('/').last
      message = @hisp.message(mid)
      assert_equal 'NEW', @hisp.status(mid)
      @hisp.update_status(mid, 'ACK')
      assert_equal 'ACK', @hisp.status(mid)
    end
    
    should 'have a certs method that returns an array of X509 certs' do
      @hisp.message_box = 'nhin.happyvalleypractice.example.org/drjones'
      certs = @hisp.certs
      example_cert = <<END
-----BEGIN CERTIFICATE-----
MIIChDCCAe0CAQEwDQYJKoZIhvcNAQEFBQAwgYMxCzAJBgNVBAYTAlVTMQswCQYD
VQQIEwJDQTEQMA4GA1UEBxMHT2FrbGFuZDEUMBIGA1UEChMLTkhJTiBEaXJlY3Qx
FDASBgNVBAMTC05ISU4gRGlyZWN0MSkwJwYJKoZIhvcNAQkBFhphcmllbi5tYWxl
Y0BuaGluZGlyZWN0Lm9yZzAeFw0xMDA1MjAxNzM0MjdaFw0yMDA1MTcxNzM0Mjda
MIGQMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExEDAOBgNVBAcTB09BS0xBTkQx
FjAUBgNVBAoTDUxpdHRsZSBISUUgQ28xHzAdBgNVBAMTFkxpdHRsZSBISUUgQ28g
b3BlcmF0b3IxKTAnBgkqhkiG9w0BCQEWGmFyaWVuLm1hbGVjQG5oaW5kaXJlY3Qu
b3JnMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQClEcq+PnXMMfTKjEXqn1n7
OxyhTxxsjTHPXJ/Mp/uu2tHcrF5zHHs/uRChEP5XODwYyfXjJM5+5IVgJmKEhmai
sxSPA/bOc4UVcLcyvsPr43f30Ua0WKDn30js4UUr+JqBS70yyfqOxWSmZJJo43u4
2q0+AfQQt4dw8tJyzmgE9wIDAQABMA0GCSqGSIb3DQEBBQUAA4GBACEEhfU0ibFM
73emNPpP5sBZ0CSkX535UhBPViVUV5XVQYJ57d3L0yZQRQrSCOSOWQ9bN2eszVsl
h1D33YmonW1npy8W84AshDGYYp4KjHEeQr+pQfoUm46+e1tOC22KNeJi7YhDs2yq
D7b4mDr6WDtMSuewfapVEJdzsTDTRdWz
-----END CERTIFICATE-----
END
      assert_not_nil certs
      assert_equal 1, certs.length
      assert_equal example_cert, certs[0].to_pem
    end
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
