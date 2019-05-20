package com.github.yilmazorhan.clustersingleton.test.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import com.github.yilmazorhan.clustersingleton.task.SysOutPrintTask;

/**
 * 
 * This class simulates console.out exceptions.
 *
 */
public class RandomFailSysOutPrintWithCountTask extends SysOutPrintTask {

	private static final long serialVersionUID = 3816317557471881313L;

	private static final Random random = new Random(System.nanoTime());

	private final String tmpFolderPath;
	private final String taskUUID;

	public RandomFailSysOutPrintWithCountTask(String tmpFolderPath, String taskUUID) {
		this.tmpFolderPath = tmpFolderPath;
		this.taskUUID = taskUUID;
	}

	@Override
	public void process() {
		if (random.nextBoolean()) {
			// assume that something bad happened.
			System.out.println("Something bad happened.");
			throw new IllegalStateException("Something bad happened while executing sysout.");
		}
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
