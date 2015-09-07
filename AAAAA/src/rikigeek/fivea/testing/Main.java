package rikigeek.fivea.testing;

import java.nio.file.*;

public class Main {
	

	public static void main(String[] args) throws Exception {
		Thread.currentThread().setName(
				"TestingMain(" + Thread.currentThread().getId() + ")");
		
		Path p = Paths.get("C:\\CNAM\\DATA\\tmp");
		
		System.out.println(p.toAbsolutePath());
		for (Path s : p) {
			System.out.println(s);
		}
		
		if (Files.isDirectory(p)) {
			System.out.println("répertoire ok");
		}
		else {
			System.out.println("répertoire n'existe pas ");
			Files.createDirectory(p);
		}
		
		p = Paths.get("/cnam/data/1");
		
		System.out.println(p.toAbsolutePath());
		System.out.println(p.toRealPath());
		
		
		
		test(args);
		
		return;
	}
	public static void test(String[] args) throws Exception {
		String[] pArgs = new String[4];
		pArgs[0] = "new";
		pArgs[1] = "test";
		pArgs[2] = "20158";
		pArgs[3] = "C:\\CNAM\\DATA\\MAIN";

		NodeTestingThread nodes[] = new NodeTestingThread[50];

		NodeTestingThread node1 = new NodeTestingThread(pArgs);
		node1.start();
		Thread.sleep(1000);
		int firstPort = 20159;
		pArgs = new String[3];
		pArgs[0] = "localhost:20158";
		for (int i = 0; i < 5; i++) {
			pArgs[1] = String.format("%d", firstPort + i);
			pArgs[2] = String.format("C:\\CNAM\\DATA\\%d", i);
			NodeTestingThread node = new NodeTestingThread(pArgs);
			node.start();
			nodes[i] = node;
			Thread.sleep(1000);
		}

		

		// MessageNodeAddress[] list;
		// ConcurrentSkipListSet<NodeAddress> domainNodeList = new
		// ConcurrentSkipListSet<NodeAddress>();
		// domainNodeList.add(new NodeAddress("localhost", 10));
		// list = (MessageNodeAddress[]) domainNodeList.toArray(new
		// NodeAddress[0]);
		// for (int i = 0; i < list.length; i++) System.out.println(list[i]);
		// domainNodeList.add(new NodeAddress("localhost", 11));
		// list = (MessageNodeAddress[]) domainNodeList.toArray(new
		// NodeAddress[0]);
		//
		// for (int i = 0; i < list.length; i++) System.out.println(list[i]);

		System.out.printf("%s %s RETURN", Main.class.getCanonicalName(),
				"static main(..)");

	}
}
