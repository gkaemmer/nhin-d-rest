require 'test_helper'

class RemoteHISPTest < Test::Unit::TestCase
  
  context 'A RemoteHISP' do
    
    setup do
      @hisp = RemoteHISP.new('localhost', 'nhin.happyvalleypractice.example.org', 'drjones', 'drjones_secret', '3000')
    end
        
    should 'be able to configure the messages address' do
      @hisp.message_box = 'nhin.sunnyfamilypractice.example.org/drsmith'
      assert_equal (@hisp.version_path + '/nhin.sunnyfamilypractice.example.org/drsmith/messages'), @hisp.messages_path
    end
    
    should 'have messages that returns Atom feed' do
      @hisp.message_box = 'nhin.happyvalleypractice.example.org/drjones'
      loc = @hisp.create_message(SAMPLE_MESSAGE)
      mid = loc.split('/').last
      @hisp.message_box = 'nhin.happyvalleypractice.example.org/drjones'
      atom_index = @hisp.messages
      feed = Feedzirra::Feed.parse(atom_index)
      assert feed.entries.detect { |entry| URI::split(entry.url)[5] == @hisp.messages_path + '/' + mid }
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
