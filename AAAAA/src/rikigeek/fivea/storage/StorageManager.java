package rikigeek.fivea.storage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.logging.Logger;

import rikigeek.fivea.DispatchConsult;
import rikigeek.fivea.Dispatcher;
import rikigeek.fivea.Node;

/**
 * Class to organize and to interface with the local storage of resources
 * 
 * @author Rikigeek
 *
 */
public class StorageManager {
	static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

	private boolean initialized = false;
	private boolean loaded = false;
	private Path storagePath;
	private Path indexFile;
	private StorageIndex index;
	private Node node;

	/**
	 * Initialize the storage manager, and load index
	 * @param node the Node instance we are running on
	 * @param path the storage path for the node
	 */
	public StorageManager(Node node, String path) {
		this.storagePath = Paths.get(path);
		this.node = node;

		if (!Files.isDirectory(storagePath)) {
			LOGGER.info("Storage Path is not a directory.. Trying to initialize storage");
			if (!initializeStorage()) {
				// If we can't initialize the storage, we cancel the load, and
				// request to exit the program
				initialized = false;
				return;
			}
			initialized = true;

		} else {
			// the folder already exists
			initialized = true;
		}
		loaded = loadIndex();
	}

	/** 
	 * Dump the index to the disk
	 * @return false if an error happened
	 */
	boolean saveIndex() {
		// save the index file to the file system
		if (Files.exists(indexFile)) {
			if (!Files.isRegularFile(indexFile) || !Files.isWritable(indexFile)) {
				// The index file is not a regular or not writable. Exiting
				LOGGER.severe("Cannot write the index file ("
						+ indexFile.toAbsolutePath() + ").");
				return false;
			}
		}
		// Really save the index
		try (ObjectOutputStream out = new ObjectOutputStream(
				Files.newOutputStream(indexFile, StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING))) {
			out.writeObject(index);
		} catch (IOException e) {
			LOGGER.throwing(this.getClass().getName(), "saveIndex()", e);
			LOGGER.severe("Unable to save Content of index file : "
					+ indexFile.toAbsolutePath());
			return false;
		}
		return true;
	}

	/**
	 * Save a resource in the local storage
	 * @param resource
	 * @return
	 */
	public boolean saveResource(Resource resource) {
		if (resource == null){
			LOGGER.warning("If you want me to store a resource, it must not be null !");
			return false;
		}
		if (resource.isFolder()) {
			// Add the resource to the index, and save the index
			if (index.addResource(resource)) {
				LOGGER.info("Folder Resource " + resource + " stored in the index");
				saveIndex();
			}
		} else {
			Path location = storagePath.resolve(resource.getPhysicalPath()).toAbsolutePath();
			Resource localFile = index.findResourceByLocation(location, storagePath);
			if (localFile == null) {
				// location can be erased
			} else {
				if (localFile.equals(resource)) {
					// Resource are also the same. No need to store it
					LOGGER.info("This resource " + resource + " is already saved in the storage manger");
					return false;
				}
			}
			// Check location
			Resource local = index.getRessource(resource.getName(), resource.getFolder(), resource.isFolder());
			if (local != null) {
				if (resource.getRevision() > local.getRevision()) {
					// local is not the most recent
					index.addResource(resource);
				}
			}
		}
		return true;
	}
	/**
	 * Load index from the local file
	 * If local file doesn't exists, create a new index file
	 * @return false if an error happened
	 */
	boolean loadIndex() {
		// Load the index file, and eventually check the local file system
		try {
			indexFile = storagePath.resolve(".index");
		} catch (InvalidPathException e) {
			LOGGER.throwing(this.getClass().getName(), "loadIndex", e);
			LOGGER.severe("Unable to access to the index file. Exiting program");
			return false;
		}
		if (Files.exists(indexFile)) {
			if (!Files.isRegularFile(indexFile) || !Files.isWritable(indexFile)) {
				// The index file is not a regular or not writable. Exiting
				LOGGER.severe("Cannot write the index file ("
						+ indexFile.toAbsolutePath() + "). Exiting program");
				return false;
			}

			// Really load the index
			try (ObjectInputStream in = new ObjectInputStream(
					Files.newInputStream(indexFile, StandardOpenOption.READ))) {
				Object obj = in.readObject();
				if (obj.getClass().equals(StorageIndex.class)) {
					// OK, we got the StorageIndex
					index = (StorageIndex) obj;
				}
			} catch (IOException e) {
				LOGGER.throwing(this.getClass().getName(), "loadIndex", e);
				LOGGER.severe("Unable to load Content of index file. Exiting the program : "
						+ indexFile.toAbsolutePath());
				return false;

			} catch (ClassNotFoundException e) {
				LOGGER.throwing(this.getClass().getName(), "loadIndex", e);
				LOGGER.severe("Index file is corrupted... Exiting the program : "
						+ indexFile.toAbsolutePath());
				return false;
			}
		} else {
			// File doesn't exist, we create a new index file
			index = new StorageIndex();
		}

		return true;
	}

