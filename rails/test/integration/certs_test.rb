require 'test_helper'

class CertsTest < ActionController::IntegrationTest
  fixtures :all
  
  def setup
    @drj_root = '/nhin/v1/nhin.happyvalleypractice.example.org/drjones/certs'
    @default_cert = <<END
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
  end
  
  should 'be able to route to certificate index' do
    assert_routing @drj_root, {:controller => 'certs', :action => 'index', :domain => 'nhin.happyvalleypractice.example.org', :endpoint => 'drjones'}
  end
  
  should 'be have an Atom representation of certificates' do
    get @drj_root, nil, {:accept => 'application/atom+xml'}
    assert_response :success
    assert_equal response.content_type, "application/atom+xml"
  end
  
  should 'have default HISP cert for Dr Jones' do
    get @drj_root, nil, {:accept => 'application/atom+xml'}
    feed = Feedzirra::Feed.parse(response.body)
    entry = feed.entries.first
    assert_equal @default_cert, OpenSSL::X509::Certificate.new(Base64.decode64(entry.content)).to_pem
  end
  
end