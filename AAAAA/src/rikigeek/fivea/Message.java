package rikigeek.fivea;

import java.io.Serializable;

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	
	public enum Subject {CONFIG, CONNECTION, REPLICATION, CONSULT, RESERVED};
	public enum Verb {OK, NOK, NEW, CONNECT, QUIT, INSERT, STOP, PERTEELEC, PERTERESULELEC, PERTECONNECT, TREECONNECT, TREEWEIGHT, BROTHER, DISCONNECT, REQINS, WAKEUP }
	public boolean question = true; // if it is a request (in opposite to a response)
	
	protected Subject subject;
	protected Verb verb;
	protected NodeAddress sourceNodeAddress;
	protected NodeAddress[] childrens;
	protected NodeAddress[] brothers;
	protected NodeAddress grandFather;
	protected NodeAddress father;
	protected byte[] data;
		
	private Message(NodeAddress source, Subject subject, Verb verb, boolean question) {
		this.sourceNodeAddress = source;
		this.subject = subject;
		this.verb = verb;
		this.question = question;
	}

	/**
	 * Message NEW
	 * @param source
	 * @param contact
	 * @return
	 */
	public static Message newNode(NodeAddress source, NodeAddress contact) {
		Message m = new Message(source, Subject.CONNECTION,  Verb.NEW, true);
		m.data = contact.toString().getBytes();
		return m;
	}
	/**
	 * Response of message NEW
	 * @param source
	 * @param father
	 * @return
	 */
	public static Message okNew(NodeAddress source, NodeAddress father) {
		Message m = new Message(source, Subject.CONNECTION, Verb.OK, false);
		m.father = father;
		return m;
	}
	/**
	 * Message REQINS
	 * @param source
	 * @param dest
	 * @return
	 */
	public static Message reqIns(NodeAddress source, NodeAddress dest) {
		Message m = new Message(source, Subject.CONNECTION, Verb.REQINS, true);
		m.father = dest;
		return m;
	}
	/**
	 * Answer of a message REQINS
	 * @param source
	 * @param father
	 * @return
	 */
	public static Message okReqIns(NodeAddress source, NodeAddress father) {
		Message m = new Message(source, Subject.CONNECTION, Verb.REQINS, false);
		m.father = father;
		return m;
	}
	/**
	 * Message CONNECT
	 * @param source
	 * @param father
	 * @return
	 */
	public static Message connect(NodeAddress source, NodeAddress father) {
		Message m = new Message(source, Subject.CONNECTION, Verb.CONNECT, true);
		m.father = father;
		return m;
	}
	/** 
	 * Response of message CONNECT
	 * @param source
	 * @param father
	 * @param brothers
	 * @param grandFather
	 * @return
	 */
	public static Message okConnect(NodeAddress source, NodeAddress father, NodeAddress[] brothers, NodeAddress grandFather) {
		Message m = new Message(source, Subject.CONNECTION, Verb.OK, false);
		m.father = father;
		m.brothers = brothers;
		m.grandFather = grandFather;
		return m;
	}
	/**
	 * Message QUIT
	 * @param source
	 * @param father
	 * @return
	 */
	public static Message quit(NodeAddress source, NodeAddress father) {
		// Don't expect for an answer
		Message m = new Message(source, Subject.CONNECTION, Verb.QUIT, false);
		m.father = father;
		return m;
	}
	/**
	 * Message INSERT
	 * @param source
	 * @param father
	 * @return
	 */
	public static Message insert(NodeAddress source, NodeAddress father) {
		Message m = new Message(source, Subject.CONNECTION, Verb.INSERT, true);
		m.father = father;
		return m;
	}
	/**
	 * Response of message INSERT
	 * @param source
	 * @param father
	 * @return
	 */
	public static Message okInsert(NodeAddress source, NodeAddress father) {
		Message m = new Message(source, Subject.CONNECTION, Verb.OK, false);
		m.father = father;
		return m;
	}
	/**
	 * Message STOP
	 * @param source
	 * @param data
	 * @return
	 */
	public static Message stop(NodeAddress source, byte[] data) {
		Message m = new Message(source, Subject.CONNECTION, Verb.STOP, false);
		m.data = data;
		return m;
	}
	public static Message wakeup(NodeAddress source, NodeAddress destination) {
		Message m = new Message(source, Subject.CONNECTION, Verb.WAKEUP, true);
		m.father = destination;
		return m;
	}
	public static Message okWakeup(NodeAddress source, NodeAddress father) {
		Message m = new Message(source, Subject.CONNECTION, Verb.OK, false);
		m.father = father;
		return m;
	}
	
	public boolean isOk() {
		return this.verb == Verb.OK;
	}
	public boolean isNok() {
		return this.verb == Verb.NOK;
	}
	
	public NodeAddress getSource() {
		return sourceNodeAddress;
	}
	
	public NodeAddress[] getChildrens() {
		return childrens;
	}
	
	public NodeAddress[] getBrothers() {
		return brothers;
	}
	
	public NodeAddress getGrandFather() {
		return grandFather;
	}
	
	public NodeAddress getFather() {
		return father;
	}
	
	public NodeAddress getElecWinner() {
		return father;
	}
	
	public byte[] getData() {
		return data;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Subject : %s, Verb : %s",  subject, verb));
		if (sourceNodeAddress != null) sb.append(String.format(", NodeAddress : %s", sourceNodeAddress));
		if (data != null) sb.append(String.format(", Data : %s", new String(data)));
		return sb.toString();
	}

}
