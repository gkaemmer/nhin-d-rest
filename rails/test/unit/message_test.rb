require 'test_helper'

class MessageTest < ActiveSupport::TestCase
  # Replace this with your real tests.
  should_validate_presence_of :uuid, :raw_message
  should_have_one :status
end
