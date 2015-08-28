package rikigeek.fivea;

import java.io.BufferedReader;
import java.util.logging.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
/***
 * Treat a connection, and dispatch orders to correct functions
 * @author Rikigeek
 *
 */
public class Dispatcher implements Runnable {
	private static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());
	
	private Socket connection;
	private Node node;
	public Dispatcher(Node node, Socket connection) {
		this.node = node;
		this.connection = connection;
	}
	
	

	@Override
	public void run() {
		LOGGER.info("Receiving a new connection");
		receivesObject();
	}
	
	private void receivesObject() {
		try {
			ObjectInputStream inputStream = new ObjectInputStream(connection.getInputStream());
			ObjectOutputStream outputStream = new ObjectOutputStream(connection.getOutputStream());
			LOGGER.fine("object streams opened");
			try {
				// We wait for a message
				LOGGER.fine("reading object");
				Object recv;
				recv = inputStream.readObject();
				if (recv != null) {
					if (recv.getClass().equals(Message.class)) {
						Message msg = (Message) recv;
						LOGGER.fine("Read : " + msg);
						switch (msg.subject) {
						case CONSULT:
							LOGGER.info("Received consultation message");
							DispatchConsult.getInstance().threatMessage(msg, this.node);
							break;
						case CONFIG:
							LOGGER.info("Received configuration message");
							break;
						case CONNECTION:
							LOGGER.info("Received connection message ID1");
							DispatchConnection.getInstance().threatMessage(msg, this.node);
							break;
						case REPLICATION:
							LOGGER.info("Received replication message");
							DispatchReplication.getInstance().threatMessage(msg, this.node);
							break;
						case RESERVED: 
							LOGGER.info("Received reserved message");
							break;
						default:
							LOGGER.warning("Received unknown message");
						}
					} 
					else {
						LOGGER.warning("Received an object with wrong class : " + recv.getClass().getName());
					}
					LOGGER.fine("Confirm to client");
					outputStream.writeObject(new String("DONE"));
					outputStream.flush();
				}

			} catch (Exception e){
				e.printStackTrace();
			}
				
			LOGGER.info("Exiting and closing connection");
			inputStream.close();
			outputStream.close();
			connection.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unused")
	private void receivesString() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			PrintWriter writer = new PrintWriter(connection.getOutputStream());
			boolean stop = false;
			
			while (!stop && !connection.isClosed() && connection.isConnected()) {
				
				String line = reader.readLine(); // First we read the header line to know what to do with this message
				if (line != null){
					if (line.toUpperCase().equalsIgnoreCase("STOP")) stop = true;
					if (line.equalsIgnoreCase("SHUTDOWN")) {
						// TODO : shutdown the server
					}
					String s = String.format("Receiving data {0}", line);
					LOGGER.fine(s);
					
					writer.println("Well received my lord");
					if (stop) writer.println("Bye bye");
					writer.flush();
				} else {
					LOGGER.warning("Cannot read on the socket");
					stop = true;
				}
			}
			LOGGER.info("Exiting and closing connection");
			reader.close();
			writer.close();
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			connection.close();
		} catch (Exception e) {
			// Do nothing
		}
		super.finalize();
	}

}
