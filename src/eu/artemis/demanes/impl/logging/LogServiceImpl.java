package eu.artemis.demanes.impl.logging;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;

import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;
import eu.artemis.demanes.logging.LogService;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class LogServiceImpl implements LogService {

	private static String logType = LogUtils.getLogTyoe();
	private PaxLoggingService logService;

	// @Activate
	// public void start() {
	// System.out.println("Activating log service implementation");
	// }

//	@Reference(optional = true)
	@Reference
	public void bindPaxLoggingService(PaxLoggingService paxLoggingService) {
		this.logService = paxLoggingService;
	}

	@Override
	public void log(LogEntry logEntry) {
		if (this.logService == null) {
			System.out
					.println("No connection to logging service. Log aborted.");
			return;
		}
		PaxLogger logger = this.logService.getLogger(null, logType, null);
		if (logger == null) {
			System.out.println("Unable to get a logService. Log aborted.");
		}

		String msg = LogUtils.formatMessage(logEntry);
		switch (logEntry.getLevel()) {
		case LogConstants.LOG_LEVEL_TRACE:
			logger.trace(msg, logEntry.getException());
			break;
		case LogConstants.LOG_LEVEL_DEBUG:
			logger.debug(msg, logEntry.getException());
			break;
		case LogConstants.LOG_LEVEL_INFO:
			logger.inform(msg, logEntry.getException());
			break;
		case LogConstants.LOG_LEVEL_WARN:
			logger.warn(msg, logEntry.getException());
			break;
		case LogConstants.LOG_LEVEL_ERROR:
			logger.error(msg, logEntry.getException());
			break;
		case LogConstants.LOG_LEVEL_FATAL:
			logger.fatal(msg, logEntry.getException());
			break;
		default:
			logger.debug(msg, logEntry.getException());
			break;
		}

	}

}
