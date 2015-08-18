package rikigeek.aaaaa;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import rikigeek.aaaaa.Message.Subject;
import rikigeek.aaaaa.Message.Verb;

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
		
		LOGGER.info("Trying to connect to " + contact);
		// And now, we try to connect to the domain
		// At creation, we need to find a place in the domain.
		Message msg = new Message();
		msg.subject = Subject.CONNECTION;
		msg.sourceNodeAddress = getAddress();
		msg.verb = Verb.INSERT;
		msg.question = true;
		
		
		// Send the message and wait for the result
		Speaker speaker = new Speaker(contact);
		if (speaker.isConnected()) {
			Message response = speaker.SendMessage(msg);
			// The response contains the node we should connect to

			speaker.Close();
			if (response!= null && response.father != null) {
				speaker.Open(response.father);
				msg.data = new String("CONNECT").getBytes();
				msg.sourceNodeAddress = getAddress();
			}
		}
		speaker.Close();
		LOGGER.info("Node is connected...");
		
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
	 * Connection to a domain, using a node address
	 * @return 
	 */
	public boolean connect(String node) {
		// 
		return false;
	}
	
	
}
