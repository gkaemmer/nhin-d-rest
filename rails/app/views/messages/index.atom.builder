atom_feed(:url => messages_url(params[:domain], params[:endpoint],:atom)) do |feed|
    feed.title("Messages for #{params[:endpoint]}@#{params[:domain]}")
    feed.updated(Time.now.utc)

    for message in @messages
      feed.entry(message, :url => message_url(params[:domain], params[:endpoint], message)) do |entry|
        entry.title("Message: #{message.uuid}")
        entry.author do |author|
          author.name(message.to_endpoint + "@" + message.to_domain)
          author.email(message.to_endpoint + "@" + message.to_domain)
        end
      end
    end
  end
