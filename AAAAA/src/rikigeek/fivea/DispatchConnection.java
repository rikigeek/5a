package rikigeek.fivea;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.MessageNodeAddress;
import rikigeek.fivea.entities.NodeAddress;

// Class to dispatch a "CONNECTION" message
public class DispatchConnection implements Runnable {
	private static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

	private static DispatchConnection dispatchSingleton = null;
	private Message receivedMessage;
	private Node node;
	enum Action { NODELISTUPDATED }
	private Action threadedAction;

	public DispatchConnection() {
		// Private constructor for singleton
	}

	public static DispatchConnection getInstance() {
		if (dispatchSingleton == null)
			dispatchSingleton = new DispatchConnection();
		return dispatchSingleton;
	}

	public Message threatMessage(Message message, Node node) {
		// Save the informations in the Object instance
		this.receivedMessage = message;
		this.node = node;
		switch (receivedMessage.getVerb()) {
		case ADDNODE:
			LOGGER.info("Received ADDNODE message");
			return doAddNode();
		case UPDLIST:
			LOGGER.info("Received UPDLIST message");
			return doUpdList(); 
		case QUIT:
			LOGGER.info("Received QUIT message");
			return doQuit();
		case CHECKNODE:
			LOGGER.info("Received CHECKNODE message");
			return doCheckNode();
		default:
			return null;
		}
	}

	/**
	 * Do the ADDNODE message
	 * 
	 * @param receivedMessage
	 */
	private Message doAddNode() {
		// First we answer with the list of all nodes that we knows
		NodeAddress[] list;
		list = node.getDomainNodeList().toArray(new NodeAddress[0]);
		
		Message response = Message.okAddNode(receivedMessage.getId(), node.getAddress(),
				list, node.getDomain());
		LOGGER.fine("Response to client : " + response);
		
		// We must add the source node to the local node list
		MessageNodeAddress sourceNode = receivedMessage.getSource();
		node.getDomainNodeList().add(new NodeAddress(sourceNode.getIpAddress(), sourceNode.getTCPPort()));
		
		// Now we can deal with the new node list (It's basically the same as if we receive a UPDLIST message)
		// but we do this in a new thread
		this.threadedAction = Action.NODELISTUPDATED;
		new Thread(this).start();
		
		// and now we can return the answer
		return response;
		
	}
	/**
	 * Do the UPDLIST message
	 * 
	 * @param receivedList : the list of nodes that we received as an update
	 */
	private Message doUpdList(){
		// We request a forward of the list update
		this.threadedAction = Action.NODELISTUPDATED;
		new Thread(this).start();
		return null;
	}

	/**
	 * Do the QUIT message
	 */
	private Message doQuit() {
		// TODO : check the list of ressources 
		// 
		return null;
	}
	
	/**
	 * Do the CHECKNODE message 
	 */
	private Message doCheckNode() {
		// Simply answer to the sender
		Message response = Message.okCheckNode(receivedMessage.getId(), node.getAddress());
		return response;
	}
	
	/**
	 * Thread execution
	 */
	@Override
	public void run() {
		LOGGER.fine("DispatchConnection Thread starte");
		// We act depending on the threadedAction attribute
		if (this.threadedAction == Action.NODELISTUPDATED) {
			forwardLocalNodeListUpdate();
		}
	}

	private void forwardLocalNodeListUpdate() {
		// This method is called whenever the node list is updated. So that we can forward this to any nodes that needs this update

		// Extract the list we received in the message
		MessageNodeAddress[] receivedList = receivedMessage.getNodeList();
		// Create a new speaker that we'll use to connect to each node
		Speaker speaker = new Speaker();
		// We can add to the local list of nodes the nodes that are in the provided list
		if (receivedList != null) {
			LOGGER.fine("Received a list of " + receivedList.length + " nodes");
			for (int i = 0; i <receivedList.length; i++) {
				MessageNodeAddress newNodeAddress = receivedList[i];
				// Create a new NodeAddress and add it into the local list of domain nodes
				node.getDomainNodeList().add(new NodeAddress(newNodeAddress.getIpAddress(), newNodeAddress.getTCPPort()));
				
			}
		}
		else {
			LOGGER.fine("Received an empty list of nodes");
		}
		// Get the list of nodes that don't have received this update yet
		MessageNodeAddress[] list = compareNodeList(receivedList);
		
		if (list != null) {
			// Get the full list of domain nodes, to send to every nodes
			MessageNodeAddress[] updatedList = (MessageNodeAddress[]) node.getDomainNodeList().toArray(new NodeAddress[0]);
			// The message we will send to every missing nodes 
			Message messageUpdateList = Message.updList(node.getAddress(), updatedList);
			// Send the message to each node of list
			for (int i = 0; i < list.length; i++) {
				if (speaker.open(list[i])) {
					speaker.sendMessage(messageUpdateList);
				}
				else {
					// The node could not be contacted. We hope it will be contacted later
					LOGGER.warning("Unable to connect to the remote node " + list[i]);
				}
			}
		} // if list is null, nothing has to be done
		
	}
	/**
	 * Find what nodes are in the domain, but not in the provided list.
	 * We exclude the source node, and the current node
	 * @param list
	 * @return
	 */
	private MessageNodeAddress[] compareNodeList(MessageNodeAddress[] list) {
		// Get the current list of all nodes in the domain
		ConcurrentSkipListSet<NodeAddress> localList = new ConcurrentSkipListSet<NodeAddress>(node.getDomainNodeList());
		LOGGER.fine("Local node list of " + localList.size() + " elements");
		int cpt = 0;
		for (NodeAddress n : localList) {
			LOGGER.fine(" LocalList #" + cpt + " = " + n + " / " + n.hashCode());
			cpt++;
		}
		// If provided list is empty, we return all the local list
		if (list != null) {
			LOGGER.fine("Provided node list of " + list.length + " elements");
			// Now, we are looking for the nodes that are missing from the provided list
			for (int i = 0; i < list.length; i++) {
				LOGGER.fine(" providedList #" + i + " = " + list[i]);
				if (localList.contains(list[i])) {
					LOGGER.fine("This one is already in local list");
					localList.remove(list[i]);
				}
			}
		}
		// Remove myself from the list of node to contact
		NodeAddress me = new NodeAddress(node.getAddress());
		LOGGER.fine("me = " + me + " / " + me.hashCode());
		localList.remove(me);
		// Remove the source node
		NodeAddress sourceNode = new NodeAddress(receivedMessage.getSource());
		LOGGER.fine("sourceNode = " + sourceNode + " / " + sourceNode.hashCode());
		localList.remove(new NodeAddress(receivedMessage.getSource()));
		// return the list of node from the domain, but not in the list
		return (MessageNodeAddress[]) localList.toArray(new NodeAddress[0]);
	}

}
