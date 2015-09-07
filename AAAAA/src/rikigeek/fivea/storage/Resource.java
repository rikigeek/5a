package rikigeek.fivea.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import rikigeek.fivea.entities.MessageNodeAddress;
import rikigeek.fivea.entities.MessageResource;

public class Resource extends MessageResource implements Comparable<Resource> {

	private String physicalPath;

	public Path getPhysicalPath() {
		return Paths.get(physicalPath);
	}
	public Resource(String name, int revision, String location, boolean folder) {
		super(name, revision, location, folder);
		if (!folder) {
			this.physicalPath = "F" + this.hashCode() + ".bin";
		}
	}

	public Resource(MessageResource messageResource) {
		super(messageResource);
		if (!this.isFolder()) {
			this.physicalPath = "F" + this.hashCode() + ".bin";
		}
	}
	
	/** 
	 * find the location of a file
	 * @param fileName
	 * @return an array of the node address that store this file
	 */
	public MessageNodeAddress[] findFile(String fileName) {
		if (!isFolder()) return null; // Not a folder, so nothing to find
		
		for (FolderEntry entry : entries) {
			if (entry.name.equals(fileName)) {
				StorageManager.LOGGER.fine("found " + fileName + " in this folder " + this.toString());
				return entry.nodes.toArray(new MessageNodeAddress[0]);
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 17 + this.getName().hashCode();
		hash = hash * 34 + this.getRevision();
		hash = hash * 11 + this.getFolder().hashCode(); 
		return hash;
	}
	
	public boolean equals(Resource res) {
		if (res == null) return false;
		return this.hashCode() == res.hashCode();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean addResource(Resource receivedResource, MessageNodeAddress node) {
		for (FolderEntry entry : entries) {
			if (entry.name == receivedResource.getName() && entry.revision == receivedResource.getRevision()) {
				// The resource already exist.
				return entry.nodes.add(node);
			}
		}
		FolderEntry entry = new FolderEntry(receivedResource.getName(), receivedResource.getRevision());
		if (!entry.nodes.add(node)) return false;
		return entries.add(entry);
	}

	public boolean isFolder() {
		return isFolder;
	}
	@Override
	public int compareTo(Resource o) {
		if (o == null) return 1;
		return this.hashCode() - o.hashCode();
	}

	public String toString() {
		String type = (isFolder?"D":" ");
		return String.format("[%s] %s%s (REV %d)", type, this.parent, this.name, this.revision);
	}
	
	public FolderEntry[] getEntries() {
		return entries.toArray(new FolderEntry[0]);
	}
}
