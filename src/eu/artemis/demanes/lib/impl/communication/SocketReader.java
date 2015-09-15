/**
 * File ProxyServerReader.java
 * 
 * This file is part of the eu.artemis.demanes.lib.sunspotConnector project.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import eu.artemis.demanes.lib.MessageDispatcher;
import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;

/**
 * ProxyServerReader
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 1 jul. 2014
 * 
 */
public class SocketReader implements Runnable {

	private final Logger logger = Logger.getLogger("dmns:log");

	private static final int MAX_BUFFER_LENGTH = 256*256;

	private final MessageDispatcher dispatcher;

	private final InputStream in;

	private final OutputStream out;

	private volatile boolean running;

	public SocketReader(InputStream in, OutputStream out, MessageDispatcher md) {
		this.in = in;
		this.out = out;
		this.dispatcher = md;
		this.running = true;
	}

	@Override
	public void run() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Comm",
				"Starting SocketReader thread"));

		ByteBuffer buf = ByteBuffer.allocate(MAX_BUFFER_LENGTH);
		int len, len2, read, readBytes;

		while (this.running) {
			try {
				// First get the length of the message
				len = this.in.read();

				// If nothing arrives, retry in a bit;
				if (len == -1) {
					Thread.sleep(100);
					continue;
				}
				
				// Add second byte
				len2 = this.in.read();
				len = (len * 256) + len2;

				logger.trace(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_TRACE,
						"Reading message of length " + len));
				
				// Now we know the length, let's read
				buf.clear();
				readBytes = 0;
				while (readBytes < len) {
					read = this.in.read();

					if (read == -1) {
						Thread.sleep(100);
						continue;
					}

					buf.put((byte) read);
					readBytes++;
				}

				// Check if it is properly ended with an eom
				read = this.in.read();
				if (read != CommUtils.END_OF_MESSAGE) {

					logger.warn(new LogEntry(this.getClass().getName(),
							LogConstants.LOG_LEVEL_WARN,
							"Message not terminated with EOM"));

					ByteBuffer soFar = buf.duplicate();
					soFar.flip();
					logger.trace(new LogEntry(this.getClass().getName(),
							LogConstants.LOG_LEVEL_TRACE,
							"Message received so far: " + CommUtils.toString(soFar)));
					
					// Read until we DO read an EOM (we are too nice)
					while (read != CommUtils.END_OF_MESSAGE && read != -1) {
						buf.put((byte) read);
						read = this.in.read();
					}

					logger.trace(new LogEntry(this.getClass().getName(),
							LogConstants.LOG_LEVEL_TRACE,
							"Message protocol indicates length of " + len
									+ ", instead found" + buf.position()));
				}

				// We now should have the complete message
				buf.flip();
				logger.trace(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_TRACE, "Comm",
						"Received message: " + CommUtils.toString(buf)));

				ByteBuffer response = dispatcher.dispatchMessage(buf);
				if (response != null) {
					System.err.println("CHECK THIS CODE!!!!");
					// I think it should use response.remaining and READ from
					// it..

					this.out.write(response.capacity());
					this.out.write(response.array());
					this.out.write(CommUtils.END_OF_MESSAGE);
					this.out.flush();
				}

			} catch (IOException e) {
				e.printStackTrace();
				break;
			} catch (InterruptedException e) {
				logger.warn(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_WARN,
						"SocketReader thread interrupted", e));
			} catch (Exception e) {
				logger.error(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_ERROR, "Comm",
						"Error in SocketReader, resetting buffers", e));
				logger.trace(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_TRACE, "Comm",
						"Current buffer contents: ["
								+ CommUtils.asHex(buf.array()) + "]"));
				buf.clear();
			}
		}
	}

	// @Override
	// public void run() {
	// logger.debug(new LogEntry(this.getClass().getName(),
	// LogConstants.LOG_LEVEL_DEBUG, "Comm",
	// "Starting SocketReader thread"));
	//
	// ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFFER_LENGTH);
	// int receivedByte = 0;
	// int len = 0;
	// while (this.running) {
	// try {
	// receivedByte = this.in.read();
	// if (receivedByte == -1) {
	// Thread.sleep(100);
	// } else if ((byte) receivedByte != CommUtils.END_OF_MESSAGE) {
	// buffer.put((byte) receivedByte);
	// len++;
	// } else {
	// ByteBuffer msg = ByteBuffer.allocate(len);
	// msg.put(buffer.array(), 0, len);
	// msg.flip();
	// logger.trace(new LogEntry(this.getClass().getName(),
	// LogConstants.LOG_LEVEL_TRACE, "Comm",
	// "Received message: " + new String(msg.array())
	// + "(" + CommUtils.asHex(msg.array()) + ")"));
	//
	// ByteBuffer response = dispatcher.dispatchMessage(msg);
	// if (response != null) {
	// this.out.write(response.array());
	// this.out.write(CommUtils.END_OF_MESSAGE);
	// this.out.flush();
	// }
	//
	// buffer.clear();
	// len = 0;
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// break;
	// } catch (InterruptedException e) {
	// logger.warn(new LogEntry(this.getClass().getName(),
	// LogConstants.LOG_LEVEL_WARN,
	// "SocketReader thread interrupted", e));
	// } catch (Exception e) {
	// logger.error(new LogEntry(this.getClass().getName(),
	// LogConstants.LOG_LEVEL_ERROR, "Comm",
	// "Error in SocketReader, resetting buffers", e));
	// logger.trace(new LogEntry(this.getClass().getName(),
	// LogConstants.LOG_LEVEL_TRACE, "Comm",
	// "Current buffer contents: ["
	// + CommUtils.asHex(buffer.array()) + "]"));
	// buffer.clear();
	// len = 0;
	// }
	// }
	// }

	public void stop() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Stopping SocketReader thread"));
		this.running = false;
	}

}
