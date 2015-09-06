package rikigeek.fivea;

import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;

// Class to dispatch a "REPLICATION" message
public class DispatchReplication implements Runnable {
	private static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());
	
	private static DispatchReplication dispatchSingleton = null;
	private Message message;
	private Node node;
	
	private DispatchReplication() {
		// Private constructor for singleton
	}
	
	public static DispatchReplication getInstance() {
		if (dispatchSingleton == null) 
			dispatchSingleton = new DispatchReplication();
		return dispatchSingleton;
	}

	public void threatMessage(Message message, Node node) {
		this.message = message;
		this.node = node;
		new Thread(this).start();
	}

	@Override
	public void run() {
		Thread.currentThread().setName(node.getAddress().getTCPPort() + "-DispatchReplication(" + Thread.currentThread().getId() + ")");
		LOGGER.finest("DispatchConnection Thread started for receivedMessage #"
				+ message.getId());

		LOGGER.finest("DispatchConnection Thread is stopping");
		
		
	}

}
