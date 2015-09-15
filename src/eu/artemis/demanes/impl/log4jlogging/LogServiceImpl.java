/**
 * Copyright 2014 TNO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.artemis.demanes.impl.log4jlogging;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.varia.LevelRangeFilter;

import aQute.bnd.annotation.component.Component;
import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;
import eu.artemis.demanes.logging.LogService;

@Component(immediate = true)
public class LogServiceImpl implements LogService {

	private static final boolean FILE_APPEND = true;

	private static final Level CONSOLE_LEVEL = Level.DEBUG;
	
	private static final String MAX_FILE_SIZE = "50MB";
	
	private static final int MAX_BACKUP_INDEX = 10;

	private final Logger logger = Logger.getLogger("dmns:log");

	public LogServiceImpl() throws IOException {
		Layout layout = new PatternLayout("[%p] %m%n");

		// Log everything in the trace file
		RollingFileAppender traceAppender = new RollingFileAppender(layout,
				"logs//demanes_trace.log", FILE_APPEND);
		traceAppender.setMaxFileSize(MAX_FILE_SIZE);
		traceAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);
		
		// Log debug messages and higher in the debug file
		RollingFileAppender debugAppender = new RollingFileAppender(layout,
				"logs//demanes_debug.log", FILE_APPEND);
		LevelRangeFilter debugFilter = new LevelRangeFilter();
		debugFilter.setLevelMin(Level.DEBUG);
		debugAppender.addFilter(debugFilter);
		debugAppender.setMaxFileSize(MAX_FILE_SIZE);
		debugAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);

		// Log only info and higher in the info file
		RollingFileAppender infoAppender = new RollingFileAppender(layout,
				"logs//demanes_info.log", FILE_APPEND);
		LevelRangeFilter infoFilter = new LevelRangeFilter();
		infoFilter.setLevelMin(Level.INFO);
		infoAppender.addFilter(infoFilter);
		infoAppender.setMaxFileSize(MAX_FILE_SIZE);
		infoAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);

		// Log warnings, errors and fatals in the error file
		RollingFileAppender errorAppender = new RollingFileAppender(layout,
				"logs//demanes_error.log", FILE_APPEND);
		LevelRangeFilter errorFilter = new LevelRangeFilter();
		errorFilter.setLevelMin(Level.WARN);
		errorAppender.addFilter(errorFilter);
		errorAppender.setMaxFileSize(MAX_FILE_SIZE);
		errorAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);

		// Also show in the console
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		LevelRangeFilter consoleFilter = new LevelRangeFilter();
		consoleFilter.setLevelMin(CONSOLE_LEVEL);
		consoleAppender.addFilter(consoleFilter);

		logger.removeAllAppenders();
		logger.addAppender(traceAppender);
		logger.addAppender(debugAppender);
		logger.addAppender(infoAppender);
		logger.addAppender(errorAppender);
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.ALL);

		this.log(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_INFO, "Log Service started"));
	}

	@Override
	public void log(LogEntry logEntry) {
		switch (logEntry.getLevel()) {
		case LogConstants.LOG_LEVEL_TRACE:
			logger.trace(logEntry);
			break;
		case LogConstants.LOG_LEVEL_DEBUG:
			logger.debug(logEntry);
			break;
		case LogConstants.LOG_LEVEL_INFO:
			logger.info(logEntry);
			break;
		case LogConstants.LOG_LEVEL_WARN:
			logger.warn(logEntry);
			break;
		case LogConstants.LOG_LEVEL_ERROR:
			logger.error(logEntry);
			break;
		case LogConstants.LOG_LEVEL_FATAL:
			logger.fatal(logEntry);
			break;
		default:
			logger.debug(logEntry);
			break;
		}

	}

}
