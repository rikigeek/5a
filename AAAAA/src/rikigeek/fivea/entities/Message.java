package rikigeek.fivea.entities;

import java.io.Serializable;

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	public enum Subject {
		CONFIG, CONNECTION, REPLICATION, CONSULT, RESERVED
	};

	// public enum Verb {OK, NOK, NEW, CONNECT, QUIT, INSERT, STOP, PERTEELEC,
	// PERTERESULELEC, PERTECONNECT, TREECONNECT, TREEWEIGHT, BROTHER,
	// DISCONNECT, REQINS, WAKEUP }
	public enum Verb {
		ADDNODE, UPDLIST, QUIT, CHECKNODE, FIND, GETDOC, NEWDOC, REPLICATEDOC, GETNODELIST, STOP
	}

	public enum Answer {
		OK, NOK, NO, YES
	};

	protected int id;
	/**
	 * Answer or answer waited: in an answer, either OK or NOK (not OK) in a
	 * question, either YES if an answer is requested, or NO if no answer is
	 * requested
	 */
	protected Answer answer;
	/**
	 * Message type (which function)
	 */
	protected Subject subject;
	/**
	 * The Message name
	 */
	protected Verb verb;
	/**
	 * Address of the source node
	 */
	protected MessageNodeAddress sourceNodeAddress;
	protected String domainName;
	protected MessageNodeAddress[] domainListNode;
	protected MessageRessource[] ressourceList;
	protected byte[] data;

	private Message(MessageNodeAddress source, Subject subject, Verb verb,
			Answer answer) {
		this.id = new java.util.Date().hashCode();
		this.sourceNodeAddress = source;
		this.subject = subject;
		this.verb = verb;
		this.answer = answer;
	}

	/**
	 * Change the answer value (if it's a question, do we wait for an answer, if
	 * it's an answer, is it ok or not)
	 * 
	 * @param answer
	 */
	public void setAnswer(Answer answer) {
		this.answer = answer;
	}

	/**
	 * Get the message Id
	 * 
	 * @return
	 */
	public int getId() {
		return this.id;
	}

	public static Message addNode(MessageNodeAddress source) {
		Message m = new Message(source, Subject.CONNECTION, Verb.ADDNODE,
				Answer.YES);
		return m;
	}

	public static Message okAddNode(int messageId, MessageNodeAddress source,
			MessageNodeAddress[] list, String domainName) {
		Message m = new Message(source, Subject.CONNECTION, Verb.ADDNODE,
				Answer.OK);
		m.id = messageId;
		m.domainListNode = list;
		m.domainName = domainName;
		return m;
	}

	public static Message updList(MessageNodeAddress source,
			MessageNodeAddress[] list) {
		Message m = new Message(source, Subject.CONNECTION, Verb.UPDLIST,
				Answer.NO);
		m.domainListNode = list;
		return m;
	}

	public static Message quit(MessageNodeAddress source,
			MessageRessource[] ressourceList) {
		Message m = new Message(source, Subject.CONNECTION, Verb.QUIT,
				Answer.NO);
		m.ressourceList = ressourceList;
		return m;
	}

	public static Message stop(MessageNodeAddress source, byte[] data) {
		Message m = new Message(source, Subject.CONNECTION, Verb.QUIT,
				Answer.NO);
		m.data = data;
		return m;
	}

	public static Message checkNode(MessageNodeAddress source, MessageNodeAddress list[]) {
		Message m = new Message(source, Subject.CONNECTION, Verb.CHECKNODE,
				Answer.YES);
		m.domainListNode = list;
		return m;
	}

	public static Message okCheckNode(int messageId, MessageNodeAddress source) {
		Message m = new Message(source, Subject.CONNECTION, Verb.CHECKNODE,
				Answer.OK);
		m.id = messageId;
		return m;
	}

	// ADDNODE, UPDLIST, QUIT, CHECKNODE, FIND, GETDOC, NEWDOC, REPLICATEDOC
	public static Message find(MessageNodeAddress source,
			MessageRessource ressource) {
		Message m = new Message(source, Subject.REPLICATION, Verb.FIND,
				Answer.YES);
		m.ressourceList = new MessageRessource[1];
		m.ressourceList[0] = ressource;
		return m;
	}

	public static Message getNodeList(MessageNodeAddress source) {
		Message m = new Message(source, Subject.CONFIG, Verb.GETNODELIST,
				Answer.YES);
		return m;
	}

	public static Message okGetNodeList(int messageId,
			MessageNodeAddress source, MessageNodeAddress[] nodeList) {
		Message m = new Message(source, Subject.CONFIG, Verb.GETNODELIST,
				Answer.OK);
		m.id = messageId;
		m.domainListNode = nodeList;
		return m;
	}
	
	public static Message doStop(MessageNodeAddress source) {
		Message m= new Message(source, Subject.CONFIG, Verb.STOP, Answer.NO);
		return m;
	}

	/**
	 * Default Error Message we can return to the sender
	 * 
	 * @param source
	 *            the local node address
	 * @param receivedMessage
	 *            the received message that was expecting to a response but
	 *            doesn't have one
	 * @return
	 */
	public static Message noOk(MessageNodeAddress source,
			Message receivedMessage) {
		Message m = new Message(source, receivedMessage.getSubject(),
				receivedMessage.getVerb(), Answer.NOK);
		m.id = receivedMessage.getId();
		return m;
	}

	/**
	 * Default validation message. Used when no specific information is expected. Can be send from the remote node, or from the loca node
	 * @param source
	 * @param receivedMessage the message we want to confirm the correct execution
	 * @return
	 */
	public static Message ok(MessageNodeAddress source, Message receivedMessage) {
		Message m = new Message(source, receivedMessage.getSubject(),
				receivedMessage.getVerb(), Answer.OK);
		m.id = receivedMessage.getId();
		return m;
	}

	/**
	 * Is the message an answer ? If not, it's a question, waiting (or not) for
	 * an answer
	 * 
	 * @return
	 */
	public boolean isAnswer() {
		return this.answer == Answer.OK || this.answer == Answer.NOK;
	}

	/**
	 * Is the message an OK answer ?
	 * 
	 * @return
	 */
	public boolean isOk() {
		return this.answer == Answer.OK;
	}

	/**
	 * Does the message need an answer ?
	 * 
	 * @return
	 */
	public boolean needAnswer() {
		return this.answer == Answer.YES;
	}

	/**
	 * Get the verb of the message (the message name)
	 * 
	 * @return
	 */
	public Verb getVerb() {
		return this.verb;
	}

	/**
	 * Get the subject of the message (the type of the message - the function)
	 * 
	 * @return
	 */
	public Subject getSubject() {
		return this.subject;
	}

	/**
	 * Get source node of the message
	 * 
	 * @return
	 */
	public MessageNodeAddress getSource() {
		return sourceNodeAddress;
	}

	/**
	 * Get domain name of the message
	 * 
	 * @return
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * Get list of nodes of the network
	 * 
	 * @return
	 */
	public MessageNodeAddress[] getNodeList() {
		return this.domainListNode;
	}

	/**
	 * Get list of resources available on the source node
	 * 
	 * @return
	 */
	public MessageRessource[] getRessourceList() {
		return this.ressourceList;
	}

	/**
	 * Get default data field (multi purpose)
	 * 
	 * @return
	 */
	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Subject : %s, Verb : %s", subject, verb));
		if (sourceNodeAddress != null)
			sb.append(String.format(", NodeAddress : %s", sourceNodeAddress));
		if (data != null)
			sb.append(String.format(", Data : %s", new String(data)));
		return sb.toString();
	}

}
