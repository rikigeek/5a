package rikigeek.fivea;

import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import rikigeek.fivea.entities.MessageNodeAddress;

public class Main {
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

		// Setting default listening port if not set
		if (port == 0)
			port = Listener.DEFAULT_PORT;

		// Parameters are parsed
		Node node;
		if (contactNode != null) {
			// Join a domain
			node = new Node(contactNode, port);
		} else {
			// New domain
			node = new Node(domainName, port);
		}

		// Create a client to send data to itself
		//new rikigeek.aaaaa.testing.ClientTesting();
	}
}
