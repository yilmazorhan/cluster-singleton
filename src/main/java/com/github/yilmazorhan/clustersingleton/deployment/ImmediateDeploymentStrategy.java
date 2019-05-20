package com.github.yilmazorhan.clustersingleton.deployment;

import static com.github.yilmazorhan.clustersingleton.AppConstants.IS_HELLOWORLD_PRINTED;
import static com.github.yilmazorhan.clustersingleton.AppConstants.LOCK_MAP;

import com.github.yilmazorhan.clustersingleton.process.RunOnlyOnceProccessor;
import com.github.yilmazorhan.clustersingleton.task.ClusterTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ImmediateDeploymentStrategy implements DeploymentStrategy {

	@Override
	public void deploy(HazelcastInstance instance, ClusterTask task) {
		IMap<String, Boolean> map = instance.getMap(LOCK_MAP);
		map.putIfAbsent(IS_HELLOWORLD_PRINTED, Boolean.FALSE);
		map.executeOnKey(IS_HELLOWORLD_PRINTED, new RunOnlyOnceProccessor(task));
	}

}
