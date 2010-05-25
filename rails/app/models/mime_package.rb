require 'mail'

class Mail::Message
  
  @@CONTENT_HEADERS = {
    :content_type => 'Content-Type',
    :content_description => 'Content-Description',
    :content_disposition => 'Content-Disposition',
    :content_id => 'Content-ID',
    :content_location => 'Content-Location',
    :content_transfer_encoding => 'Content-Transfer-Encoding'
  }
  
  def non_mime_headers
    fields = self.header_fields
    non_mime_headers = fields.reject do |f|
      sym = f.name.downcase.gsub('-', '_').intern
      @@CONTENT_HEADERS.has_key?(sym)
    end
    non_mime_headers.inject('') { |str, f| str + "#{f.name}: #{f.value}\r\n"}
  end
  
  def mime_package
    mime_headers = @@CONTENT_HEADERS.keys.inject('') do |str, header|
      value = self.send header
      if value then
        str + (@@CONTENT_HEADERS[header] + ': ' + value + "\r\n")
      else
        str
      end
    end
    
    mime_headers + "\n\n" + self.body.raw_source
  end
  
  def mime_package=(string)
    m = Mail.new(string)
    self.body = m.body if m.body
    @@CONTENT_HEADERS.keys.each do |header|
      value = m.send header
      (self.send header, value) if value
    end
  end
    
end
    