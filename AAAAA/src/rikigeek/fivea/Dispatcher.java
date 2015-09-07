package rikigeek.fivea;

import java.util.logging.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import rikigeek.fivea.entities.Message;

/***
 * Treat a connection, and dispatch orders to correct functions
 * 
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
		Thread.currentThread().setName(node.getAddress().getTCPPort() + "-Dispacher(" + Thread.currentThread().getId() + ")");
		LOGGER.info("Receiving a new connection");
		receivesObject();
	}

	private void receivesObject() {
		try {
			ObjectInputStream inputStream = new ObjectInputStream(
					connection.getInputStream());
			ObjectOutputStream outputStream = new ObjectOutputStream(
					connection.getOutputStream());
			LOGGER.fine("object streams opened");
			try {
				// We wait for a message
				LOGGER.fine("reading object");
				Object recv;
				recv = inputStream.readObject();
				if (recv != null) {
					if (recv.getClass().equals(Message.class)) {
						// The received message
						Message receivedMessage = (Message) recv;
						// The future response
						Message response = null;
						
						LOGGER.fine("Read : " + receivedMessage);
						
						// forward the request to the right Dispatcher, and get the response in return
						switch (receivedMessage.getSubject()) {
						case CONSULT:
							LOGGER.info("Received consultation message");
							response = new DispatchConsult(node).receivesMessage(
									receivedMessage);
							break;
						case CONFIG:
							LOGGER.info("Received configuration message");
							response = new DispatchConfig(node).receivesMessage(receivedMessage);
							break;
						case CONNECTION:
							LOGGER.info("Received connection message");
							response = new DispatchConnection(node).receivesMessage(
									receivedMessage);
							break;
						case REPLICATION:
							LOGGER.info("Received replication message");
							response = new DispatchReplication(node).receivesMessage(receivedMessage);
							break;
						case RESERVED:
							LOGGER.info("Received reserved message");
							break;
						default:
							LOGGER.warning("Received unknown message");
						}
						// Now we should have a response from the dispatcher. 
						// So we check if an answer is expected, and send him something
						if (receivedMessage.needAnswer()) {
							if (response != null) {
								// The response from the dispatcher is a class instance, we can answer with this
							} else {
								// no response provided, but the client wait for a response. Send him a default error message
								response = Message.noOk(node.getAddress(), receivedMessage);
							}
							LOGGER.fine("Send the response to the client " + response);
							outputStream.writeObject(response);
							outputStream.flush();
						}
					} else {
						LOGGER.warning("Received an object with wrong class : "
								+ recv.getClass().getName());
						// TODO CONNECT Maybe the client is waiting for a response.
					}
				} // If received object is null, we don't do anything special
				else {
					LOGGER.warning("Received a NULL object");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			LOGGER.info("Exiting and closing connection");
			inputStream.close();
			outputStream.close();
			connection.close();

		} catch (IOException e) {
			LOGGER.throwing(this.getClass().getCanonicalName(), "receivesObject()", e);
			LOGGER.severe("Failure while opening, or closing the streams");
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
