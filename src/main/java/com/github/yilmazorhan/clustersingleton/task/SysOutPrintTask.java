package com.github.yilmazorhan.clustersingleton.task;

import java.io.Serializable;

public class SysOutPrintTask implements ClusterTask, Serializable {
	private static final long serialVersionUID = 3583361316278614904L;

	@Override
	public void process() {
		System.out.println("Hello world");
	}

}
