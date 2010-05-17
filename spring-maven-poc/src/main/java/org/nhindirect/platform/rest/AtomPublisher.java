package org.nhindirect.platform.rest;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;

/**
 * Simple publisher to convert a message list to an Atom feed. 
 */
public class AtomPublisher {
    
    private static FastDateFormat FORMATTER = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT;

    public static String createFeed(String baseUri, HealthAddress address, List<Message> messages) {        
        
        StringBuilder fb = new StringBuilder();

        fb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        
        fb.append("<feed xmlns=\"http://www.w3.org/2005/Atom\">\n");
        
        fb.append("    <title>messages for ");
        fb.append(address.toEmailAddress());
        fb.append("</title>\n");

        fb.append("    <link href=\"");
        fb.append(baseUri);
        fb.append("\" rel=\"self\" />\n");
        
        fb.append("    <updated>");
        fb.append(FORMATTER.format(new Date()));
        fb.append("</updated>\n");
        
        for (Message message : messages) {
            fb.append("    <entry>\n");
            
            fb.append("        <title>");
            fb.append("message: ").append(message.getMessageId());
            fb.append("</title>\n");
            
            fb.append("        <link href=\"");
            fb.append(baseUri).append("/").append(message.getMessageId());
            fb.append("\"/>\n");
            
            if (message.getFrom() != null) {
                fb.append("        <author>\n");
                fb.append("            <name>").append(message.getFrom().toEmailAddress()).append("</name>\n");
                fb.append("            <email>").append(message.getFrom().toEmailAddress()).append("</email>\n");
                fb.append("        </author>\n");
            }
            
            if (message.getTimestamp() != null) {
                fb.append("        <updated>");
                fb.append(FORMATTER.format(message.getTimestamp()));
                fb.append("</updated>\n");
            }
         
            fb.append("    </entry>\n");
        }

        fb.append("</feed>");    

        
/*
<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
    <title>messages for drsmith@nhin.sunnyfamilypractice.example.org</title>
    <link href="http://localhost:8080/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/messages" rel="self" />
??    <id>urn:uuid:60a76c80-d399-11d9-b91C-0003939e0af6</id>
    <updated>2003-12-13T18:30:02Z</updated>
    <entry>
        <title>message: 1225c695-cfb8-4ebb-aaaa-80da344efa6a</title>
        <link href="http://localhost:8080/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/messages/1225c695-cfb8-4ebb-aaaa-80da344efa6a" />
        <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>
        <updated>2003-12-13T18:30:02Z</updated>
    </entry>
</feed>
*/
        return fb.toString();
    }
}
