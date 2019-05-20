package com.github.yilmazorhan.clustersingleton.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.yilmazorhan.clustersingleton.ClusterSingletonApp;
import com.github.yilmazorhan.clustersingleton.deployment.DeploymentStrategy;
import com.github.yilmazorhan.clustersingleton.deployment.ImmediateDeploymentStrategy;
import com.github.yilmazorhan.clustersingleton.test.strategy.DelayedDeploymentStrategy;
import com.github.yilmazorhan.clustersingleton.test.strategy.RandomShutdownDeploymentStrategy;
import com.github.yilmazorhan.clustersingleton.test.task.RandomFailSysOutPrintWithCountTask;
import com.github.yilmazorhan.clustersingleton.test.task.SysOutPrintWithCountTask;

/*
 * 
 * this test class based on shared file system.
 * If all test hazelcast nodes shares same directory.
 * wide range of singleton test can be made.
 * 
 * There are of course better ways to do this, but it is simple enough.
 * 
 */
@DisplayName("Entry Processor Based Strategy")
@TestMethodOrder(OrderAnnotation.class)
public class ClusterSingletonAppTest {

	private static final long TEN_SECONDS = 10_000l;

	private static final long TWENTY_SECONDS = 10_000l * 2;

	Path path;

	private static Path workDir;

	@Order(0)
	@DisplayName("Test randomly shutdown ten application instance is printing single helloworld.")
	@Test
	/**
	 * simulate crash while processing sys.out.
	 */
	public void testRandomShutdownWithTenInstance() throws Exception {

		final CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
		List<ClusterSingletonAppThread> apps = IntStream.range(0, 10).mapToObj(i -> {
			return new ClusterSingletonAppThread(cyclicBarrier,
					new ClusterSingletonApp("node-" + i, "Delayed Random Shutdown Ten Node",
							new RandomFailSysOutPrintWithCountTask(path.toString(), UUID.randomUUID().toString()),
							new RandomShutdownDeploymentStrategy(8000L)));
		}).collect(Collectors.toList());
		assertEquals(new Integer(10), apps.size());
		apps.forEach(Thread::start);
		Thread.sleep(TWENTY_SECONDS);
		apps.get(0).clusterShutdown();
		int count = Files.list(path).collect(Collectors.toList()).size();
		assertEquals(new Integer(1), count);
	}

	@Order(1)
	@DisplayName("Test single application instance is printing single helloworld.")
	@Test
	public void testSingleInstance() throws Exception {
		String uuid = UUID.randomUUID().toString();
		SysOutPrintWithCountTask clusterTask = new SysOutPrintWithCountTask(path.toString(), uuid);
		DeploymentStrategy immediateStrategy = new ImmediateDeploymentStrategy();
		ClusterSingletonApp app = new ClusterSingletonApp("singlenode", "singlenode", clusterTask, immediateStrategy);
		app.startUp();
		Thread.sleep(TEN_SECONDS);
		int count = Files.list(path).collect(Collectors.toList()).size();
		app.clusterShutdown();
		assertEquals(new Integer(1), count);
	}

	@Order(2)
	@DisplayName("Test ten application instance is printing single helloworld.")
	@Test
	public void testTenInstance() throws Exception {

		final CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
		List<ClusterSingletonAppThread> apps = IntStream.range(0, 10).mapToObj(i -> {
			return new ClusterSingletonAppThread(cyclicBarrier,
					new ClusterSingletonApp("node-" + i, "Ten Node",
							new SysOutPrintWithCountTask(path.toString(), UUID.randomUUID().toString()),
							new ImmediateDeploymentStrategy()));
		}).collect(Collectors.toList());
		assertEquals(new Integer(10), apps.size());
		apps.forEach(Thread::start);
		Thread.sleep(TWENTY_SECONDS);
		apps.get(0).clusterShutdown();
		int count = Files.list(path).collect(Collectors.toList()).size();
		assertEquals(new Integer(1), count);
	}

	@Order(3)
	@DisplayName("Test delayed ten application instance is printing single helloworld.")
	@Test
	public void testDelayedTenInstance() throws Exception {

		final CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
		List<ClusterSingletonAppThread> apps = IntStream.range(0, 10).mapToObj(i -> {
			return new ClusterSingletonAppThread(cyclicBarrier,
					new ClusterSingletonApp("node-" + i, "Delayed Ten Node",
							new SysOutPrintWithCountTask(path.toString(), UUID.randomUUID().toString()),
							new DelayedDeploymentStrategy(8000L)));
		}).collect(Collectors.toList());
		assertEquals(new Integer(10), apps.size());
		apps.forEach(Thread::start);
		Thread.sleep(TWENTY_SECONDS);
		apps.get(0).clusterShutdown();
		int count = Files.list(path).collect(Collectors.toList()).size();
		assertEquals(new Integer(1), count);
	}

	@Order(4)
	@DisplayName("Test randomly crashed delayed ten application instance is printing single helloworld.")
	@Test
	/**
	 * simulate crash while processing sys.out.
	 */
	public void testDelayedWithRandomCrashTenInstance() throws Exception {

		final CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
		List<ClusterSingletonAppThread> apps = IntStream.range(0, 10).mapToObj(i -> {
			return new ClusterSingletonAppThread(cyclicBarrier,
					new ClusterSingletonApp("node-" + i, "Delayed Random Crashed Ten Node",
							new RandomFailSysOutPrintWithCountTask(path.toString(), UUID.randomUUID().toString()),
							new DelayedDeploymentStrategy(8000L)));
		}).collect(Collectors.toList());
		assertEquals(new Integer(10), apps.size());
		apps.forEach(Thread::start);
		Thread.sleep(TWENTY_SECONDS);
		apps.get(0).clusterShutdown();
		int count = Files.list(path).collect(Collectors.toList()).size();
		assertEquals(new Integer(1), count);
	}

	@BeforeAll
	public static void prepareTempDir() throws Exception {
		workDir = Paths.get(System.getProperty("user.home") + File.separator + "workdir" + File.separator);
		if (!Files.isDirectory(workDir)) {
			Files.createDirectories(workDir);
		}
	}

	@BeforeEach
	public void setupDirectories() throws Exception {
		String tmpDir = workDir.toString() + File.separator + UUID.randomUUID().toString()
				+ File.separator;
		path = Paths.get(tmpDir);
		Files.createDirectory(path);
	}

	@AfterAll
	public static void cleanupDirectories() throws Exception {
		Files.walkFileTree(workDir, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		System.gc();
	}
}
