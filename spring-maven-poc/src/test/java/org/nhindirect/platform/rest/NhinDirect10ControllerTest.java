package org.nhindirect.platform.rest;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

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
		
		String atom = controller.getMessages(request, "domain", "endpoint");
		
		assertNotNull(atom);
		assertTrue(atom.length() > 0);
		assertThat(atom, containsString("feed"));
		// TODO Should inspect the feed to ensure we're not getting a message.
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
		when(request.getRequestURL()).thenReturn(new StringBuffer("requestURL"));
		
		String atom = controller.getMessages(request, "domain", "endpoint");
		
		assertNotNull(atom);
		assertTrue(atom.length() > 0);
		assertThat(atom,containsString("feed"));
		// TODO Should inspect the feed to ensure we are getting a message.
		// XXX Don't see the message in the feed.
	}
	
	/**
	 * Ensure that when a new message is created that a recognizable UUID is returned.
	 * @throws Exception
	 */
	@Test
	public void postMessage_validResponseId() throws Exception {
		UUID id = new UUID(1,1);
		Message message = new Message();
		message.setMessageId(id);
		
		when(messageService.createMessage(any(HealthAddress.class), anyString())).thenReturn(message);
		String response = controller.postMessage("domain", "endpoint", "some message");
		
		assertNotNull(response);
		assertTrue(UUID.fromString(response).compareTo(id) == 0);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void getMessage_validData() throws Exception {
		Message message = new Message();
		message.setData("some data".getBytes());
		
		when(messageService.getMessage(any(HealthAddress.class), any(UUID.class))).thenReturn(message);
		
		String response = controller.getMessage("domain", "endpoint", new UUID(0,0).toString());
		
		assertNotNull(response);
		assertEquals(response, "some data");
	}
	
	/**
	 * Ensure that invalid id's are rejected.
	 * @throws Exception
	 */
	@Test
	public void getMessage_invalidRequestId() throws Exception {
		try {
			controller.getMessage("domain", "endpoint", "not a uuid");
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
			controller.getMessageStatus("domain", "endpoint", "not a uuid");
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
			controller.setMessageStatus("domain", "endpoint", "not a uuid", MessageStatus.ACK.name());
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
			controller.setMessageStatus("domain", "endpoint", new UUID(0,0).toString(), "not a status");
			fail();
		} catch ( IllegalArgumentException e ) {
			// Nothing to see here...
		} catch ( Exception e ) {
			fail();
		}
	}
}
