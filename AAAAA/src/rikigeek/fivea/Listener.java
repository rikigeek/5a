package rikigeek.fivea;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import rikigeek.fivea.exception.DispatcherUncaughtExceptionHandler;
import rikigeek.fivea.exception.ExceptionThreadFactory;

public class Listener implements Runnable {
	public final static int DEFAULT_PORT = 20158;
	private int port = DEFAULT_PORT; //Default port number (August 2015!)
	private ThreadPoolExecutor executor; 
	private boolean isStopped = false;
	private Node node;
	
	private static Logger LOGGER = Node.Logger();  
	
	public Listener(Node node) {
		this(node, DEFAULT_PORT);
	}
	
	public Listener(Node node, int tcpPort) {
		this.node = node; 
		// check value of tcp port
		if (tcpPort > 0) {
			port = tcpPort;
		}
		else {
			throw new IllegalArgumentException("tcpPort must be > 0");
		}
		// Build the thread pool
		executor = new ThreadPoolExecutor(2,  3,  5000,  TimeUnit.MILLISECONDS,  new LinkedBlockingQueue<Runnable>());
		// Use my own ExceptionThreadFactory with an home made exceptionHandler.
		executor.setThreadFactory(new ExceptionThreadFactory(new DispatcherUncaughtExceptionHandler()));
		
	}
	
	public int getPort() {
		return port;
	}
	
	private boolean isStopped() {
		return node.isStopped();
	}
	
	private ServerSocket socket;
	public void stop() throws IOException {
		// force the listener to close the server socket, and then exit the thread
		socket.close();
	}
	@Override
	public void run() {
		//ServerSocket socket;
		LOGGER.info("Waiting for a connection on port " + port);
		try {
			socket = new ServerSocket(port);
			while (!isStopped()) { // Infinite loop to wait for new connection
				Socket connection;
				try {
					connection = socket.accept();
					// A connection is made. Start the dispatcher to analyze it and do whatever is needed
					Dispatcher doit = new Dispatcher(node, connection);
					executor.execute(doit); // The Dispatcher thread is enqueued in the pool 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
