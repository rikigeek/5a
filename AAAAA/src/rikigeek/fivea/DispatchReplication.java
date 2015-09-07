package rikigeek.fivea;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.NodeAddress;
import rikigeek.fivea.storage.Resource;

// Class to dispatch a "REPLICATION" message
public class DispatchReplication implements Runnable {
	private static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

	private Message message;
	private Node node;

	public DispatchReplication(Node node) {
		this.node = node;
	}

	public Message receivesMessage(Message message) {
		this.message = message;
		switch (message.getVerb()) {
		case NEWDOC:
			LOGGER.info("Received NEWDOC message");
			return doNewDoc();
		case FREE:
			LOGGER.info("Received FREE message");
			return doFree();
		default:
			return null;

		}
		// Start the thread only for background process
		// new Thread(this).start();
		// return Message.noOk(node.getAddress(), this.message);
	}

	@Override
	public void run() {
		Thread.currentThread().setName(
				node.getAddress().getTCPPort() + "-DispatchReplication("
						+ Thread.currentThread().getId() + ")");
		LOGGER.finest("DispatchConnection Thread started for receivedMessage #"
				+ message.getId());

		LOGGER.finest("DispatchConnection Thread is stopping");
	}

	private Message doNewDoc() {
		if (message.getResourceList() != null
				&& message.getResourceList().length > 0) {
			Resource receivedResource = new Resource(
					message.getResourceList()[0]);
			// Get the stored root folder from the index
			Resource resource = node.getStorageManager().getRessource("/", "",
					true);
			resource.addResource(receivedResource, message.getSource());
			
			Message sendMsg = Message.free(node.getAddress(), resource, message.getSource());
			Speaker speaker = new Speaker();
			Message result = null;
			int replicationCount = 1;
			// And now find another place to replicate it
			NodeAddress[] list = node.findResourceHost(receivedResource, 2);
			for (int i = 0; i < list.length; i ++) {
				if (list[i] != null && !list[i].equals(message.getSource())) {
					// Send the FREE message to it
					speaker.open(list[i]);
					if (speaker.isConnected()) {
						result = speaker.sendMessage(sendMsg);
						if (result != null && result.isOk()) {
							// The FREE succeeded. So we can add the node to the list of the resource owner
							resource.addResource(receivedResource, list[i]);
							replicationCount++;
						}
						speaker.close();
					}
				}
			} // for
			node.getStorageManager().saveResource(resource);

			return Message.okNewDoc(message.getId(), node.getAddress(), replicationCount);
		} // if 

		return null;
	}

	private Message doFree() {
		Resource resource = new Resource(message.getResourceList()[0]);
		NodeAddress contact = new NodeAddress(message.getNodeList()[0]);
		Message msg = Message.download(node.getAddress(), resource);
		Speaker speaker = new Speaker(contact);
		Message result = speaker.sendMessage(msg);
		speaker.close();
		OutputStream out = node.getStorageManager().addNewResource(resource);
		if (out != null) {
			try {
				out.write(result.getData());
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.throwing(this.getClass().getCanonicalName(), "doFree()", e);
				LOGGER.warning("Cannot save resource to output stream");
				return null;
			}
		}
		return Message.okFree(message.getId(), node.getAddress());
	}

	public boolean replicateFile(Resource ressource) {
		// TODO REPLIC Auto-generated method stub
		NodeAddress[] list = node.getRootOwners();
		int count = list.length;
		if (count < 1) {
			LOGGER.severe("Got an issue : no owners for the root folder !!");
			return false;
		}
		Message message = Message.newDoc(node.getAddress(), ressource);
		Speaker speaker = new Speaker();
		Message result = null;
		for (int i = 0; i < count && result == null; i++) {
			NodeAddress rootNode = list[i];
			if (rootNode != null) {
				speaker.open(rootNode);
				if (speaker.isConnected()) {
					// Ok, we found a root owner available
					// Send the message
					result = speaker.sendMessage(message);
					if (result != null) {
						// We got a result
						if (!result.isOk()) {
							LOGGER.info("Node " + rootNode
									+ " answered with non OK");
							LOGGER.fine(result.toString());
							result = null; // we ignore not positive answer
						} // isok
					} // not null
				} // connected
				speaker.close();
			} // list[i] not null
		} // for

		if (result == null) {
			return false;
		}
		LOGGER.info("Successfully replicated to " + result.getCount() + " nodes (this node included)");
		return true;

	}

}
