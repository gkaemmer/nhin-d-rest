require 'test_helper'

class MessageTest < ActiveSupport::TestCase
  should_validate_presence_of :raw_message
  should_have_one :status
  
  should 'not be valid without all required headers' do
    m = Message.new
    m.raw_message = 'foo bar baz quux'
    assert !m.valid?
  end
  
  should 'be owned by sending or receiving address' do
    m = Message.new
    m.raw_message = SAMPLE_MESSAGE
    assert m.owned_by 'drsmith@nhin.sunnyfamilypractice.example.org'
    assert m.owned_by 'drjones@nhin.happyvalleypractice.example.org'
    assert !(m.owned_by 'foo@bar.baz.quux')
  end
  
  should 'be signable and verifiable' do
    m = Message.new(:raw_message => SAMPLE_MESSAGE)
    RemoteHISP.any_instance.stubs(:certs).returns [OpenSSL::X509::Certificate.new(TO_CRT)]
    t = m.signed_and_encrypted
    m2 = Message.decrypt(t)
    parsed = m2.parsed_message
    assert_not_nil parsed
    assert_not_nil parsed.from
    assert_not_nil parsed.to
    assert_equal 'drsmith@nhin.sunnyfamilypractice.example.org', parsed.from[0]
    assert_equal 'drjones@nhin.happyvalleypractice.example.org', parsed.to[0]
    assert_equal 'This is the third document I am sending you', parsed.parts[0].parts[0].body.raw_source
    assert m2.signature_verified?
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

TO_CRT = <<TO_CRT
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
TO_CRT
