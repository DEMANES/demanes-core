/**
 * File TestANES_BUNDLE.java
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
package eu.artemis.demanes.test.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import eu.artemis.demanes.datatypes.ANES_BUNDLE;
import eu.artemis.demanes.exceptions.NonExistentKeyException;
import eu.artemis.demanes.exceptions.TypedRequestException;
import eu.artemis.demanes.impl.datatypes.DefaultANES_BUNDLE;

/**
 * TestANES_BUNDLE
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 10 apr. 2014
 * 
 */
public class TestANES_BUNDLE {

	private ANES_BUNDLE bundle;

	private final String key = "TestKey";

	private final String testText = "This is a test String";

	@Test
	public void containsTest() {
		assertTrue("Test key should be in the bundle", bundle.containsKey(key));
		assertTrue("The bundle should contain a string at the test key",
				bundle.containsKey(key, String.class));
		assertFalse("The bundle should not contain an Integer at the test key",
				bundle.containsKey(key, Integer.class));
		assertFalse("Random keys should not be in the bundle",
				bundle.containsKey("foobar"));
		assertFalse("Random keys should not be in the bundle",
				bundle.containsKey("foobar", String.class));

		String castKey = "CastTestKey";
		bundle.put(castKey, new HashMap<String, Integer>());
		assertTrue("The new key should be in the bundle",
				bundle.containsKey(castKey));
		assertTrue(
				"The new key bundle should contain a HashMap at the test key",
				bundle.containsKey(castKey, HashMap.class));
		assertTrue("The new key bundle should contain a Map at the test key",
				bundle.containsKey(castKey, Map.class));
	}

	@Before
	public void init() {
		bundle = new DefaultANES_BUNDLE();
		bundle.put(key, new String(testText));
	}

	@Test
	public void niceTest() throws TypedRequestException,
			NonExistentKeyException {
		String f = bundle.get(key, String.class);

		assertEquals("Unexpected type returned", bundle.getType(key),
				String.class);
		assertEquals("Input string does not match output string", f, testText);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testClone() throws TypedRequestException,
			NonExistentKeyException {
		// First test is to see if the clone contains the original key
		ANES_BUNDLE clone = bundle.clone();
		assertTrue("Test key should be in the clone", clone.containsKey(key));

		// Second test is to see if adding an object to the bundle does NOT add
		// it to the clone
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(77);
		// Integer aapje = new Integer(77);
		String aapKey = "aapTestKey";
		bundle.put(aapKey, list);
		assertFalse("A new key should not be available in the clone",
				clone.containsKey(aapKey));

		// Last test is to see if the object we put in the bundle itself gets
		// cloned
		ArrayList<Integer> retType = bundle.get(aapKey, ArrayList.class);
		assertEquals("This should not show", retType, list);

		// aapje *= 2;
		list.add(new Integer(88));
		retType = bundle.get(aapKey, ArrayList.class);
		// assertNotEquals("Object not cloned when put in the bundle", retType,
		// list);
	}

	@Test
	public void testIncorrectType() {
		try {
			bundle.get(key, Integer.class);
			fail("A TypedRequestException should have been thrown");
		} catch (Exception e) {
			assertEquals("Unexpected class type returned", e.getClass(),
					TypedRequestException.class);
		}
	}

	@Test
	public void testNonExistenyKey() {
		try {
			bundle.get("shoopdawoop");
			fail("A NonExistentKeyException should have been thrown");
		} catch (Exception e) {
			assertEquals("Unexpected class type returned", e.getClass(),
					NonExistentKeyException.class);
		}

		try {
			bundle.getType("shoopdawoop");
			fail("A NonExistentKeyException should have been thrown");
		} catch (Exception e) {
			assertEquals("Unexpected class type returned", e.getClass(),
					NonExistentKeyException.class);
		}

		try {
			bundle.get("shoopdawoop", Integer.class);
			fail("A NonExistentKeyException should have been thrown");
		} catch (Exception e) {
			assertEquals("Unexpected class type returned", e.getClass(),
					NonExistentKeyException.class);
		}
	}

	// @Test
	// public void serializeTest() {
	// OutputStream os =
	// }

}
