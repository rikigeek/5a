package rikigeek.fivea;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.MessageNodeAddress;


/**
 * The speaker is the class that communicates with other nodes
 * 
 * usage : s = Speaker(node to connect)
 * s.sendMessage(message to send)
 * 
 * @author Rikigeek
 *
 */
public class Speaker {

	private static Logger LOGGER = Node.Logger();

	private Socket socket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private MessageNodeAddress destNode;
	private boolean connected = false;

	/**
	 * Create a new instance and connect to the dest node
	 * @param dest
	 */
	public Speaker(MessageNodeAddress dest) {
		open(dest);
	}
	/**
	 * Create a new instance, but don't connect to any node
	 */
	public Speaker() {
		connected = false;
	}
	/**
	 * Open a connection to the remote Node
	 * @param dest
	 * @return true if the connection succeeded
	 */
	public boolean open(MessageNodeAddress dest) {
		if (connected) {
			// If the speaker is already connected, we must close it before
			close();
		}
		// Save the node we want to connect to
		this.destNode = dest;
	
		try {
			// opening the socket
			socket = new Socket(destNode.getIpAddress(), destNode.getTCPPort());

			// Building the streams
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());

			connected = true;
			LOGGER.info("Starting client");
		} catch (IOException e) {
			LOGGER.warning("Failure to connect to the remote node");
			LOGGER.throwing("Speaker", "Speaker", e);
		}
		return connected;

	}

	/**
	 * Close the connection to the remote node
	 */
	public void close() {
		// Closing everything
		LOGGER.fine("closing streams and socket");
		try {
			inputStream.close();
			outputStream.close();
			socket.close();
		} catch (IOException e) {
			LOGGER.warning("Failed to close the speaker");
			LOGGER.throwing("Speaker", "Close", e);
		}
		connected = false;

	}

	/**
	 * Send a message to the connected node
	 * @param msg
	 * @return the message we got as an answer (null if no response was expected)
	 */
	public Message sendMessage(Message msg) {
		if (!connected) {
			// The socket is not connected.
			LOGGER.warning("Unable to send the message, we are not connected");
			return null;
		}
		LOGGER.info("Sending a message to " + destNode);
		// Default answer is a positive answer
		Message response = Message.ok(msg.getSource(), msg);

		try {

			// First we send the message
			outputStream.writeObject(msg);
			outputStream.flush();

			// And we try to read the response, only if message is a question
			if (msg.needAnswer()) {
				try {
					Object obj;
					obj = inputStream.readObject();
					LOGGER.fine("received object : " + obj.getClass().getName()
							+ " = " + obj.toString());
	
					// Check if the response is a message
					if (obj instanceof Message) {
						LOGGER.fine("Object is a message");
						response = (Message) obj;
					} 
					else {
						LOGGER.warning("Object is not a message");
						response = Message.noOk(this.destNode, msg);
					}
				} catch (ClassNotFoundException e) {
					LOGGER.warning("Failure to get response");
					response = Message.noOk(this.destNode, msg);
				}
			}

		} catch (IOException e) {
			// Happens if sockets or Data Streams fail
			LOGGER.warning("Failed to send or receive the message");
			LOGGER.throwing("Speaker", "SendMessage", e);
			response = Message.noOk(this.destNode, msg);
		}

		return response;

	}

	/**
	 * Is the speaker connected to a node ?
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}

}
