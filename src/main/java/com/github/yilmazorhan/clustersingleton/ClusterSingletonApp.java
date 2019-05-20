package com.github.yilmazorhan.clustersingleton;

import java.util.UUID;

import com.github.yilmazorhan.clustersingleton.deployment.DeploymentStrategy;
import com.github.yilmazorhan.clustersingleton.deployment.ImmediateDeploymentStrategy;
import com.github.yilmazorhan.clustersingleton.task.ClusterTask;
import com.github.yilmazorhan.clustersingleton.task.SysOutPrintTask;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ClusterSingletonApp {
	private ClusterTask clusterTask;
	private DeploymentStrategy deploymentStrategy;
	private String nodename;
	private String groupName;
	private HazelcastInstance hzInstance;

	public ClusterSingletonApp(String nodename, String groupname, ClusterTask clusterTask,
			DeploymentStrategy deploymentStrategy) {
		this.clusterTask = clusterTask;
		this.nodename = nodename;
		this.groupName = groupname;
		this.deploymentStrategy = deploymentStrategy;
	}

	public static void main(String[] args) {
		ClusterSingletonApp app = new ClusterSingletonApp(UUID.randomUUID().toString(), "ClusterSingletonApp",
				new SysOutPrintTask(), new ImmediateDeploymentStrategy());
		app.startUp();
	}

	public void startUp() {
		Config cfg = new ClasspathXmlConfig("hazelcast.xml");
		cfg.setInstanceName(nodename);
		cfg.getGroupConfig().setName(groupName);
		hzInstance = Hazelcast.getOrCreateHazelcastInstance(cfg);
		deploymentStrategy.deploy(hzInstance, clusterTask);
	}

	public void clusterShutdown() {
		try {
			hzInstance.getCluster().shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		if (null != null) {
			hzInstance.shutdown();
			// hzInstance.getLifecycleService().terminate();
		}
	}
}