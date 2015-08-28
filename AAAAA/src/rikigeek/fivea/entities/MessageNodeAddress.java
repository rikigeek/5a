package rikigeek.fivea.entities;

import java.io.Serializable;

public class MessageNodeAddress implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ip;
	private int port;
	
	/**
	 * Build an address for a remote node
	 * @param ip DNS name, or ip address of the remote node
	 * @param port TCP port of the remote node
	 */
	public MessageNodeAddress(String ip, int port) {
		this.ip = ip;
		this.port = port;
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
	
	@Override
	public String toString() {
		return this.getFullName();
	}

	/**
	 * Return the full address of the node
	 * @return
	 */
	public String getFullName() {
		return String.format("%s:%d", ip, port);
	}
}
