package com.vo.common;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.Templates;


/**
 * 
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 * 
 */
public class ZE {
	
	public static <V> Future<V> submit(final Callable<V> callable) {
		final Future<V> f = ZE.submit(callable);
		return f;
	}
	
	public static void execute(final Runnable runnable) {
		ZE.execute(runnable);
	}

	
	private static final ThreadPoolExecutor ZE = new ThreadPoolExecutor(
			Runtime.getRuntime().availableProcessors(),
			Runtime.getRuntime().availableProcessors(),
			0L,
			TimeUnit.MILLISECONDS, 
			new LinkedBlockingQueue<>(),
			new ZThreadFactory("zrpc-client-worker-thread-"),
			new ThreadPoolExecutor.CallerRunsPolicy());


	public static class ZThreadFactory implements ThreadFactory {
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private  String namePrefix;
		
		public ZThreadFactory(final String namePrefix) {
			super();
			this.namePrefix = namePrefix;
			final SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.namePrefix = namePrefix;
		}
		
		@Override
		public Thread newThread(final Runnable r) {
			final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}

	}
	
}
