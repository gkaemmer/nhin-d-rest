atom_feed(:url => certs_url(params[:domain], params[:endpoint],:atom)) do |feed|
    feed.title("Certificates for #{params[:endpoint]}@#{params[:domain]}")
    feed.updated(Time.now.utc)

    for cert in @certs
      feed.entry(cert, {:url => certs_url(params[:domain], params[:endpoint]),
          :id => "tag:#{params[:endpoint]},#{Date.today.year}:cert/#{cert.id}"}) do |entry|
        entry.title("Certificate for #{params[:endpoint]}@#{params[:domain]}")
        entry.summary("Certificate for #{params[:endpoint]}@#{params[:domain]}")
        entry.author do |author|
          author.name(params[:endpoint] + "@" + params[:domain])
          author.email(params[:endpoint] + "@" + params[:domain])
        end
        entry.content(Base64.encode64(cert.cert), :type => 'application/pkix-cert')
      end
    end
  end
