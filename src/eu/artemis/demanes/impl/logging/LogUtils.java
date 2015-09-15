package eu.artemis.demanes.impl.logging;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;

import eu.artemis.demanes.logging.LogEntry;
//import eu.artemis.demanes.logging.MonitoringLogEntryImpl;
import eu.artemis.demanes.logging.LogConstants;

public class LogUtils {

//	public static String formatMessage(LogEntry logEntry) {
//
//		String strDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
//				.format(logEntry.getTime());
//		StringBuilder sb = new StringBuilder("[").append(strDate)
//				.append("] ");
//		if (logEntry.getSource() == null) {
//			sb.append("- [undefined] ");
//		} else {
//			sb.append("- [").append(logEntry.getSource()).append("] ");
//		}
//		if (logEntry.getClass().equals(MonitoringLogEntryImpl.class)) {
//			sb.append("- [").append(
//					((MonitoringLogEntryImpl) logEntry).getTag()).append("] ");
//		} else {
//			sb.append("- [] ");
//		}
//		sb.append("- [").append(logEntry.getMessage()).append("]");
//		return sb.toString();
//	}

	public static String formatMessage(LogEntry logEntry) {

		String strDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
				.format(logEntry.getTime());
		StringBuilder sb = new StringBuilder("[").append(strDate)
				.append("] ");
		if (logEntry.getSource() == null) {
			sb.append("- [undefined] ");
		} else {
			sb.append("- [").append(logEntry.getSource()).append("] ");
		}
		if (logEntry.getTag() == null) {
			sb.append("- [] ");
		} else {
			sb.append("- [").append( logEntry.getTag()).append("] ");
		}
		sb.append("- [").append(logEntry.getMessage()).append("]");
		return sb.toString();
	}
	
	public static String getLogTyoe(){
		Properties prop = LogUtils.getProperties(LogConstants.CONFIG_PROPERTIES);
		String deviceLogger = LogConstants.POOR_DEVICE_LOGGER;
		if(prop!=null){
			deviceLogger = prop.getProperty(LogConstants.DEVICE_lOGGER_KEY);
			if(deviceLogger==null || !(deviceLogger.equalsIgnoreCase(LogConstants.RICH_DEVICE_LOGGER))){
				deviceLogger = LogConstants.POOR_DEVICE_LOGGER;
			}
		}
		return deviceLogger;
	}
		
		public static Properties getProperties(String file){
			Properties prop = new Properties();
			InputStream input = null;
			try {
				input = new FileInputStream(file);
				prop.load(input);
			} catch (FileNotFoundException e) {
				prop = null;
			} catch (IOException e) {
				prop = null;
			}finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return prop;
		
		}

/*	public static String getLogType() throws IOException {
		String logType = LogConstants.POOR_DEVICE_LOGGER;
		String logDirectory = System
				.getProperty(LogConstants.SYSTEM_LOG_LOCATION);
		if (logDirectory == null) {
			return logType;
		}
		long threshold = getLogThreshold();
		long freeSpace = FileSystemUtils.getUsableSpace(logDirectory);
		if ((freeSpace / 1024 / 1024) > threshold) {
			logType = LogConstants.RICH_DEVICE_LOGGER;
		}
		return logType;
	}

	private static long getLogThreshold() {
		long threshold = -1;
		Properties prop = FileSystemUtils.getProperties(System
				.getProperty(LogConstants.SYSTEM_CONFIG_LOCATION)
				+ "\\"
				+ LogConstants.CONFIG_PROPERTIES);
		if (prop != null) {
			String thr = prop.getProperty(LogConstants.FREE_STORAGE_THRESHOLD);
			if (thr != null) {
				try {
					threshold = Long.valueOf(thr).longValue();
				} catch (Exception e) {
					threshold = -1;
				}
			}

		}
		return threshold;
	} */

}
