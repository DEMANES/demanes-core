/**
 * File LoggerTester.java
 * 
 * This file is part of the eu.artemis.demanes.impl project.
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
package eu.artemis.demanes.test.logging;

import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;
import eu.artemis.demanes.logging.LogService;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * LoggerTester
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 19 nov. 2014
 *
 */
@Component
public class LoggerTester {

	@Reference
	public void setLogger(LogService service) {
		System.out.println("Starting to do the logger!");
		//while (true) {
			service.log(new LogEntry("LoggerTest", LogConstants.LOG_LEVEL_INFO, "This is a random test!"));
		//}
	}
	
}
