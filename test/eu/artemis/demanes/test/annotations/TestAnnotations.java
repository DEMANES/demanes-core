/**
 * File TestAnnotations.java
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
package eu.artemis.demanes.test.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import eu.artemis.demanes.datatypes.ANES_BUNDLE;
import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.ActionInvocationException;
import eu.artemis.demanes.exceptions.ObservationInvocationException;
import eu.artemis.demanes.exceptions.ReconfigurationAnnotationException;
import eu.artemis.demanes.impl.datatypes.DefaultANES_BUNDLE;
import eu.artemis.demanes.lib.impl.selfregistry.ORAFactory;
import eu.artemis.demanes.reconfiguration.Action;
import eu.artemis.demanes.reconfiguration.Actuator;
import eu.artemis.demanes.reconfiguration.Observation;
import eu.artemis.demanes.reconfiguration.Observer;
import eu.artemis.demanes.test.ReconfigurableTestModule;

/**
 * TestAnnotations
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 9 apr. 2014
 * 
 */
public class TestAnnotations {

	private Actuator actuator;
	private Observer observer;
	private ReconfigurableTestModule rtm;

	@Before
	public void init() throws ReconfigurationAnnotationException {
		this.rtm = new ReconfigurableTestModule();
		this.actuator = ORAFactory.createActuator(rtm);
		this.observer = ORAFactory.createObserver(rtm);
	}

	@Test
	public void noAnnotationTest() throws ReconfigurationAnnotationException {
		String test = "Test string";

		Actuator a = ORAFactory.createActuator(test);
		Observer o = ORAFactory.createObserver(test);

		assertTrue("No annotations expected in java.lang.String", a
				.getActions().isEmpty());
		assertTrue("No annotations expected in java.lang.String", o
				.getObservations().isEmpty());
	}

	@Test
	public void testActions() throws URISyntaxException,
			ActionInvocationException {
		Set<Action> actions = this.actuator.getActions();
		assertEquals("Invalid number of actions returned", actions.size(), 3);

		rtm.trigger();
		assertEquals(1, rtm.getNumAlerts());

		for (Action a : actions)
			if (a.getActionID().equals(
					new ANES_URN(ReconfigurableTestModule.SETTEXT))) {
				// Test rtm.setText() functionality
				ANES_BUNDLE arguments = new DefaultANES_BUNDLE();
				String newValue = "Nieuwe Text";
				arguments.put("newText", newValue);

				a.invoke(arguments);
				assertEquals(newValue, rtm.getMessage());
			} else if (a.getActionID().equals(
					new ANES_URN(ReconfigurableTestModule.RESETMESSAGES))) {
				// Test rtm.reset() functionality
				a.invoke(null);
				assertEquals(0, rtm.getNumAlerts());
			} else if (a.getActionID().equals(
					ReconfigurableTestModule.THROWERROR)) {
				// Test rtm.throwError() functionality
				try {
					a.invoke(null);
					fail("Unexpected ActionInvocationException");
				} catch (Exception e) {
					assertEquals(ActionInvocationException.class, e.getClass());
					assertEquals(InvocationTargetException.class, e.getCause()
							.getClass());
					assertEquals(RuntimeException.class, e.getCause()
							.getCause().getClass());
				}
			} else {
				fail("Unexpected Action URN");
			}
	}

	@Test
	public void testObservations() throws ObservationInvocationException, URISyntaxException {
		Set<Observation> observations = this.observer.getObservations();
		assertEquals("Invalid number of observations returned",
				observations.size(), 2);

		for (Observation o : observations) {
			Object value = o.getValue();
			if (o.getObservationID().equals(new ANES_URN(ReconfigurableTestModule.GETMESSAGE))) {
				assertEquals(String.class, value.getClass());
				assertEquals("Hello World!", value);
			} else if (o.getObservationID().equals(new ANES_URN(ReconfigurableTestModule.GETMESSAGENUM))) {
				assertEquals(Integer.class, value.getClass());
				assertEquals(0, value);
			} else {
				fail("Unexpected Observation URN");
			}
		}
	}
}
