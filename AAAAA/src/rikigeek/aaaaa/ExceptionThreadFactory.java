package rikigeek.aaaaa;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
/**
 * Specific threadfactory that take care of the uncaugh exception
 * @author Rikigeek
 *
 */
public class ExceptionThreadFactory implements ThreadFactory {
	private static final ThreadFactory defaultFactory = Executors
			.defaultThreadFactory();
	private final Thread.UncaughtExceptionHandler handler;

	public ExceptionThreadFactory(Thread.UncaughtExceptionHandler handler) {
		this.handler = handler;
	}

	@Override
	public Thread newThread(Runnable run) {
		Thread thread = defaultFactory.newThread(run);
		thread.setUncaughtExceptionHandler(handler);
		return thread;
	}
}
