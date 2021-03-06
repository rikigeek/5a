package rikigeek.fivea;

import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.MessageNodeAddress;
import rikigeek.fivea.entities.NodeAddress;

/**
 * The role of this class is to answer to CONFIG messages, and to send new
 * CONFIG messages
 * 
 * @author Rikigeek
 *
 */
public class DispatchConfig implements Runnable {
	private static Logger LOGGER = Logger.getLogger(DispatchConfig.class
			.getName());

	private Node node;
	private Message receivedMessage;

	public DispatchConfig(Node node) {
		// Default constructor
		this.node = node;
	}

	/**
	 * Threat the received message, and return the message to send back to the
	 * send (the response)
	 * 
	 * @param message
	 *            the message we received and that we must work on
	 * @return a message to send back to the sender
	 */
	public Message receivesMessage(Message receivedMessage) {
		this.receivedMessage = receivedMessage;
		switch (receivedMessage.getVerb()) {
		case GETNODELIST:
			return Message
					.okGetNodeList(
							receivedMessage.getId(),
							node.getAddress(),
							node.getDomainNodeList().toArray(
									new MessageNodeAddress[0]));
		case STOP:
			node.stopNode();
			return Message.ok(node.getAddress(), receivedMessage);
		default:
			return Message.noOk(node.getAddress(), receivedMessage);
		}

	}

	/**
	 * Request the contact to send to us its local list of nodeAddress
	 * 
	 * @param contact
	 * @return the list
	 */
	public MessageNodeAddress[] requestNodeList(NodeAddress contact) {
		LOGGER.info("Request the nodeAddress list of " + contact);

		LOGGER.fine("Opening the speaker");
		Speaker speaker = new Speaker(contact);
		// We build the message we'll send to the remote contact
		Message msg = Message.getNodeList(node.getAddress());
		// Sending the message and waiting for the answer
		Message response = speaker.sendMessage(msg);
		if (response != null) {
			if (response.isOk()) {
				// this information is valid only if response is not empty
				// object and if the response is OK
				return response.getNodeList();
			} else {
				// The response is not a positive answer
				LOGGER.info("Response is not OK : " + response);
			}
		} else {
			// Haven't received anything
			LOGGER.info("No response received");
		}
		return null;
	}
	
	public boolean requestStopNode(NodeAddress contact) {
		LOGGER.info("Request to stop the remote node " + contact);
		
		LOGGER.fine("opening the speaker");
		Speaker speaker = new Speaker(contact);
		// We build the message we'll send to the remote contact
		Message msg = Message.doStop(node.getAddress());
		// Sending the message and waiting for the answer
		Message response = speaker.sendMessage(msg);
		if (response != null) {
			if (response.isOk()) {
				// this information is valid only if response is not empty
				// object and if the response is OK
				return true;
			} else {
				// The response is not a positive answer
				LOGGER.info("Response is not OK : " + response);
			}
		} else {
			// Haven't received anything
			LOGGER.info("No response received");
		}
		return false;
		// 
	}

	@Override
	public void run() {
		Thread.currentThread().setName(node.getAddress().getTCPPort() + "-DispachConfig(" + Thread.currentThread().getId() + ")");
		LOGGER.finest("DispatchConfig Thread started for receivedMessage #"
				+ receivedMessage.getId());

		LOGGER.finest("DispatchConfig Thread is stopping");
	}

}
