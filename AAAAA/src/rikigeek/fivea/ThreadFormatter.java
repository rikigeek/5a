package rikigeek.fivea;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ThreadFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		SimpleDateFormat d = new SimpleDateFormat();
		Date dd = new Date(record.getMillis());

		StringBuilder sb = new StringBuilder();
		sb.append(d.format(dd));
		sb.append(" ");
		sb.append(record.getLevel().getName());
		sb.append("\t: ");
		sb.append("[");
		sb.append("T#");
		sb.append(record.getThreadID());
		sb.append(".");
		Thread t = Thread.currentThread();
		sb.append(t.getName());
		sb.append("-");
		sb.append(record.getLoggerName());
		sb.append("], ");
		sb.append(record.getSourceClassName() + "."
				+ record.getSourceMethodName());
		sb.append(", ");
		// If a throwable, display the error message and the stack trace
		if (record.getThrown() != null) {
			Throwable exception = record.getThrown();
			// Format the exception
			sb.append(exception.toString());
			StackTraceElement stack[] = exception.getStackTrace();
			if (stack != null) {
				for (int i = 0; i < stack.length; i++) {
					sb.append(String.format("%n    at %s", stack[i].toString()));
				}
			}
		} else {
			// If it's not a throwable, display the classic message
			sb.append(formatMessage(record));
		}
		sb.append(System.lineSeparator());
		return sb.toString();
	}

}
