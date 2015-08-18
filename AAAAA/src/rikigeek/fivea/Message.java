package rikigeek.fivea;

import java.io.Serializable;

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	
	public enum Subject {CONFIG, CONNECTION, REPLICATION, CONSULT, RESERVED};
	public enum Verb {NEW, CONNECT, QUIT, INSERT, STOP }
	public boolean question = true; // if it is a request (in opposite to a response)
	
	public Subject subject;
	public Verb verb;
	public NodeAddress sourceNodeAddress;
	public NodeAddress[] childrens;
	public NodeAddress[] brothers;
	public NodeAddress grandFather;
	public NodeAddress father;
	public byte[] data;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Subject : %s, Verb : %s",  subject, verb));
		if (sourceNodeAddress != null) sb.append(String.format(", NodeAddress : %s", sourceNodeAddress));
		if (data != null) sb.append(String.format(", Data : %s", new String(data)));
		return sb.toString();
	}
}
