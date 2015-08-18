package rikigeek.aaaaa.testing;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String[] pArgs = new String[3];
		pArgs[0] = "new";
		pArgs[1] = "test";
		pArgs[2] = "20158";

		NodeTestingThread node1 = new NodeTestingThread(pArgs);
		node1.start();
		Thread.sleep(1000);
		int firstPort = 20159;
		pArgs = new String[2];
		pArgs[0] = "localhost:20158";
		for (int i = 0; i < 10; i++) {
			pArgs[1] = String.format("%d", firstPort + i);
			NodeTestingThread node = new NodeTestingThread(pArgs);
			node.start();
			Thread.sleep(1000);
		}

	}
}
