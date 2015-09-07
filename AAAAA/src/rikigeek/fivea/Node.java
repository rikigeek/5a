package rikigeek.fivea;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.MessageNodeAddress;
import rikigeek.fivea.entities.NodeAddress;
import rikigeek.fivea.storage.Resource;
import rikigeek.fivea.storage.StorageManager;

public class Node {

	private static Logger LOGGER = Logger.getLogger(Node.class.getName());

	public static Logger Logger() {
		return LOGGER;
	}

	// Save the main thread (correspond to the console thread)
	private Thread consoleThread;

	// Save the scheduler thread
	private Thread schedulerThread;
	/**
	 * The thread that listens on the server Socket (the listener Thread)
	 */
	private Thread listenerThread;

	// Flag set to true when node is stopping
	boolean quit = false;

	// Flag set to true when node is connected to the domain
	boolean connected = false;
	// list of other nodes addresses
	private NodeAddress myAddress;
	private ConcurrentSkipListSet<NodeAddress> domainNodeList;

	private String localName;

	/**
	 * Listener that will "listen" to all other nodes
	 */
	Listener listener;

	/**
	 * The domain the node is member of
	 */
	private String domain;

	public String getDomain() {
		return domain;
	}

	/**
	 * Indicate if the program must be stopped
	 * 
	 * @return
	 */
	public boolean isStopped() {
		return quit;
	}

	/**
	 * Indicates if the node is connected to the domain
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}

	public ConcurrentSkipListSet<NodeAddress> getDomainNodeList() {
		return this.domainNodeList;
	}

	private Node(int port, String storagePath) {
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

		// Update the NodeAddress
		myAddress = new NodeAddress(localName, listener.getPort());

		domainNodeList = new ConcurrentSkipListSet<NodeAddress>();
		domainNodeList.add(myAddress);

		LOGGER.fine("Node #" + this.hashCode() + " / domainNodeList #"
				+ domainNodeList.hashCode() + " " + domainNodeList.size()
				+ " elements");
		
		// Storage initialization 
		setStoragePath(storagePath);
		if (!storageManager.isLoaded()) {
			// Storage manager not loaded
			LOGGER.severe("Storage manager failed to load. Aborting process");
			this.stopNode();
			return;
		}
		
		
		// Now the node is fully initialized, we can start the SocketServer (the
		// listener)
		listenerThread = new Thread(listener);
		listenerThread.start();
	}

	public Node(String domainName, int port, String storagePath) {
		this(port, storagePath);
		// And now, we create a new domain
		LOGGER.info("Creation of new domain : " + domainName);
		this.domain = domainName;
		// We are first node of the domain, so we are well connected
		this.connected = true;
		
		// Because it's a new domain, we must check if the root folder is initialized
		Resource rootFolder = storageManager.getRessource("/", "", true);
		if (rootFolder == null) {
			storageManager.createNewRessource("/",  "", true);
		}

		LOGGER.exiting(this.getClass().getCanonicalName(),
				"<Constructor(String, int)>");
	}

	public Node(MessageNodeAddress contact, int port, String storagePath) {
		this(port, storagePath);
		LOGGER.info("Trying to contact existing domain through node : "
				+ contact);

		// If node contacted the contact node, then we are well connected to the
		// domain
		connected = newNode(contact);

		LOGGER.exiting(this.getClass().getCanonicalName(),
				"<Constructor(MessageNodeAddress, int)>");
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
			if (response != null && response.isOk()
					&& response.getNodeList() != null) {
				MessageNodeAddress[] list = response.getNodeList();
				if (list != null) {
					for (int i = 0; i < list.length; i++) {
						this.domainNodeList.add(new NodeAddress(list[i]));
					}
				}
				LOGGER.info("we are in the domain " + response.getDomainName()
						+ " of " + domainNodeList.size() + " nodes");
				this.domain = response.getDomainName();
				return true;
			} else {
				LOGGER.severe(contact
						+ " could not get an answer from my contact...");

			}
		} else {
			LOGGER.warning("Could not connect to " + contact);
		}
		return false;
	}

	/**
	 * Stop the node. This method will also stop all threads, and close all connections.
	 * It also send a message to the neighbor to inform them this node is stopping
	 */
	public void stopNode() {
		// Create a dispatcher
		DispatchConnection dconn = new DispatchConnection(this);
		// And ask to stop the node
		dconn.stopNode();
		// and now we can really stop the node
		quit = true;
		try {
			listener.stop();
		} catch (IOException e) {
			LOGGER.throwing(this.getClass().getCanonicalName(), "stopNode()", e);
			LOGGER.severe("Failure while stopping the listener");
		}
		// force all the threads to wakeup and so to exit
		if (listenerThread != null)
			listenerThread.interrupt();
		if (consoleThread != null)
			consoleThread.interrupt();
		if (schedulerThread != null)
			schedulerThread.interrupt();

	}

