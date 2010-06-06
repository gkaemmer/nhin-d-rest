package org.nhindirect.platform.rest;

import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.nhindirect.platform.HealthAddress;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Person;

public class CertificateAtomView extends AbstractAtomFeedView {

    protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        List<Entry> entries = new ArrayList<Entry>();

        @SuppressWarnings("unchecked")
        List<X509Certificate> certs = (List<X509Certificate>) model.get("certs");
        HealthAddress address = (HealthAddress) model.get("address");

        for (X509Certificate cert : certs) {
            Entry entry = new Entry();

            entry.setId(request.getRequestURL().toString() + "/" + cert.getSerialNumber().toString());
            entry.setTitle("Certificate for " + address.toEmailAddress());
            entry.setAuthors(createAuthorList(address));
            entry.setContents(createContent(cert));
            entry.setSummary(createReadableContent(cert));
            entry.setUpdated(cert.getNotBefore());

            entries.add(entry);
        }

        return entries;
    }

    protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
        super.buildFeedMetadata(model, feed, request);

        HealthAddress address = (HealthAddress) model.get("address");
        feed.setTitle("Certificates for " + address.toEmailAddress());
        feed.setUpdated(new Date());
        feed.setId(request.getRequestURL().toString());
    }

    private ArrayList<Person> createAuthorList(HealthAddress address) {
        Person person = new Person();
        person.setEmail(address.toEmailAddress());
        person.setName(address.toEmailAddress());

        ArrayList<Person> authors = new ArrayList<Person>();
        authors.add(person);
        return authors;
    }

    private ArrayList<Content> createContent(X509Certificate cert) throws Exception {
        Content content = new Content();
        content.setType("application/pkix-cert");

        content.setValue(new String(Base64.encodeBase64(cert.getEncoded())));

        ArrayList<Content> contents = new ArrayList<Content>();
        contents.add(content);

        return contents;
    }

    private Content createReadableContent(X509Certificate cert) {
        Content content = new Content();
        content.setType("text/html");

        StringWriter s = new StringWriter();
        s.append("Subject: " + cert.getSubjectX500Principal().toString());
        s.append("<br/>");
        s.append("Issuer: " + cert.getIssuerX500Principal().toString());

        content.setValue(s.toString());

        return content;
    }
}
