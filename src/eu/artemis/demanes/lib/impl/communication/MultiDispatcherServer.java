/**
 * File MultiDispatcherServer.java
 * 
 * This file is part of the eu.artemis.demanes.lib.usbConnector project.
 *
 * Copyright 2014 TNO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.artemis.demanes.lib.impl.communication;

import java.nio.ByteBuffer;
import java.util.HashSet;

import org.apache.log4j.Logger;

import eu.artemis.demanes.lib.MessageDispatcher;
import eu.artemis.demanes.lib.MessageDispatcherRegistry;
import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;

/**
 * MultiDispatcherServer
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 28 okt. 2014
 *
 */
public class MultiDispatcherServer implements MessageDispatcherRegistry, MessageDispatcher {

	private final Logger logger = Logger.getLogger("dmns:log");

	private final HashSet<MessageDispatcher> dispatchers = new HashSet<MessageDispatcher>();

	/**
	 * Is this strategy viable?
	 * 
	 * @param msg
	 * @return
	 */
	@Override
	public ByteBuffer dispatchMessage(ByteBuffer msg) {
		logger.trace(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_TRACE, "Comm", "Dispatching message "
						+ CommUtils.toString(msg)));

		msg.mark();
		for (MessageDispatcher md : dispatchers) {
			msg.reset(); // Reset the buffer to the mark
			ByteBuffer response = md.dispatchMessage(msg);
			if (response != null) {
				logger.trace(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_TRACE, "Comm", "Returning response "
								+ response));
				return response;
			}
		}
		return null;
	}

	/**
	 * @param dispatcher
	 */
	@Override
	public void addDispatcher(MessageDispatcher dispatcher) {
		this.dispatchers.add(dispatcher);
	}

	/**
	 * @param dispatcher
	 */
	@Override
	public void removeDispatcher(MessageDispatcher dispatcher) {
		this.dispatchers.remove(dispatcher);
	}

	/* (non-Javadoc)
	 * @see eu.artemis.demanes.lib.MessageDispatcherRegistry#containsDispatcher(eu.artemis.demanes.lib.MessageDispatcher)
	 */
	@Override
	public boolean containsDispatcher(MessageDispatcher dispatcher) {
		return this.dispatchers.contains(dispatcher);
	}

}
