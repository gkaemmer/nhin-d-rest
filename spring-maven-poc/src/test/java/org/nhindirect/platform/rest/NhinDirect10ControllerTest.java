package org.nhindirect.platform.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageService;
import org.nhindirect.platform.MessageStatus;

@RunWith(MockitoJUnitRunner.class)
public class NhinDirect10ControllerTest {

	// Class tested
	private NhinDirect10Controller controller;
	
	// Direct dependency
	@Mock private MessageService messageService;
	
	// Transient dependencies
	@Mock private HttpServletRequest request;
	@Mock private HttpServletResponse response;
	@Mock private PrintWriter writer;
	@Mock private Message message;
	List<Message> messageList;
	HealthAddress address = new HealthAddress("domain", "endpoint");
	
	@BeforeClass
	public static void initialize() {
	}
	
	@Before
	public void setUp() {
		// Doesn't need to be re-created per test.
		controller = new NhinDirect10Controller();
		controller.messageService = messageService;
		
		// Re-create per test so that we can test 0, 1, many massage scenarios.
		messageList = new LinkedList<Message>();
	}
	
	/**
	 * Ensure that if the MessageService reports no new messages, we retrieve an empty feed.
	 * @throws Exception
	 */
	@Test
	public void getMessages_noNewMessages() throws Exception {
		when(messageService.getNewMessages(address)).thenReturn(messageList);
		when(request.getRequestURL()).thenReturn(new StringBuffer("requestURL"));
		StringWriter out = new StringWriter();  
		when(response.getWriter()).thenReturn(new PrintWriter(out));
		
		controller.getMessages(request, response, "domain", "endpoint");
		
		String atom = out.toString();
		
		assertNotNull(atom);
		assertTrue(atom.length() > 0);
		assertThat(atom, containsString("feed"));
		assertTrue(atom.indexOf("entry") == -1);
	}
	/**
	 * Ensure that if the MessageService reports one new message, we see the message in the feed.
	 * @throws Exception
	 */
	@Test
	public void getMessages_oneNewMessage() throws Exception {
		Message message = new Message();
		message.setData("some data".getBytes());
		
		messageList.add(message);
		when(messageService.getNewMessages(address)).thenReturn(messageList);

		StringWriter out = new StringWriter();  
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        when(request.getRequestURL()).thenReturn(new StringBuffer("requestURL"));
		
		controller.getMessages(request, response, "domain", "endpoint");
		
		String atom = out.toString();
		
		assertNotNull(atom);
		assertTrue(atom.length() > 0);
		assertThat(atom, containsString("feed"));
		assertThat(atom, containsString("entry"));
	}
	
	/**
	 * Ensure that when a new message is created that a recognizable UUID is returned.
	 * @throws Exception
	 */
	@Test
	public void postMessage_locationSet() throws Exception {
		UUID id = new UUID(1,1);
		
		when(message.getMessageId()).thenReturn(id);
		when(messageService.handleMessage(any(HealthAddress.class), anyString())).thenReturn(message);
		when(request.getRequestURL()).thenReturn(new StringBuffer("requestUrl"));
		
		controller.postMessage(request, response, "domain", "endpoint", "some message");
		
		verify(response).setHeader("Location", "requestUrl/" + id);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void getMessage_messageInResponse() throws Exception {
		String data = "some data";
		
		when(message.getData()).thenReturn(data.getBytes());
		when(messageService.getMessage(any(HealthAddress.class), any(UUID.class))).thenReturn(message);
		when(response.getWriter()).thenReturn(writer);
		
		controller.getMessage(response, "domain", "endpoint", new UUID(0,0).toString());
		
		verify(response).setContentType("message/rfc822");
		verify(writer).write(data, 0, 9);
		verify(writer).close();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void getMessage_noMessage404() throws Exception {
		when(messageService.getMessage(any(HealthAddress.class), any(UUID.class))).thenReturn(null);
		when(response.getWriter()).thenReturn(writer);
		
		controller.getMessage(response, "domain", "endpoint", new UUID(0,0).toString());
		
		verify(response).setStatus(404);
		verify(writer).write(anyString(), anyInt(), anyInt());
		verify(writer).close();
	}
	
	/**
	 * Ensure that invalid id's are rejected.
	 * @throws Exception
	 */
	@Test
	public void getMessage_invalidRequestId() throws Exception {
		try {
			controller.getMessage(response, "domain", "endpoint", "not a uuid");
			fail();
		} catch ( IllegalArgumentException e ) {
			// Nothing to see here...
		} catch ( Exception e ) {
			fail();
		}
	}
	
	/**
	 * Ensure that invalid id's are rejected.
	 * @throws Exception
	 */
	@Test
	public void getMessageStatus_invalidRequestId() {
		try {
			controller.getMessageStatus(response, "domain", "endpoint", "not a uuid");
			fail();
		} catch ( IllegalArgumentException e ) {
			// Nothing to see here...
		} catch ( Exception e ) {
			fail();
		}
	}

	/**
	 * Ensure that invalid id's are rejected.
	 * @throws Exception
	 */
	@Test
	public void setMessageStatus_invalidRequestId() {
		try {
			controller.setMessageStatus(response, "domain", "endpoint", "not a uuid", MessageStatus.ACK.name());
			fail();
		} catch ( IllegalArgumentException e ) {
			// Nothing to see here...
		} catch ( Exception e ) {
			fail();
		}
	}
	
	/**
	 * Ensure that invalid statuses are rejected.
	 * @throws Exception
	 */
	@Test
	public void setMessageStatus_invalidStatus() {
		try {
			controller.setMessageStatus(response, "domain", "endpoint", new UUID(0,0).toString(), "not a status");
			fail();
		} catch ( IllegalArgumentException e ) {
			// Nothing to see here...
		} catch ( Exception e ) {
			fail();
		}
	}
}
