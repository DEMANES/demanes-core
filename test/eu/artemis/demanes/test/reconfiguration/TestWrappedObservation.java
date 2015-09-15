/**
 * File TestAction.java
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
package eu.artemis.demanes.test.reconfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.InexistentObservationID;
import eu.artemis.demanes.exceptions.ObservationInvocationException;
import eu.artemis.demanes.exceptions.ReconfigurationAnnotationURNException;
import eu.artemis.demanes.impl.reconfiguration.DefaultORAMediator;
import eu.artemis.demanes.lib.impl.selfregistry.ORAFactory;
import eu.artemis.demanes.reconfiguration.ObservationProvider;
import eu.artemis.demanes.test.ReconfigurableTestModule;

/**
 * TestAction
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 10 apr. 2014
 * 
 */
public class TestWrappedObservation {

	private ObservationProvider observationProvider;

	private ReconfigurableTestModule rtm;

	@Before
	public void init() {
		this.rtm = new ReconfigurableTestModule();
		DefaultORAMediator oram = new DefaultORAMediator();
		try {
			oram.registerObserver(ORAFactory.createObserver(rtm));
		} catch (ReconfigurationAnnotationURNException e) {
			e.printStackTrace();
		}
		this.observationProvider = oram;
	}

	@Test
	public void testProperObservationInvocation()
			throws InexistentObservationID, ObservationInvocationException,
			URISyntaxException {
		Object o = this.observationProvider.getValue(new ANES_URN(
				ReconfigurableTestModule.GETMESSAGE));

		assertEquals(String.class, o.getClass());
		assertEquals(o, "Hello World!");

		Object o2 = this.observationProvider.getValue(new ANES_URN(
				ReconfigurableTestModule.GETMESSAGENUM));

		assertEquals(o2.getClass(), Integer.class);
		assertEquals(o2, 0);
	}

	@Test
	public void testImproperObservationInvocation() {
		try {
			this.observationProvider.getValue(null);
			fail("Expected InexistentObservationID Exception");
		} catch (Exception e) {
			assertEquals(InexistentObservationID.class, e.getClass());
		}
		
		try {
			this.observationProvider.getValue(new ANES_URN("dmns",
					"bestaatniet"));
			fail("Expected InexistentObservationID Exception");
		} catch (Exception e) {
			assertEquals(InexistentObservationID.class, e.getClass());
		}
	}
}
