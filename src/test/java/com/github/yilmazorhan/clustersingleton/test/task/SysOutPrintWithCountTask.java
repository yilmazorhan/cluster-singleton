package com.github.yilmazorhan.clustersingleton.test.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.yilmazorhan.clustersingleton.task.SysOutPrintTask;

public class SysOutPrintWithCountTask extends SysOutPrintTask {

	private static final long serialVersionUID = 3816317557471881313L;
	private final String tmpFolderPath;
	private final String taskUUID;

	public SysOutPrintWithCountTask(String tmpFolderPath, String taskUUID) {
		this.tmpFolderPath = tmpFolderPath;
		this.taskUUID = taskUUID;
	}

	@Override
	public void process() {
		super.process();
		Path path = Paths.get(tmpFolderPath + File.separator + taskUUID);
		try {
			Files.createFile(path);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
