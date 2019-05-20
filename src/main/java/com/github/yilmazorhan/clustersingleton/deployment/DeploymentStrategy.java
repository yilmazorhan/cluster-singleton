package com.github.yilmazorhan.clustersingleton.deployment;

import com.github.yilmazorhan.clustersingleton.task.ClusterTask;
import com.hazelcast.core.HazelcastInstance;

public interface DeploymentStrategy {
	public void deploy(HazelcastInstance instance, ClusterTask task);
}
