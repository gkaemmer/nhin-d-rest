atom_feed(:url => certs_url(params[:domain], params[:endpoint],:atom)) do |feed|
    feed.title("Certificates for #{params[:endpoint]}@#{params[:domain]}")
    feed.updated(Time.now.utc)

    for cert in @certs
      feed.entry(cert, :url => certs_url(params[:domain], params[:endpoint])) do |entry|
        entry.author do |author|
          author.name(params[:endpoint] + "@" + params[:domain])
          author.email(params[:endpoint] + "@" + params[:domain])
        end
        entry.content(cert.cert, :type => 'application/pkix-cert')
      end
    end
  end