	/**
	 * Get the root folder owners of the domain. 
	 * @return an array of the nodes that are supposed to own the root folder
	 * This array can contain null values ! 
	 */
	public NodeAddress[] getRootOwners() {
		// The list of all root folder owners
		NodeAddress[] list = new NodeAddress[3];
		int i = 0;
		// Get the list of all nodes of the domain
		ConcurrentSkipListSet<NodeAddress> nodeList = getDomainNodeList();
		NodeAddress node = nodeList.first();
		// We take the first 3 active nodes.
		while (node != null && i < 3) {
			if (node.isActive()) { // Found 1 active
				list[i] = node;
				i++;
			}
			// Get the next node in the list
			node = nodeList.higher(node);
		}
		return list;
	}
	/**
	 * Find the active neighbor next in the list
	 * @return
	 */
	public NodeAddress getNextNeighbor() {
		// Get my address
		NodeAddress me = new NodeAddress(getAddress());
		// Get the list of all nodes of the domain
		ConcurrentSkipListSet<NodeAddress> nodeList = getDomainNodeList();

		// Get the next node
		NodeAddress next = nodeList.higher(me);
		// Loop to the next if it's not active
		// and break the loop when there are no higher node
		while (next != null && !next.isActive()) {
			next = nodeList.higher(next);
		}

		// next is null when there were no higher node
		if (next == null) {
			// get the first node (lowest)
			next = nodeList.first();
			// Break the loop when we find an active node
			// or when we find ourself (then it means we looped through all the list, and no active node has been found
			while (!next.equals(me) && !next.isActive()) {
				next = nodeList.higher(next);
			}
			// if no active node has been found, we return null value
			if (next.equals(me)) {
				next = null;
			}
		}
		
		return next;
		
	}
	
	/**
	 * Find the active neighbor previous in the list
	 * @return
	 */
	public NodeAddress getPreviousNeighbor() {
		// Get my address
		NodeAddress me = new NodeAddress(getAddress());
		// Get the list of all nodes of the domain
		ConcurrentSkipListSet<NodeAddress> nodeList = getDomainNodeList();

		// Get the previous node
		NodeAddress previous = nodeList.lower(me);
		// Loop to the previous if it's not active
		// and break the loop when there are no lower node
		while (previous != null && !previous.isActive()) {
			previous = nodeList.lower(previous);
		}

		// previous is null when there were no lower node
		if (previous == null) {
			// get the last node (highest)
			previous = nodeList.last();
			// Break the loop when we find an active node
			// or when we find ourself (then it means we looped through all the list, and no active node has been found
			while (!previous.equals(me) && !previous.isActive()) {
				previous = nodeList.lower(previous);
			} 
			// if no active node has been found, we return null value
			if (previous.equals(me)) {
				previous = null;
			}
		}
		
		return previous;
	}

	/**
	 * Get the thread of the console
	 * @return
	 */
	public Thread getConsoleThread() {
		return consoleThread;
	}

	/** 
	 * Set the thread of the console
	 * @param consoleThread
	 */
	void setConsoleThread(Thread consoleThread) {
		this.consoleThread = consoleThread;
	}

	/**
	 * Get the thread of the scheduler
	 * @return
	 */
	public Thread getSchedulerThread() {
		return schedulerThread;
	}

	/** 
	 * Set the thread of the scheduler
	 * @param schedulerThread
	 */
	void setSchedulerThread(Thread schedulerThread) {
		this.schedulerThread = schedulerThread;
	}

	/**
	 * Get the thread of the listener
	 * @return
	 */
	public Thread getListenerThread() {
		return listenerThread;
	}

	/**
	 * Get the configuration value : scheduler period (time the scheduler must wait between 2 jobs)
	 * @return the time to wait in ms
	 */
	public int getSchedulerPeriod() {
		// Default is 60s period for the scheduler. 
		return 60 * 1000;
	}
	
	/**
	 * Get the maximum connection failure allowed to a node before we set it as inactive
	 * @return
	 */
	public static int getMaxFailureConnect() {
		return 5;
	}

	private String getDefaultStoragePath() {
		return "C:\\TEMP\\5A\\PORT" + listener.getPort();
	}
	public Path getStoragePath() {
		return storageManager.getStoragePath();
	}
	protected void setStoragePath(String path) {
		String storagePath;
		if (path != null && path != "") {
			storagePath = path;
		}
		else {
			storagePath = getDefaultStoragePath();
		}
		storageManager = new StorageManager(this, storagePath);
	}
	public StorageManager getStorageManager() {
		return this.storageManager;
	}
	private StorageManager storageManager;
	
	public NodeAddress[] findResourceHost(Resource resource, int nbRepl ) {
		NodeAddress[] result = new NodeAddress[nbRepl];
		int count = 0;
		//TODO improve the way to choose node to host the resource;
		Iterator<NodeAddress> iterator = getDomainNodeList().iterator();
		Boolean higherHashCode = false;
		while (iterator.hasNext() && !higherHashCode) {
			result[count] = iterator.next();
			// We stop at the first NodeAddress that has a hashcode smaller than the resource hashCode
			higherHashCode = result[count].hashCode() < resource.hashCode();
			count = (count+1) % nbRepl;
		}
		return result;
	}
}

