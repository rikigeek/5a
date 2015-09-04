package rikigeek.fivea.entities;

public class NodeAddress extends MessageNodeAddress implements Comparable<NodeAddress>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int hashValue;
	
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
		return this.hashCode() - o.hashCode();
	}
	
	 @Override
	 public int hashCode() {
		 // TODO : return the hashvalue cache ?
		 int hash = 1;
		 hash = hash * 17 + this.getTCPPort();
		 hash = hash * 34 + this.getIpAddress().hashCode();
		 return hash;
	 }

}
