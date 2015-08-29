package rikigeek.fivea.entities;

public class NodeAddress extends MessageNodeAddress implements Comparable<NodeAddress>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int hashValue;
	
	public NodeAddress(String ip, int port) {
		super(ip, port);
		// TODO Auto-generated constructor stub
		hashValue = this.hashCode();
	}

	public NodeAddress(MessageNodeAddress nodeAddress) {
		this(nodeAddress.getIpAddress(), nodeAddress.getTCPPort());
	}
	@Override
	public int compareTo(NodeAddress o) {
		return this.hashCode() - o.hashCode();
	}
	
	 @Override
	 public int hashCode() {
		 return this.toString().hashCode();
	 }

}
