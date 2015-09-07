package rikigeek.fivea.storage;

import java.io.Serializable;
import java.util.HashSet;

import rikigeek.fivea.entities.MessageNodeAddress;

public class FolderEntry implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String name;
	public int revision;
	public HashSet<MessageNodeAddress> nodes;
	
	public FolderEntry(String name, int revision) {
		this.name = name;
		this.revision = revision;
		nodes = new HashSet<MessageNodeAddress>();
	}
	
	public boolean addNode(MessageNodeAddress node) {
		return nodes.add(node);
	}
	public boolean removeNode(MessageNodeAddress node) {
		return nodes.remove(node);
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (MessageNodeAddress node : nodes) {
			sb.append(String.format("[%s]", node));
		}
		return String.format("%s - %d %s", name, revision, sb.toString());
	}
}
