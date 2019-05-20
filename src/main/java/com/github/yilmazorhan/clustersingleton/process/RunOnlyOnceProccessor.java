package com.github.yilmazorhan.clustersingleton.process;

import java.io.Serializable;
import java.util.Map;

import com.github.yilmazorhan.clustersingleton.task.ClusterTask;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public class RunOnlyOnceProccessor
		implements EntryProcessor<String, Boolean>, EntryBackupProcessor<String, Boolean>, Serializable {

	private static final long serialVersionUID = -1406373331872105693L;

	private final ClusterTask clusterTask;

	public RunOnlyOnceProccessor(ClusterTask clusterTask) {
		this.clusterTask = clusterTask;
	}

	public Object process(Map.Entry<String, Boolean> entry) {
		if (!Boolean.TRUE.equals(entry.getValue())) {
			clusterTask.process();
			entry.setValue(Boolean.TRUE);
		}
		return null;
	}

	public EntryBackupProcessor<String, Boolean> getBackupProcessor() {
		return RunOnlyOnceProccessor.this;
	}

	public void processBackup(Map.Entry<String, Boolean> entry) {
		entry.setValue(Boolean.TRUE);//eventually true
	}

}
