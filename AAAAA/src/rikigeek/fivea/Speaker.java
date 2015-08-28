package rikigeek.fivea;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;


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
	private NodeAddress destNode;
	private boolean connected = false;

	public Speaker(NodeAddress dest) {
		open(dest);
	}
	public boolean open(NodeAddress dest) {
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

	}

	public Message sendMessage(Message msg) {
		if (!connected) {
			// The socket is not connected.
			LOGGER.warning("Unable to send the message, we are not connected");
			return null;
		}
		LOGGER.info("Sending a message to " + destNode);
		Message response = null;

		try {

			// First we send the message
			outputStream.writeObject(msg);
			outputStream.flush();

			// And we try to read the response, only if message is a question
			if (msg.question) {
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
				} catch (ClassNotFoundException e) {
					LOGGER.warning("Failure to get response");
				}
			}

		} catch (IOException e) {
			// Happens if sockets or Data Streams fail
			LOGGER.warning("Failed to send or receive the message");
			LOGGER.throwing("Speaker", "SendMessage", e);
		}

		return response;

	}

	public boolean isConnected() {
		return connected;
	}

}
