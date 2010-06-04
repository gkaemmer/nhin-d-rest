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
  
  should 'detect if encrypted' do
    m = Message.new(:raw_message => SAMPLE_MESSAGE)
    assert !m.encrypted?
    m = Message.new(:raw_message => ENCRYPTED_MESSAGE)
    assert m.encrypted?
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

ENCRYPTED_MESSAGE = <<MESSAGE_END
Date: Thu, 08 Apr 2010 20:53:17 -0400
From: drsmith@nhin.sunnyfamilypractice.example.org
To: drjones@nhin.happyvalleypractice.example.org
Message-ID: <db00ed94-951b-4d47-8e86-585b31fe01ba@nhin.sunnyfamilypractice.example.org>
Mime-Version: 1.0
Content-Disposition: attachment; filename="smime.p7m"
Content-Type: application/x-pkcs7-mime; smime-type=enveloped-data; name="smime.p7m"
Content-Transfer-Encoding: base64

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
cs02CGo4vOi7hV2zCdM6ryKKnbPRyCPatuM=

MESSAGE_END