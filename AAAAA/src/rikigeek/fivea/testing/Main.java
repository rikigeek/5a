package rikigeek.fivea.testing;

import java.util.concurrent.ConcurrentSkipListSet;

import rikigeek.fivea.entities.MessageNodeAddress;
import rikigeek.fivea.entities.NodeAddress;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String[] pArgs = new String[3];
		pArgs[0] = "new";
		pArgs[1] = "test";
		pArgs[2] = "20158";


		
		NodeTestingThread node1 = new NodeTestingThread(pArgs);
		node1.start();
		Thread.sleep(10000);
		int firstPort = 20159;
		pArgs = new String[2];
		pArgs[0] = "localhost:20158";
		for (int i = 0; i < 1; i++) {
			pArgs[1] = String.format("%d", firstPort + i);
			NodeTestingThread node = new NodeTestingThread(pArgs);
			node.start();
			Thread.sleep(1000);
		}
		
//		MessageNodeAddress[] list;
//		ConcurrentSkipListSet<NodeAddress> domainNodeList = new ConcurrentSkipListSet<NodeAddress>();
//		domainNodeList.add(new NodeAddress("localhost", 10));
//		list = (MessageNodeAddress[]) domainNodeList.toArray(new NodeAddress[0]);
//		for (int i = 0; i < list.length; i++) System.out.println(list[i]);
//		domainNodeList.add(new NodeAddress("localhost", 11));
//		list = (MessageNodeAddress[]) domainNodeList.toArray(new NodeAddress[0]);
//		
//		for (int i = 0; i < list.length; i++) System.out.println(list[i]);
		


	}
}
