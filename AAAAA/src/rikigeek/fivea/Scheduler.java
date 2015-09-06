package rikigeek.fivea;

import java.util.logging.Logger;

import rikigeek.fivea.entities.Message;
import rikigeek.fivea.entities.NodeAddress;

/**
 * This class execute all periodic tasks, as for example the checking
 * neighborhood
 * 
 * @author Rikigeek
 *
 */
public class Scheduler implements Runnable {

	private static Logger LOGGER = Logger.getLogger(Scheduler.class.getName());

	// The current node
	private Node node;

	public Scheduler(Node node) {
		this.node = node;
	}

	@Override
	public void run() {
		Thread.currentThread().setName(
				node.getAddress().getTCPPort() + "-Scheduler("
						+ Thread.currentThread().getId() + ")");
		while (!node.isStopped()) {
			// Do the scheduled stuff
			LOGGER.finest("Doing stuff");

			// Check status of previous neighbor
			NodeAddress previous = node.getPreviousNeighbor();
			checkNeighbors(previous);
			// Check status of next neighbor, only if not the same as previous
			// (case of a domain with only 2 active nodes)
			NodeAddress next = node.getNextNeighbor();
			if (next != null && !next.equals(previous))
				checkNeighbors(next);

			//TODO : check the neighbor states, and initiate a replication if neighbors are inactive
			
			// Sleep for the configured value
			try {
				Thread.sleep(node.getSchedulerPeriod());
			} catch (InterruptedException e) {
				// Nothing, just wake up and do the next loop
				LOGGER.info("Has been waked up");
			}
		}
		LOGGER.info("Exiting Scheduler");
	}

	/**
	 * Send a CHECK message to the neighbor. Also send them the local list of nodes
	 * The neighbor active state will be updated if connection still fails
	 * @param neighbor
	 * @return true if node has been contacted.
	 */
	protected boolean checkNeighbors(NodeAddress neighbor) {
		LOGGER.info("Checking neighbor " + neighbor);
		boolean result = false;
		Speaker speaker = new Speaker();

		// Get the local list of nodes
		NodeAddress list[];
		// small trick : initialize the array with an empty NodeAddress array,
		// so that list will never be null
		list = node.getDomainNodeList().toArray(new NodeAddress[0]);

		Message check = Message.checkNode(node.getAddress(), list);
		Message response;
		if (neighbor != null) {
			speaker.open(neighbor);
			if (speaker.isConnected()) {
				response = speaker.sendMessage(check);
				speaker.close();
				if (!response.isOk()) {
					LOGGER.info("Neighbor " + neighbor + " is stopping");
					neighbor.setInactive();
					result = false;
				} else {
					// We confirm this node is active (that also reset the
					// failure count)
					LOGGER.info("Neighbor " + neighbor + " is alive");
					neighbor.setActive();
					result = true;
				}
			} else {
				// Node could not be contacted.
				// Set the timer to declare it out of the domain
				LOGGER.info("Neighbor " + neighbor
						+ " could not be contacted. Increase is count to "
						+ neighbor.getConnectFailure());
				neighbor.increaseConnectFailure();
				result = false;
			}
		}
		LOGGER.fine("Neighbor " + neighbor + " checking result in " + result);
		return result;
	}

}
