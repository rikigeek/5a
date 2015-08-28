package rikigeek.fivea;

import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;

// Class to dispatch a "CONSULT" message
public class DispatchConsult implements Runnable {
	private static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());
	
	private static DispatchConsult dispatchSingleton = null;
	private Message message;
	private Node node;
	
	private DispatchConsult() {
		// Private constructor for singleton
	}
	
	public static DispatchConsult getInstance() {
		if (dispatchSingleton == null) 
			dispatchSingleton = new DispatchConsult();
		return dispatchSingleton;
	}
	
	public void threatMessage(Message message, Node node) {
		this.message = message;
		this.node = node;
		new Thread(this).start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	

}