	/**
	 * Is the Storage manager initialized (storage path is initialized and available)
	 * @return
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/** 
	 * Is the index loaded
	 * @return
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * Create a new resource. Doesn't add it to the index file.
	 * To add it to the index file and to store the resource in file system,
	 * call the addNewResource method
	 * @param name Name of the file or folder to create
	 * @param parentFolder Domain folder to store the ressource into 
	 * @param isFolder True to create a folder, or false to create a regular file
	 * @return the resource created. 
	 */
	public Resource createNewRessource(String name, String parentFolder, boolean isFolder) {
		Resource resource = new Resource(name, 0, parentFolder, isFolder);
		index.addResource(resource);
		return resource;
	}

	/**
	 * Add a new resource to the local storage.
	 * Index file is updated, and a storage is allocated
	 * @param resource the resource to store
	 * @return an allocated output stream. Just write into it. 
	 * Don't forget to close it when you finish
	 */
	public OutputStream addNewResource(Resource resource) {
		// add the resource in the index file, and return an outputStream to
		// write the resource content to
		Path location = storagePath.resolve(resource.getPhysicalPath());
		if (Files.exists(location)) {
			// No luck, or internal issue
			LOGGER.severe("Cannot store resource " + resource + " to the following location: file already exists ! " + location.toAbsolutePath());
			return null;
		}
		index.addResource(resource);
		saveIndex();
		OutputStream out;
		try {
			out = Files.newOutputStream(location, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.throwing(this.getClass().getCanonicalName(), "addNewResource()", e);
			LOGGER.severe("Unable to store resource " + resource + " to the following location : " + location);
			return null;
		}
		return out;
	}
	
	/**
	 * To find the local storage path
	 * @return
	 */
	public Path getStoragePath() {
		return this.storagePath;
	}

	/**
	 * initialize the storage location : check if folder exists, if we can write in it...
	 * Doesn't load the index file
	 * @return true if everything is correct
	 */
	boolean initializeStorage() {
		if (Files.notExists(storagePath)) {
			try {
				Files.createDirectory(storagePath);
				LOGGER.info("storage path created : "
						+ storagePath.toRealPath());
			} catch (IOException e) {
				LOGGER.throwing(this.getClass().getName(), "initializeStorage",
						e);
				LOGGER.severe("Unable to initialize the storage folder "
						+ storagePath.toAbsolutePath() + ". Exiting program");
				return false;
			}
			return true;
		} else {
			LOGGER.severe(storagePath.toAbsolutePath()
					+ " already exists, but is not a directory. Exiting program");
			return false;
		}
	}


	/** 
	 * get a file from the domain
	 * @param localFileName
	 * @param domainFileName
	 * @return
	 * @throws FileAlreadyExistsException
	 * @throws InvalidPathException
	 * @throws AccessDeniedException
	 * @throws IOException
	 */
	public boolean getFile(Path localFileName, String domainFileName)
			throws FileAlreadyExistsException, InvalidPathException,
			AccessDeniedException, IOException {
		// - First we check the destination file
		if (Files.exists(localFileName)) {
			LOGGER.warning("Could not get the file " + domainFileName
					+ " : destination already exists (" + localFileName + ")");
			throw new FileAlreadyExistsException(localFileName.toString());
		}

		// - Find the file in the domain
		Path tempLocation = null;
		Resource file = this.getRessource(domainFileName, "/", false);
		if (file != null) {
			// The file is stored locally
			tempLocation = file.getPhysicalPath();
		} else {
			// - Get a local copy
			DispatchConsult dConsult = new DispatchConsult(node);
			tempLocation = dConsult.getFile(domainFileName);
		}
		if (tempLocation != null) {
			// Local copy in cache is done
			// - Allocate the destination
			OutputStream os = Files.newOutputStream(localFileName,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			// - Copy the file
			Files.copy(tempLocation, os);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get a resource from its name, domain folder, and if it's a folder. It looks into the index
	 * @param name
	 * @param folder
	 * @param isFolder
	 * @return
	 */
	public Resource getRessource(String name, String folder, boolean isFolder) {
		// TODO Auto-generated method stub
		return index.getRessource(name, folder, isFolder);
	}
	
	public HashSet<Resource> getIndex() {
		return index.index;
	}
}
