require 'test_helper'

class MessageTest < ActiveSupport::TestCase
  # Replace this with your real tests.
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

