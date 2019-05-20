package com.github.yilmazorhan.clustersingleton.test.strategy;

import static com.github.yilmazorhan.clustersingleton.AppConstants.IS_HELLOWORLD_PRINTED;
import static com.github.yilmazorhan.clustersingleton.AppConstants.LOCK_MAP;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.github.yilmazorhan.clustersingleton.deployment.DeploymentStrategy;
import com.github.yilmazorhan.clustersingleton.process.RunOnlyOnceProccessor;
import com.github.yilmazorhan.clustersingleton.task.ClusterTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class RandomShutdownDeploymentStrategy implements DeploymentStrategy {

	private final Timer timer = new Timer(false);
	private static final Random random = new Random(System.nanoTime());

	private final Long delayInMiliseconds;

	public RandomShutdownDeploymentStrategy(Long delayInMiliseconds) {
		super();
		this.delayInMiliseconds = delayInMiliseconds;
	}

	@Override
	public void deploy(HazelcastInstance instance, ClusterTask task) {

		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (random.nextBoolean()) {
					// assume that something bad happened.
					System.out.println("Unlucky node." + instance.getName());
					instance.getLifecycleService().terminate();
				} else {
					IMap<String, Boolean> map = instance.getMap(LOCK_MAP);
					map.putIfAbsent(IS_HELLOWORLD_PRINTED, Boolean.FALSE);
					map.executeOnKey(IS_HELLOWORLD_PRINTED, new RunOnlyOnceProccessor(task));
				}
			}
		}, delayInMiliseconds);

	}

}
