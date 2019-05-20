package com.github.yilmazorhan.clustersingleton.test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import com.github.yilmazorhan.clustersingleton.ClusterSingletonApp;

public class ClusterSingletonAppThread extends Thread {
	private final CyclicBarrier cyclicBarrier;
	private final ClusterSingletonApp app;

	public ClusterSingletonAppThread(CyclicBarrier cyclicBarrier, ClusterSingletonApp app) {
		super();
		this.cyclicBarrier = cyclicBarrier;
		this.app = app;
	}

	public void run() {
		try {
			cyclicBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException();
		}
		app.startUp();
	}
	
	public void clusterShutdown() {
		app.clusterShutdown();
	}
}
