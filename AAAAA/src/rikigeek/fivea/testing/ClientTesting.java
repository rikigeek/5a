package rikigeek.fivea.testing;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.MessageNodeAddress;

public class ClientTesting {
	private Socket socket;
	private static Logger LOGGER = Logger.getLogger(ClientTesting.class.getName());
	// We don't deal with Exception, it's a test
	public ClientTesting() throws Exception {
		
		LOGGER.setLevel(Level.ALL);
		
		socket = new Socket("localhost", 20158);
		
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		
		Message msg = Message.stop(new MessageNodeAddress("TOTO", 2121), "Données".getBytes());
		LOGGER.info("Starting client");
		//String toto = new String("TOTO");
		
		oos.writeObject(msg);
		oos.flush();
		
		Object obj = ois.readObject();
		
		LOGGER.fine("received object : " + obj.getClass().getName() + " = " + obj.toString());
		ois.close();
		oos.close();
		
		socket.close();
	}
	
	
	

}
