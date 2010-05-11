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
end
