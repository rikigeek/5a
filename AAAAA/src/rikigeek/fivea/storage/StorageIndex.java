package rikigeek.fivea.storage;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashSet;

/**
 * Class that represent the index file
 * 
 * @author Rikigeek
 *
 */
public class StorageIndex implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	HashSet<Resource> index;

	public StorageIndex() {
		// Initialize the storage index
		index = new HashSet<Resource>();
	}

	public boolean addResource(Resource resource) {
		return index.add(resource);

	}
	
	public Resource findResourceByLocation(Path location, Path storagePath) {
		location = location.toAbsolutePath();
		for (Resource res : index) {
			// Build the absolute path of the resource res
			Path resLocation = storagePath.resolve(res.getPhysicalPath()).toAbsolutePath();
			// compare both
			if (resLocation.compareTo(location) == 0) { 
				// They are the same
				return res;
			}
		}
		return null;
	}
	
	public boolean removeResource(Resource resource) {
		return index.remove(resource);
	}

	/** 
	 * Get the latest revision of the resource stored locally
	 * @param name name of the file/folder
	 * @param folder parent folder 
	 * @param isFolder true if resource is a folder
	 * @return the latest resource
	 */
	public Resource getRessource(String name, String folder, boolean isFolder) {
		Resource result = null;
		for (Resource res : index) {
			if (res != null) {
				if (res.getName().equals(name)
						&& res.getFolder().equals(folder)
						&& res.isFolder() == isFolder) {
					// We found the resource
					if (result == null) {
						result = res;
					} else {
						// Check if it's the latest one
						if (result.getRevision() < res.getRevision()) {
							result = res;
						}
					}
				}
			}
		}
		return result;
	}

}
