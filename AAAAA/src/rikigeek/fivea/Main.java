package rikigeek.fivea;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import rikigeek.fivea.console.Console;
import rikigeek.fivea.entities.MessageNodeAddress;

public class Main {
	private static Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	public static void main(String args[]) throws Exception {
		// Logging configuration
		Logger root = LogManager.getLogManager().getLogger("");
		Handler[] handlers = root.getHandlers();
		if (handlers[0] instanceof ConsoleHandler) {
			handlers[0].setFormatter(new ThreadFormatter());
			// TODO : remove the manual setting for logLevel (used only in
			// Eclipse IDE)
			handlers[0].setLevel(Level.ALL);
			root.setLevel(Level.ALL);
		}

		/*
		 * Minimal args parsing Possible options are if we connect to an
		 * existing domain Main contact:port [TCP Port] or if we create a new
		 * domain Main new domainName [TCP Port]
		 */
		String domainName = "";
		int port = 0;
		String contact = "";
		int contactPort;
		MessageNodeAddress contactNode = null;

		// no error check... exception will be thrown to the console
		int argc = args.length;
		if (args[0].compareToIgnoreCase("new") == 0) {
			// Creation of a new domain
			domainName = args[1];
			// Eventually get the port number
			if (argc > 2) {
				port = Integer.parseInt(args[2]);
			}
		} else {
			// Joining an existing domain
			contact = args[0];
			String s[] = contact.split(":");
			contactPort = Integer.parseInt(s[1]);
			contact = s[0];
			contactNode = new MessageNodeAddress(contact, contactPort);
			// Eventually get the port number
			if (argc > 1) {
				// the TCP port
				port = Integer.parseInt(args[1]);
			}
		}

		LOGGER.finest("choosing listening port");
		// Setting default listening port if not set
		if (port == 0) {
			LOGGER.config("using default port : " + Listener.DEFAULT_PORT);
			port = Listener.DEFAULT_PORT;
		} else {
			LOGGER.config("using specified port : " + port);
		}
		// Set a name to the thread (now we have the listening port)
		Thread.currentThread().setName(port + "-Main(" + Thread.currentThread().getId() + ")");
		// Parameters are parsed
		LOGGER.finest("Building the node");
		Node node;
		if (contactNode != null) {
			// Join a domain
			node = new Node(contactNode, port);
			LOGGER.finest("the node is built");
		} else {
			// New domain
			node = new Node(domainName, port);
			LOGGER.finest("A new domain is created, and the node is built");
		}

		// Create a client to send data to itself
		//new rikigeek.aaaaa.testing.ClientTesting();

		// Start the console if the node is connected to the domain
		if (node.isConnected()) {
			// Start the scheduler and save it into the node
			Scheduler scheduler = new Scheduler(node);
			Thread schedulerThread = new Thread(scheduler);
			node.setSchedulerThread(schedulerThread);
			schedulerThread.start();
			
			// Start the console after saving the console thread instance
			node.setConsoleThread(Thread.currentThread());
			startConsole(node);
		}
		else {
			node.stopNode();
			LOGGER.severe("The node could not connect to the domain... Exiting");
			System.out.println("The node could not connect to the domain... Exiting");
		}
		
		LOGGER.exiting(Main.class.getCanonicalName(), "static main(...)");
	}
	
	/** 
	 * Blocking method : this method returns only when the program must quit
	 */
	private static void startConsole(Node node) {
		LOGGER.info("Starting console in current Thread");
		// Start the client console
		new Console(node).start();

		LOGGER.info("Console is closed. The node should already has been closed");
		// We stop the node if it was not already closed
		if (!node.isStopped()) node.stopNode();
	}

}
