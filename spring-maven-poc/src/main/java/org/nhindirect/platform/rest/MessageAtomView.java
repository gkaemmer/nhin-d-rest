package org.nhindirect.platform.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

public class MessageAtomView extends AbstractAtomFeedView {

    protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        List<Entry> entries = new ArrayList<Entry>();

        @SuppressWarnings("unchecked")
        List<Message> certs = (List<Message>) model.get("messages");

        for (Message message : certs) {
            Entry entry = new Entry();

            String linkHref = request.getRequestURL().toString() + "/" + message.getMessageId().toString();

            entry.setTitle("message: " + message.getMessageId());
            entry.setAuthors(createAuthorList(message.getFrom()));
            entry.setAlternateLinks(createLinkList(linkHref));
            entry.setUpdated(message.getTimestamp());
            entry.setId(linkHref);
            entry.setContents(generateContents(message));

            entries.add(entry);

        }

        return entries;
    }

    private List<Content> generateContents(Message message) {

        Content content = new Content();
        content.setType("text/html");

        content.setValue(message.getSubject());

        ArrayList<Content> contents = new ArrayList<Content>();
        contents.add(content);

        return contents;
    }

    protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
        HealthAddress address = (HealthAddress) model.get("address");

        feed.setTitle("Messages for " + address.toEmailAddress());
        String linkHref = request.getRequestURL().toString();
        feed.setAlternateLinks(createLinkList(linkHref));
        feed.setUpdated(new Date());
        feed.setId(request.getRequestURL().toString());
    }

    private ArrayList<Link> createLinkList(String linkHref) {
        Link link = new Link();
        link.setHref(linkHref);
        link.setRel(null);

        ArrayList<Link> links = new ArrayList<Link>();
        links.add(link);
        return links;
    }

    private ArrayList<Person> createAuthorList(HealthAddress address) {
        Person person = new Person();
        person.setEmail(address.toEmailAddress());
        person.setName(address.toEmailAddress());

        ArrayList<Person> authors = new ArrayList<Person>();
        authors.add(person);
        return authors;
    }
}
