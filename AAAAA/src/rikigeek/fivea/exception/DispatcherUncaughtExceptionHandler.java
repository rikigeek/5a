package rikigeek.fivea.exception;

import java.lang.Thread.UncaughtExceptionHandler;
// TODO : extends from a global "Exception Reporter". This reporter will log everything
public class DispatcherUncaughtExceptionHandler 
		implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// TODO Recovery or logging code
	}

}
