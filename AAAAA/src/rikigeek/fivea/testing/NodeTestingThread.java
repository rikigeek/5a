package rikigeek.fivea.testing;

public class NodeTestingThread extends Thread {
	String[] sArgs;
	public NodeTestingThread(String[] args) {
		sArgs = args;
		
	}
	public void run() {
		try {
			rikigeek.fivea.Main.main(sArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
