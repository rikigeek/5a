package rikigeek.fivea;

import java.io.Serializable;

public class NodeAddress implements Serializable {
	private String ip;
	private int port;
	
	public NodeAddress(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public String getIpAddress() {
		return ip;
	}
	
	public int getTCPPort() {
		return port;
	}
	
	@Override
	public String toString() {
		return String.format("%s:%d", ip, port);
	}
}
