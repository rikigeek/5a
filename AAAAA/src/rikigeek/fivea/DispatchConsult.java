package rikigeek.fivea;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.MessageNodeAddress;
import rikigeek.fivea.entities.MessageResource;
import rikigeek.fivea.entities.NodeAddress;
import rikigeek.fivea.storage.Resource;

// Class to dispatch a "CONSULT" message
public class DispatchConsult implements Runnable {
	private static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

	private Message message;
	private Node node;

	public DispatchConsult(Node node) {
		//
		this.node = node;
	}

	public Message receivesMessage(Message message) {
		this.message = message;
		switch (message.getVerb()) {
		case FIND:
			LOGGER.info("Received FIND message");
			return doFind();
		default:
			return null;

		}
	}

	private Message doFind() {
		Resource result = node.getStorageManager().getRessource(message.getFileName(), "", true);
		Message response;
		if (result != null) {
			response = Message.okFind(message.getId(), node.getAddress(), result);
		} else {
			response = Message.noOk(node.getAddress(), message);
			
		}
		return response;
	}
	/**
	 * The node will download a file from the domain to a temporary location
	 * 
	 * @param file
	 *            the name of the file in the domain
	 * @return the Path were the file is stored. Null if an error happened
	 */
	public Path getFile(String file) {
		LOGGER.info("Going to download file " + file + " from the domain");
		try {
			Path destination = Files.createTempFile(node.getStoragePath()
					.resolve("cache"), null, null);
			LOGGER.fine("Storing downloaded file in " + destination);
			NodeAddress[] rootList = node.getRootOwners();
			if (rootList == null) {
				LOGGER.severe("No root owners found");
				return null;
			}
			Message message = Message.find(node.getAddress(), file);
			Speaker speaker = new Speaker();
			Message result = null;
			result = speaker.sendMessageToFirstAvailable(message, rootList);

			if (result == null) {
				LOGGER.warning("Didn't get any valid answer from root folders");
				return null;
			}
			MessageResource[] resourceList = result.getResourceList();
			MessageNodeAddress[] nodeList = result.getNodeList();

			// Last test to check the validity of the answer
			if (resourceList == null || nodeList == null
					|| resourceList.length == 0 || nodeList.length == 0) {
				LOGGER.warning("Didn't get any valid answer : no resource or no node");
				return null;
			}
			LOGGER.fine("We got a valid answer from a root folder owner. Resource to download is "
					+ resourceList[0]);

			// Now send the download message
			message = Message.download(node.getAddress(), resourceList[0]);
			result = speaker.sendMessageToFirstAvailable(message, nodeList);

			if (result == null) {
				LOGGER.warning("Didn't get any valid answer for the DOWNLOAD message");
				return null;
			}

			LOGGER.fine("We have a valid answer for the file " + file);
			// The data is in the result !!
			OutputStream out = Files.newOutputStream(destination,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			out.write(result.getData());
			out.close();
			// Finally send back the path to the cache folder
			return destination;
		} catch (IOException e) {
			LOGGER.throwing(this.getClass().getCanonicalName(), "getFile()", e);
			LOGGER.warning("An error occured while getting the file " + file);
			return null;
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName(
				node.getAddress().getTCPPort() + "-DispachConsult("
						+ Thread.currentThread().getId() + ")");
		LOGGER.finest("DispatchConsult Thread started for receivedMessage #"
				+ message.getId());

		LOGGER.finest("DispatchConsult Thread is stopping");
	}

	public Resource sendDir(String string) {
		Message msg = Message.find(node.getAddress(), "/");
		NodeAddress[] nodeList = node.getRootOwners();
		Speaker speaker = new Speaker();
		Message result = null;
		Resource response = null;
		if (nodeList != null) {
			result = speaker.sendMessageToFirstAvailable(msg, nodeList);
		}
		if (result != null && result.isOk()) {
			if (result.getResourceList() != null
					&& result.getResourceList().length > 0) {
				response = new Resource(result.getResourceList()[0]);
				if (!response.isFolder()) response = null; // We ignore the result if it's not a folder
			}
		}

		return response;
	}

}
