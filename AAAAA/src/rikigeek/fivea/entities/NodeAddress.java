package rikigeek.fivea.entities;

import rikigeek.fivea.Node;

public class NodeAddress extends MessageNodeAddress implements Comparable<NodeAddress>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int hashValue;
	protected byte failureConnectCount = 0;
	
	public NodeAddress(String ip, int port) {
		super(ip, port);
		hashValue = this.hashCode();
	}
	public NodeAddress(String ip, int port, boolean active, int logicalClock) {
		super(ip, port, active, logicalClock);
		// localy save the hashcode (to prevent recalculation ?)
		hashValue = this.hashCode();
	}

	public NodeAddress(MessageNodeAddress nodeAddress) {
		this(nodeAddress.getIpAddress(), nodeAddress.getTCPPort(), nodeAddress.isActive(), nodeAddress.getLogicalClock());
	}
	@Override
	public int compareTo(NodeAddress o) {
		if (o == null) return 1;
		return this.hashCode() - o.hashCode();
	}
	public boolean equals(NodeAddress o) {
		if (o == null) return false;
		return this.hashCode() == o.hashCode();
	}
	 @Override
	 public int hashCode() {
		 // TODO CONNECT return the hashvalue cache ?
		 int hash = 1;
		 hash = hash * 17 + this.getTCPPort();
		 hash = hash * 34 + this.getIpAddress().hashCode();
		 return hash;
	 }

	 public void increaseConnectFailure() {
		 failureConnectCount++;
		 if (failureConnectCount > Node.getMaxFailureConnect()) {
			 this.setInactive();
		 }
	 }
	 
	 public void setActive() {
		 super.setActive();
		 // Also reset the failure connection count
		 this.failureConnectCount = 0;
	 }
	public byte getConnectFailure() {
		return this.failureConnectCount;
	}
}
