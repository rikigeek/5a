package rikigeek.fivea.entities;

import java.io.Serializable;

public class MessageNodeAddress implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ip;
	private int port;
	private boolean active;
	private int logicalClock;
	
	/**
	 * Build an address for a remote node
	 * @param ip DNS name, or ip address of the remote node
	 * @param port TCP port of the remote node
	 */
	public MessageNodeAddress(String ip, int port) {
		this(ip, port, true, 0);
	}
	
	/**
	 * Build an address for a remote node
	 * @param ip DNS name, or ip address of the remote node
	 * @param port TCP port of the remote node
	 * @param active status of the node:if it's active or not (off)
	 * @param logicalClock value of the logical clock of this nodeAddress entry
	 */
	public MessageNodeAddress(String ip, int port, boolean active, int logicalClock) {
		this.ip = ip;
		this.port = port;
		this.active = active;
		this.logicalClock = logicalClock;
	}
	
	/**
	 * Check if the node is in an active state
	 * @return
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Set the node as an active node
	 */
	public void setActive() {
		active = true;
	}
	/** 
	 * Set the node as an inactive node (the node is not reachable anymore)
	 */
	public void setInactive() {
		active = false;
	}
	
	/**
	 * get the ip or dns name of the remote node
	 * @return
	 */
	public String getIpAddress() {
		return ip;
	}
	
	/**
	 * get the TCP port of the remote node
	 * @return
	 */
	public int getTCPPort() {
		return port;
	}

	/**
	 * get the logical clock value
	 * @return
	 */
	public int getLogicalClock() {
		return logicalClock;
	}
	
	public int increaseLogicalClock() {
		logicalClock++;
		return logicalClock;
	}
	
	public int setLogicalClock(int value) {
		logicalClock = value;
		return logicalClock;
	}
	
	/**
	 * Update this address with the new logical clock and new active state
	 * @param active
	 * @param logicalClock
	 */
	public void update(boolean active, int logicalClock) {
		this.active = active;
		this.logicalClock = logicalClock;
	}
	/**
	 * Compare the logical clocks
	 * @param remoteClock the clock of a remote node
	 * @return +1 if the local clock is more recent
	 *   0 if the two clocks are equals
	 * - 1 if the local clock is out-dated (remote is more recent)
	 */
	public byte compareLogicalClock(int remoteClock) {
		int maxOffset = Integer.MAX_VALUE; // The maximum difference
		
		if (remoteClock == this.logicalClock) {
			return 0; // Clocks are equals
		}
		if (remoteClock - this.logicalClock > maxOffset) {
			// offset is over : it means logical clock made a modulo and is more recent than the remote one
			return +1;
		}
		if (this.logicalClock - remoteClock > maxOffset) {
			// offset is over, it means remote clock made a modulo, and is more recent than the local one
			return -1;
		}
		if (this.logicalClock > remoteClock) {
			return +1; // Local is more recent
		}
		if (this.logicalClock < remoteClock) {
			return -1;
			// Remote one is more recent
		}
		return 0; // code never reached
	}
	
	@Override
	public String toString() {
		return this.getFullName();
	}

	/**
	 * Return the full address of the node
	 * @return
	 */
	public String getFullName() {
		String state = (active?"active":"inactive");
		return String.format("%s:%d - %d (%s)", ip, port, logicalClock, state);
	}
}
