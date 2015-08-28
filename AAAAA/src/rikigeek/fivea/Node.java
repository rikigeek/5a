package rikigeek.fivea;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import rikigeek.fivea.Message.Subject;
import rikigeek.fivea.Message.Verb;

public class Node {
	
	private static Logger LOGGER = Logger.getLogger(Node.class.getName());
	public static Logger Logger() {
		return LOGGER;
	}
	
	// list of other nodes addresses
	private NodeAddress[] brothers;
	private NodeAddress[] childrens;
	private NodeAddress father;
	private NodeAddress grandFather;
	private NodeAddress myAddress;
	
	// flag to indicate election mode 
	private boolean election = false;
	// Flag to indicate that the father is lost
	private boolean fatherLost = false;

	private String localName;
	
	/**
	 * Listener that will "listen" to all other nodes
	 */
	private Listener listener;
	
	/**
	 * The domain the node is member of
	 */
	private String domain;
	
	private Node(int port) {
		LOGGER.info("Creation of a node on port " + port);
		// Creation of a node.
		// A node is checking the domain change 

		// Initialisation of the node
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
		myAddress = new NodeAddress(localName,  listener.getPort());
		
	}
	public Node(String domainName, int port) {
		this(port);
		// And now, we create a new domain
		LOGGER.info("Creation of new domain : " + domainName);
		this.domain = domainName;
	}
	
	public Node(NodeAddress contact, int port) {
		this(port);
		
		newNode(contact);
		
	}
	
	/**
	 * Try to find the local host name of the node
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
					BufferedInputStream in = new BufferedInputStream(proc.getInputStream());
					byte b[] = new byte[256];
					in.read(b);
					return new String(b);
				} catch (IOException e1) {
					return null;
				}
				
			} 
			else {
				return address;
			}
		}
		
	}
	
	/**
	 * Address of the node
	 * @return
	 */
	public NodeAddress getAddress() {
		return myAddress;
	}
	
	/**
	 * Insertion into an existing domain, using a node address
	 * @param contact the nodeAddress of an existing node
	 * @return 
	 */
	public boolean newNode(NodeAddress contact) {
		// 
		LOGGER.info("Asking " + contact + " for a new father");
		// And now, we try to connect to the domain
		// At creation, we need to find a place in the domain.
		Message msg = Message.newNode(this.getAddress(), contact);
		
		// Send the message and wait for the result
		Speaker speaker = new Speaker(contact);
		if (speaker.isConnected()) {
			Message response = speaker.sendMessage(msg);
			// The response contains the node we should connect to
			speaker.close();
			if (response.isOk() && response.father != null) {
				LOGGER.info("we found a new father... : " + response.father);
				return connect(response.father);
			}
			else {
				LOGGER.severe(contact + " could not find a father for us");

			}
		}
		else {
			LOGGER.warning("Could not connect to " + contact);
		}
		return false;
	}
	
	public boolean connect(NodeAddress father) {
		LOGGER.info("Trying to connect to a new father : " + father);
		Speaker speaker = new Speaker(father);
		if (speaker.isConnected()) {
			Message msg = Message.connect(this.getAddress(),  father);
			Message response = speaker.sendMessage(msg);
			speaker.close();
			if (response.isOk()) {
				// Message is ok. Retrieving father, grandFather, and brothers;
				this.father = response.father;
				this.grandFather = response.grandFather;
				this.brothers = response.brothers;
				LOGGER.info("Node is connected...");
				return true;
			}
			else {
				LOGGER.severe("father " + father + " refused us :(");
			}
			
		}
		else {
			LOGGER.warning("Could not connect to father " + father);
		}
		return false;
		
	}
	/**
	 * Set the election mode
	 * @param b
	 */
	public void setElectionFlag(boolean b) {
		this.election = b;
		
	}
	/**
	 * Defines if the father is lost
	 * @param b
	 */
	public void setFatherLostFlag(boolean b) {
		this.fatherLost = b;
	}
}
