/**
 * File ReconfigurableTestModule.java
 *
 * This file is part of the demanesImplementation project 2014.
 * 
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
package eu.artemis.demanes.test;

import eu.artemis.demanes.impl.annotations.DEM_Action;
import eu.artemis.demanes.impl.annotations.DEM_Argument;
import eu.artemis.demanes.impl.annotations.DEM_Observation;
import eu.artemis.demanes.reconfiguration.Triggerable;

/**
 * ReconfigurableTestModule
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 9 apr. 2014
 * 
 */
public class ReconfigurableTestModule implements Triggerable {

	public static final String GETMESSAGE = "urn:reconfigurabletestmodule:GetAlertMessage";
	public static final String GETMESSAGENUM = "urn:reconfigurabletestmodule:GetNumberOfMessage";
	public static final String RESETMESSAGES = "urn:reconfigurabletestmodule:ResetNumberOfMessages";
	public static final String SETTEXT = "urn:reconfigurabletestmodule:SetAlertMessage";
	public static final String THROWERROR = "urn:reconfigurabletestmodule:RaiseRunTimeException";

	private int numAlerts = 0;

	private String text = "Hello World!";

	@DEM_Observation(urn = GETMESSAGE)
	public String getMessage() {
		return this.text;
	}

	@DEM_Observation(urn = GETMESSAGENUM)
	public int getNumAlerts() {
		return this.numAlerts;
	}

	@DEM_Action(urn = RESETMESSAGES)
	public void reset() {
		this.numAlerts = 0;
	}

	@DEM_Action(urn = SETTEXT)
	public void setText(@DEM_Argument(name = "newText") String newText) {
		this.text = newText;
	}

	@DEM_Action(urn = THROWERROR)
	public void throwError() {
		throw new RuntimeException("This is the expected behavior");
	}
	
	@Override
	public void trigger() {
		this.numAlerts++;
		System.out.println(this.text);
	}
}
