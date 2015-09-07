package rikigeek.fivea;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.MessageNodeAddress;
import rikigeek.fivea.entities.NodeAddress;

// Class to dispatch a "CONNECTION" message
public class DispatchConnection implements Runnable {
	private static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

	private Message receivedMessage; // Message received = message to analyze
	private Node node; // Instance of the currect node

	enum Action {
		NODELISTUPDATED
	}

	private Action threadedAction; // Selection of the action to do in the new
									// thread (asynchronous job)

	public DispatchConnection(Node node) {
		this.node = node;
	}

	/**
	 * Threat the received message, and return the message to send back to the
	 * send (the response)
	 * 
	 * @param message
	 *            the message we received and that we must work on
	 * @return a message to send back to the sender
	 */
	public Message receivesMessage(Message message) {
		// Save the informations in the Object instance
		this.receivedMessage = message;
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
	 */
	private Message doAddNode() {
		// First we answer with the list of all nodes that we knows
		NodeAddress[] list;
		// small trick : initialize the array with an empty NodeAddress array,
		// so that list will never be null
		list = node.getDomainNodeList().toArray(new NodeAddress[0]);

		Message response = Message.okAddNode(receivedMessage.getId(),
				node.getAddress(), list, node.getDomain());
		LOGGER.fine("Response to client : " + response);

		// We must add the source node to the local node list
		MessageNodeAddress sourceNode = receivedMessage.getSource();
		node.getDomainNodeList().add(
				new NodeAddress(sourceNode.getIpAddress(), sourceNode
						.getTCPPort()));

		// Now we can deal with the new node list (It's basically the same as if
		// we receive a UPDLIST message)
		// but we do this in a new thread
		this.threadedAction = Action.NODELISTUPDATED;
		new Thread(this).start();

		// and now we can return the answer
		return response;

	}

	/**
	 * Do the UPDLIST message
	 */
	private Message doUpdList() {
		// We request a forward of the list update
		this.threadedAction = Action.NODELISTUPDATED;
		new Thread(this).start();
		return null;
	}

	/**
	 * Do the QUIT message
	 */
	private Message doQuit() {
		// TODO REPLIC check the list of ressources
		//
		NodeAddress source = new NodeAddress(receivedMessage.getSource());
		if (node.getDomainNodeList().contains(source)) {
			source = node.getDomainNodeList().floor(source);
			source.setInactive();
		}
		return null;
	}

	/**
	 * Do the CHECKNODE message
	 */
	private Message doCheckNode() {
		// Simply answer to the sender
		Message response;
		// Send an ok only if node is not stopping
		if (node.isStopped())
			response = Message.noOk(node.getAddress(), receivedMessage);
		else
			response = Message.okCheckNode(receivedMessage.getId(),
					node.getAddress());

		// In background, compare the received list, and forward if necessary
		this.threadedAction = Action.NODELISTUPDATED;
		new Thread(this).start();

		return response;
	}

	/**
	 * Thread execution
	 */
	@Override
	public void run() {
		Thread.currentThread().setName(
				node.getAddress().getTCPPort() + "-DispachConnection("
						+ Thread.currentThread().getId() + ")");
		LOGGER.finest("DispatchConnection Thread started for receivedMessage #"
				+ receivedMessage.getId());
		// We act depending on the threadedAction attribute
		if (this.threadedAction == Action.NODELISTUPDATED) {
			forwardLocalNodeListUpdate();
		}
		LOGGER.finest("DispatchConnection Thread is stopping");
	}

	/**
	 * Method that analyze the received list. It updates the local list. And
	 * also forward to nodes that are in the local list but not in the received
	 * list
	 */
	private void forwardLocalNodeListUpdate() {
		// This method is called whenever the node list is updated. So that we
		// can forward this to any nodes that needs this update

		// Extract the list we received in the message
		MessageNodeAddress[] receivedList = receivedMessage.getNodeList();
		// Create a new speaker that we'll use to connect to each node
		Speaker speaker = new Speaker();
		// We can add to the local list of nodes the nodes that are in the
		// provided list
		if (receivedList != null) {
			LOGGER.fine("Received a list of " + receivedList.length + " nodes");
			for (int i = 0; i < receivedList.length; i++) {
				if (receivedList[i] != null) {
					// Create a new NodeAddress and add it into the local list
					// of domain nodes
					// Add it only if its logical clock is greater than the
					// local one (it means it's a more recent one)
					NodeAddress remote = new NodeAddress(receivedList[i]);
					LOGGER.finest("Doing node " + remote.toString());
					if (node.getDomainNodeList().contains(remote)) {
						// Already have this node in our list. So update it only
						// if info is more recent
						LOGGER.finest(" This one is already in local list  : "
								+ remote);
						NodeAddress local = node.getDomainNodeList().floor(
								remote);
						LOGGER.finest(" Comparing to local = " + local);
						if (local.compareLogicalClock(remote.getLogicalClock()) < 0) {
							// Local value is outdated
							LOGGER.finest(" Need to update the local one");
							local.update(remote.isActive(),
									remote.getLogicalClock());
						}
					} else {
						// Doesn't exist in our local list. Add it
						LOGGER.finest(" doesn't exist. Adding it");
						node.getDomainNodeList().add(remote);
					} // If contains
				} // END if not null
			}
		} else {
			LOGGER.fine("Received an empty list of nodes");
		}
		// Get the list of nodes that don't have received this update yet
		MessageNodeAddress[] list = compareNodeList(receivedList);

		if (list != null) {
			// Get the full list of domain nodes, to send to every nodes
			MessageNodeAddress[] updatedList = (MessageNodeAddress[]) node
					.getDomainNodeList().toArray(new NodeAddress[0]);
			// The message we will send to every missing nodes
			Message messageUpdateList = Message.updList(node.getAddress(),
					updatedList);
			// Send the message to each node of list
			for (int i = 0; i < list.length; i++) {
				// Don't send the message to myself or to sender
				NodeAddress dest = new NodeAddress(list[i]);
				LOGGER.finest("Sending UPDLIST message to " + dest);
				if (dest.compareTo(new NodeAddress(node.getAddress())) != 0
						&& dest.compareTo(new NodeAddress(receivedMessage
								.getSource())) != 0) {
					if (speaker.open(dest)) {
						speaker.sendMessage(messageUpdateList);
					} else {
						// The node could not be contacted. Nevermind, it will
						// be contacted later with a CHECK message
						LOGGER.warning("Unable to connect to the remote node "
								+ list[i]);
					}
				} else {
					LOGGER.finest("... well, not really sending a message to myself nor to the sender");
				}
			}
		} // if list is null, nothing has to be done

	}

	/**
	 * The node send a message to tell he'll be stopped soon
	 */
	public void stopNode() {
		Speaker speaker = new Speaker();
		NodeAddress me = new NodeAddress(node.getAddress());

		// First : who will we send the message to ? The active node before and
		// the node after me.
		NodeAddress previous = node.getPreviousNeighbor();
		NodeAddress next = node.getNextNeighbor();

		Message msg = Message.quit(me, null);
		// Previous is null if no active node has been found
		if (previous != null) {
			speaker.open(previous);
			speaker.sendMessage(msg);
			speaker.close();
		}
		// Again, we don't send message if no active node has been found
		// and we also don't send message if the next is also the previous
		if (next != null && next != previous) {
			speaker.open(next);
			speaker.sendMessage(msg);
			speaker.close();
		}
	}

	/**
	 * Find what nodes are in the domain, but not in the provided list. We
	 * exclude the source node, and the current node
	 * 
	 * @param list
	 * @return
	 */
	private MessageNodeAddress[] compareNodeList(MessageNodeAddress[] list) {
		// Get the current list of all nodes in the domain
		ConcurrentSkipListSet<NodeAddress> localList = new ConcurrentSkipListSet<NodeAddress>(
				node.getDomainNodeList());
		LOGGER.fine("Local node list of " + localList.size() + " elements");
		int cpt = 0;
		for (NodeAddress n : localList) {
			LOGGER.fine(" LocalList #" + cpt + " = " + n + " / " + n.hashCode());
			cpt++;
		}
		// If provided list is empty, we return all the local list
		if (list != null) {
			LOGGER.fine("Provided node list of " + list.length + " elements");
			// Now, we are looking for the nodes that are missing from the
			// provided list
			for (int i = 0; i < list.length; i++) {
				LOGGER.fine(" providedList #" + i + " = " + list[i]);
				if (localList.contains(new NodeAddress(list[i]))) {
					LOGGER.fine("This one is already in local list");
					// TODO CONNECT check (or not ?) if the local node is more recent.
					// In that case, maybe we should keep it ?
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
		LOGGER.fine("sourceNode = " + sourceNode + " / "
				+ sourceNode.hashCode());
		localList.remove(new NodeAddress(receivedMessage.getSource()));
		// return the list of node from the domain, but not in the list
		return (MessageNodeAddress[]) localList.toArray(new NodeAddress[0]);
	}

}
