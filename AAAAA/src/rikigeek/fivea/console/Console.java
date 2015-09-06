package rikigeek.fivea.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.logging.Logger;

import rikigeek.fivea.DispatchConfig;
import rikigeek.fivea.Node;
import rikigeek.fivea.entities.MessageNodeAddress;
import rikigeek.fivea.entities.NodeAddress;

/**
 * The console of a node
 * 
 * @author Rikigeek
 *
 */
public class Console {

	private static Logger LOGGER = Logger.getLogger(Node.class.getName());
	private Node node;

	public Console(Node node) {
		this.node = node;
		br = new BufferedReader(new InputStreamReader(System.in));
		ps = System.out;
		cs = System.console();
	}

	BufferedReader br;
	PrintStream ps;

	private java.io.Console cs;

	/**
	 * Writing something to the standard output. Note we encapsulate the access
	 * to the output, because in some cases, System.console() doesn't work
	 * 
	 * @param format
	 * @param objects
	 * @return
	 */
	public PrintStream printf(String format, Object... objects) {
		return ps.format(format, objects);
	}

	/**
	 * Reading line input from the standard input. We can set a prompt Note we
	 * encapsulate the access to the input, because in some cases,
	 * System.console() doesn't work
	 * 
	 * @param out
	 *            the prompt we want to print before the user input
	 * @return the line the user has typed in (everything that is typed until he
	 *         typed <Enter>
	 */
	public String readLine(String out) {
		// If console is defined, we use it.
		if (cs != null) {
			return cs.readLine(out);
		} else { // Console is not set, we use the old way
			ps.format(out);
			try {
				return br.readLine();
			} catch (IOException ex) {
				return null;
			}
		}
	}

	/**
	 * Start the console. A standard message is shown, and then the console
	 * waits for user input
	 */
	public void start() {

		// Start a console. User will be able to interact with the application
		printf("Welcome to the 5A console. Connected to %s. Waiting for order%n",
				node.getAddress());

		while (!node.isStopped()) {
			String line = readLine(" > ");
			execute(line);
		}
		// Quit the program
	}

	private void usage() {
		// Show usage help
		printf("quit          : stop the local node%n");
		printf("stop <nodeId> : stop the remote node #nodeId%n");
		printf("show me                : print information about local node%n");
		printf("     nodelist          : print local node list%n");
		printf("     nodelist <nodeId> : print node list known by the remote node #nodeId%n");
		printf("put <file>  : put file in the domain%n");
		printf("get <file>  : get file from the domain%n");
		printf("dir         : show content of current directory%n");
		printf("cd          : show current directory%n");
		printf("cd <dir>    : change to directory dir%n");
		printf("del <file>  : delete file from the domain%n");
		printf("file <file> : display information about the file stored in the domain%n");
		printf("help : this help%n");
		printf("%n");
		
	}
	/**
	 * Execute order received on console. if command is not correct, nothing is
	 * done
	 * 
	 * @param line
	 *            the command typed by user
	 */
	private void execute(String line) {
		// if a command is recognized, then it returns at the end of the if.
		// because, at the end of this method, the usage is always printed.
		
		// 'help' command show usage information
		if (line.equals("help")) {
			usage();
			return;
		}
		// 'quit' command request to stop the program
		if (line.startsWith("quit")) {
			node.stopNode();
			return;
		}
		// 'stop' commande request a remote node to stop
		if (line.startsWith("stop")) {
			// We must now check what is the value after the command
			String value = line.substring("stop".length()).trim();
			int nodeId = -1;
			// Try to parse this value
			try {
				if (!value.equals(""))
					nodeId = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				printf("%s is not a valid number.%n", value);
			}

			if (nodeId >= 0) {
				// Stop the remote node
				// Get its address
				NodeAddress remoteNode = getNodeAddress(nodeId);
				if (new DispatchConfig(node).requestStopNode(remoteNode))
					printf("Remote Node %s is stopping%n", remoteNode);
				else
					printf("Failed to stop the remote node %s%n", remoteNode);
			} else {
				usage();
			}
			return;
		}
		// 'show nodelist' command request to show the local nodelist
		if (line.startsWith("show nodelist")) {
			// We must now check what is the value after the command
			String value = line.substring("show nodelist".length()).trim();
			int nodeId = -1;
			// Try to parse this value
			try {
				if (!value.equals(""))
					nodeId = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				printf("%s is not a valid number.%n", value);
			}
			// Get the local list of nodes
			MessageNodeAddress[] list = node.getDomainNodeList().toArray(
					new MessageNodeAddress[0]);

			if (nodeId >= 0) { // only remote request if nodeId was set
				// Get the nodeAddress of the nodeId-th node
				NodeAddress remoteNode = getNodeAddress(nodeId);
				if (remoteNode != null) {
					// Send the message to request the list of the remote node
					MessageNodeAddress[] remoteList = new DispatchConfig(node)
							.requestNodeList(remoteNode);
					if (remoteList != null && remoteList.length > 0) {
						// We replace the local list with the remote one if
						// the remote one is not empty
						printf("List of the remote node %s is:%n", remoteNode);
						list = remoteList;
					} else {
						// No node in the remote list
						printf("The remote node #%d (%s) doesn't have a node list%n",
								nodeId, list[nodeId]);
						// And we set the local list a empty to not show it
						// in the console
						list = new MessageNodeAddress[0];
					}
				}
			} else {
				// get the local list
				printf("List of locally known nodes is:%n");
			}
			// Display the list
			for (int i = 0; i < list.length; i++) {
				printf("#%d : %s%n", i, list[i]);
			}
			return;
		}
		// 'show me' command shows the address of the local node (the current
		// node)
		if (line.equals("show me")) {
			printf("Local node is %s%n", node.getAddress());
			return;
		}
		
		// if we reach this point, it means the command is unknown
		usage();
	}

	/**
	 * Get the nodeAddress from the nodeid (node id is the # of the node in the local list)
	 * @param nodeId 
	 * @return
	 */
	private NodeAddress getNodeAddress(int nodeId) {
		// Get the local list of nodes
		MessageNodeAddress[] list = node.getDomainNodeList().toArray(
				new MessageNodeAddress[0]);
		if (list != null) {
			if (nodeId >= 0) { // check if nodeId was set

				// Get the nodeAddress of the nodeId-th node
				if (list.length > nodeId) {
					return new NodeAddress(list[nodeId]);
				} else {
					// Node # nodeid doesn't exist
					printf("There is no node #%d%n", nodeId);
				}
			}
			// Node # nodeid doesn't exist
			LOGGER.warning("Node #" + nodeId + " doesn't exist");
		} else {
			printf("ERROR ! No local node is known%n");
			LOGGER.warning("ERROR ! No local node is known");
		}
		return null;

	}

}
