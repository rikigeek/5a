package rikigeek.fivea;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.MessageNodeAddress;
import rikigeek.fivea.entities.NodeAddress;

public class Node {

	private static Logger LOGGER = Logger.getLogger(Node.class.getName());

	public static Logger Logger() {
		return LOGGER;
	}

	// list of other nodes addresses
	private NodeAddress myAddress;
	private ConcurrentSkipListSet<NodeAddress> domainNodeList;

	private String localName;

	/**
	 * Listener that will "listen" to all other nodes
	 */
	private Listener listener;

	/**
	 * The domain the node is member of
	 */
	private String domain;

	public String getDomain() {
		return domain;
	}

	public ConcurrentSkipListSet<NodeAddress> getDomainNodeList() {
		return this.domainNodeList;
	}

	private Node(int port) {
		LOGGER.info("Creation of a node on port " + port);
		// Creation of a node.
		// A node is checking the domain change

		// Initialization of the node
		// find the address
		localName = findLocalName();
		if (localName == null) {
			LOGGER.severe("No local name found for the node. Please provide one manually");
			return;
		}
		LOGGER.info("Current node created as " + localName);

		// Build the listener
		LOGGER.info("Starting the listener");
		listener = new Listener(this, port);
		new Thread(listener).start();

		// Update the NodeAddress
		myAddress = new NodeAddress(localName, listener.getPort());
		
		domainNodeList = new ConcurrentSkipListSet<NodeAddress>();
		domainNodeList.add(myAddress);

	}

	public Node(String domainName, int port) {
		this(port);
		// And now, we create a new domain
		LOGGER.info("Creation of new domain : " + domainName);
		this.domain = domainName;
	}

	public Node(MessageNodeAddress contact, int port) {
		this(port);

		newNode(contact);

	}

	/**
	 * Try to find the local host name of the node
	 * 
	 * @return
	 */
	private String findLocalName() {
		String address;
		try {
			address = java.net.InetAddress.getLocalHost().getHostName();
			return address;
		} catch (UnknownHostException e) {
			// Doing a non portable way :
			address = System.getenv("HOSTNAME");
			if (address == null) {
				// running the hostname system Command
				Runtime run = Runtime.getRuntime();
				Process proc;
				try {
					proc = run.exec("hostname");
					BufferedInputStream in = new BufferedInputStream(
							proc.getInputStream());
					byte b[] = new byte[256];
					in.read(b);
					return new String(b);
				} catch (IOException e1) {
					return null;
				}

			} else {
				return address;
			}
		}

	}

	/**
	 * Address of the node
	 * 
	 * @return
	 */
	public MessageNodeAddress getAddress() {
		return myAddress;
	}

	/**
	 * Insertion into an existing domain, using a node address
	 * 
	 * @param contact
	 *            the nodeAddress of an existing node
	 * @return
	 */
	public boolean newNode(MessageNodeAddress contact) {
		//
		LOGGER.info("Asking " + contact + " to enter the domain");
		// And now, we try to connect to the domain
		// At creation, we need to find a place in the domain.
		Message msg = Message.addNode(this.getAddress());

		// Send the message and wait for the result
		Speaker speaker = new Speaker(contact);
		if (speaker.isConnected()) {
			Message response = speaker.sendMessage(msg);
			// The response contains the node we should connect to
			speaker.close();
			if (response != null && response.isOk() && response.getNodeList() != null) {
				MessageNodeAddress[] list = response.getNodeList();
				if (list != null) {
					for (int i = 0; i < list.length; i++) {
						this.domainNodeList.add(new NodeAddress(list[i]));
					}
				}
				LOGGER.info("we are in the domain " + response.getDomainName() + " of " + domainNodeList.size()
						+ " nodes");
				this.domain = response.getDomainName();
				return true;
			} else {
				LOGGER.severe(contact + " could not get an answer from my contact...");

			}
		} else {
			LOGGER.warning("Could not connect to " + contact);
		}
		return false;
	}

}
