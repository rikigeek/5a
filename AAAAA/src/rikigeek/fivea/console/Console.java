package rikigeek.fivea.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import rikigeek.fivea.Node;

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

	public PrintStream printf(String format, Object... objects) {
		return ps.format(format, objects);
	}

	public String readLine(String out) {
		ps.format(out);
		try {
			return br.readLine();
		} catch (IOException ex) {
			return null;
		}
	}

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

	private void execute(String line) {
		if (line.equals("quit")) {
			quit = true;
		}
	}

}
