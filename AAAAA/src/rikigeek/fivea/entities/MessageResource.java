package rikigeek.fivea.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

import rikigeek.fivea.storage.FolderEntry;

public class MessageResource implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected int resourceId; // The hash of the resource
	protected String name;
	protected String parent;
	protected boolean isFolder;
	protected int revision; // version of the document
	protected int size;
	protected Date lastAccessTime;
	protected Date creationTime;
	protected Date lastModifiedTime;
	protected HashSet<FolderEntry> entries;
	
	protected MessageNodeAddress[] lastKnownFolderNodes;
	
	public String getName() {
		return name;
	}
	public int getRevision() {
		return revision;
	}
	public String getFolder() {
		if (parent == null) {
			if (!isFolder) 
				return "/";
			else 
				return "";
		}
		else {
			return parent.toString();
		}
	}
	
	public MessageResource(String name, int revision, String location, boolean folder) {
		this.name = name;
		this.revision = revision;
		this.parent = location;
		this.isFolder = folder;
		
		if (isFolder) {
			// Creation of a folder
			entries = new HashSet<FolderEntry>();
		}
	}

	public MessageResource(MessageResource messageResource) {
		this.resourceId = messageResource.resourceId;
		this.name = messageResource.name;
		this.parent = messageResource.parent;
		this.isFolder = messageResource.isFolder;
		this.revision = messageResource.revision;
		this.size = messageResource.size;
		this.lastAccessTime = messageResource.lastAccessTime;
		this.creationTime = messageResource.creationTime;
		this.lastModifiedTime = messageResource.lastModifiedTime;
		this.entries = messageResource.entries;
		this.lastKnownFolderNodes = messageResource.lastKnownFolderNodes;
		
	}
	
	
}
