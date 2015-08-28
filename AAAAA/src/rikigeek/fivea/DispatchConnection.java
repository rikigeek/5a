package rikigeek.fivea;

import java.util.logging.Logger;

// Class to dispatch a "CONNECTION" message
public class DispatchConnection implements Runnable {
	private static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

	private static DispatchConnection dispatchSingleton = null;
	private Message message;
	private Node node;
	
	private DispatchConnection() {
		// Private constructor for singleton
	}
	
	public static DispatchConnection getInstance() {
		if (dispatchSingleton == null) 
			dispatchSingleton = new DispatchConnection();
		return dispatchSingleton;
	}

	public void threatMessage(Message message, Node node) {
		this.message = message;
		this.node = node;
		new Thread(this).start();
	}

	@Override
	public void run() {
		Speaker speaker;
		switch(message.verb) {
		case PERTEELEC:
			LOGGER.info("Received PERTEELEC message");
			speaker = new Speaker(message.sourceNodeAddress);
			node.setElectionFlag(true);
			node.setFatherLostFlag(true);
			//
			break;
		case BROTHER:
			break;
		case CONNECT:
			break;
		case DISCONNECT:
			break;
		case INSERT:
			break;
		case NEW:
			break;
		case NOK:
			break;
		case OK:
			break;
		case PERTECONNECT:
			break;
		case PERTERESULELEC:
			break;
		case QUIT:
			break;
		case REQINS:
			break;
		case STOP:
			break;
		case TREECONNECT:
			break;
		case TREEWEIGHT:
			break;
		case WAKEUP:
			break;
		default:
			break;
		}
		
	}
}
