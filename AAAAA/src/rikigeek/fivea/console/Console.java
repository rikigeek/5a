package rikigeek.fivea.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

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

	private Node node;

	public Console(Node node) {
		this.node = node;
		br = new BufferedReader(new InputStreamReader(System.in));
		ps = System.out;
		cs = System.console();
	}

	BufferedReader br;
	PrintStream ps;

	private boolean quit = false;
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

		while (!quit) {
			String line = readLine(" > ");
			execute(line);
		}
		// Quit the program
	}

	/**
	 * Execute order received on console. if command is not correct, nothing is
	 * done
	 * 
	 * @param line
	 *            the command typed by user
	 */
	private void execute(String line) {
		// 'quit' command request to stop the program
		if (line.equals("quit")) {
			quit = true;
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
			if (list != null) {
				if (nodeId >= 0) { // only remote request if nodeId was set

					// Get the nodeAddress of the nodeId-th node
					if (list.length > nodeId) {
						MessageNodeAddress[] remoteList = new DispatchConfig(node)
								.requestNodeList(new NodeAddress(list[nodeId]));
						if (remoteList != null && remoteList.length > 0) {
							// We replace the local list with the remote one if the remote one is not empty
							printf("List of the remote node %s is:%n", list[nodeId]);
							list = remoteList;
						} else {
							// No node in the remote list
							printf("The remote node #%d (%s) doesn't have a node list%n", nodeId, list[nodeId]);
							// And we set the local list a empty to not show it in the console
							list = new MessageNodeAddress[0];
						}
					} else {
						// Node # nodeid doesn't exist
						printf("There is no node #%d%n", nodeId);
						list = new MessageNodeAddress[0];
					}
				} else {
					// get the local list
					printf("List of locally known nodes is:%n");
				}
				// Display the list
				for (int i = 0; i < list.length; i++) {
					printf("#%d : %s%n", i, list[i]);
				}
			} else {
				printf("ERROR ! No local node is known");
			}
		}
		// 'show me' command shows the address of the local node (the current node)
		if (line.equals("show me")) {
			printf("Local node is %s%n", node.getAddress());
		}
	}

}
