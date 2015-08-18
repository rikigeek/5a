package rikigeek.aaaaa;

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
		sb.append(formatMessage(record));
		sb.append(", ");
		sb.append("[");
		sb.append(record.getLoggerName());
		sb.append("-");
		sb.append("T#");
		sb.append(record.getThreadID());
		sb.append("], ");
		sb.append(record.getSourceClassName() + "." + record.getSourceMethodName());
		sb.append(System.lineSeparator());
		return sb.toString();
	}

}
