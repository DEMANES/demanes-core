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

import eu.artemis.demanes.datatypes.ANES_BUNDLE;
import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.ActionInvocationException;
import eu.artemis.demanes.exceptions.InexistentActionID;
import eu.artemis.demanes.exceptions.ReconfigurationAnnotationException;
import eu.artemis.demanes.impl.datatypes.DefaultANES_BUNDLE;
import eu.artemis.demanes.impl.reconfiguration.DefaultORAMediator;
import eu.artemis.demanes.lib.impl.selfregistry.ORAFactory;
import eu.artemis.demanes.reconfiguration.ActionProvider;
import eu.artemis.demanes.test.ReconfigurableTestModule;

/**
 * TestAction
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 10 apr. 2014
 * 
 */
public class TestWrappedAction {

	private ActionProvider actionProvider;

	private ReconfigurableTestModule rtm;

	@Before
	public void init() {
		this.rtm = new ReconfigurableTestModule();
		DefaultORAMediator oram = new DefaultORAMediator();
		try {
			oram.registerActuator(ORAFactory.createActuator(rtm));
		} catch (ReconfigurationAnnotationException e) {
			e.printStackTrace();
		}
		this.actionProvider = oram;
	}

	@Test
	public void testProperActionInvocation() throws InexistentActionID,
			ActionInvocationException, URISyntaxException {
		String replacementText = "Hello Test!";
		ANES_BUNDLE arguments = new DefaultANES_BUNDLE();
		arguments.put("newText", replacementText);

		this.actionProvider.invoke(new ANES_URN(
				ReconfigurableTestModule.SETTEXT), arguments);

		assertEquals("Expected new message after invoking action",
				replacementText, this.rtm.getMessage());

		rtm.trigger();
		assertEquals("Unexpected increase in numAlerts after trigger", 1,
				this.rtm.getNumAlerts());

		this.actionProvider.invoke(new ANES_URN(
				ReconfigurableTestModule.RESETMESSAGES), null);
		assertEquals("Unexpected zero alerts after reset", 0,
				this.rtm.getNumAlerts());
	}

	@Test
	public void testImproperActionInvocation() {
		ANES_BUNDLE arguments = new DefaultANES_BUNDLE();

		try {
			this.actionProvider.invoke(null, null);
			fail("Expected InexistentActionID Exception");
		} catch (Exception e) {
			assertEquals(InexistentActionID.class, e.getClass());
		}

		try {
			this.actionProvider.invoke(new ANES_URN("dmns", "bestaatniet"),
					arguments);
			fail("Expected InexistentActionID Exception");
		} catch (Exception e) {
			assertEquals(InexistentActionID.class, e.getClass());
		}

		try {
			this.actionProvider.invoke(new ANES_URN(
					ReconfigurableTestModule.SETTEXT), null);
			fail("Expected ActionInvocationException");
		} catch (Exception e) {
			assertEquals(ActionInvocationException.class, e.getClass());
		}

		try {
			this.actionProvider.invoke(new ANES_URN(
					ReconfigurableTestModule.SETTEXT), arguments);
			fail("Expected ActionInvocationException");
		} catch (Exception e) {
			assertEquals(ActionInvocationException.class, e.getClass());
		}

		String replacementText = "Hello Test!";
		arguments.put("wrongname", replacementText);

		try {
			this.actionProvider.invoke(new ANES_URN(
					ReconfigurableTestModule.SETTEXT), arguments);
			fail("Expected ActionInvocationException");
		} catch (Exception e) {
			assertEquals(ActionInvocationException.class, e.getClass());
		}

		arguments.put("newText", new Integer(5));

		try {
			this.actionProvider.invoke(new ANES_URN(
					ReconfigurableTestModule.SETTEXT), arguments);
			fail("Expected ActionInvocationException");
		} catch (Exception e) {
			assertEquals(ActionInvocationException.class, e.getClass());
		}
	}
}
