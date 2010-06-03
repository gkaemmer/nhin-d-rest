package org.nhin.test;

import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.mailet.GenericMatcher;
import org.apache.mailet.Mail;
import org.apache.mailet.MatcherConfig;

public class NhinMatcher extends GenericMatcher {

	public void init(MatcherConfig config) throws MessagingException
	  {
	    super.init(config);
	  }
	  
	  public Collection match(Mail mail) throws MessagingException
	  {
		  MimeMessage msg = mail.getMessage();
		  String header = msg.getHeader("NHIN Internal", ",");
		  if (header != null && header.contains("true")) {
			  log("Found NHIN header: " + header);
			  msg.removeHeader("NHIN Internal");
			  return null;
		  }
		  
		  log("Did NOT find NHIN header");
		  return mail.getRecipients();
	  }
}
