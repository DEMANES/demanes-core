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

package eu.artemis.demanes.test.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.ParameterizationException;
import eu.artemis.demanes.exceptions.ParameterizationURNException;
import eu.artemis.demanes.exceptions.ParameterizationValueTypeException;
import eu.artemis.demanes.impl.annotations.DEM_GetParameter;
import eu.artemis.demanes.impl.annotations.DEM_SetParameter;
import eu.artemis.demanes.lib.impl.selfregistry.InvalidAnnotationException;
import eu.artemis.demanes.lib.impl.selfregistry.ParameterizationWrapper;
import eu.artemis.demanes.parameterization.Parameterizable;

public class TestParameterizationWrapper {

	private Parameterizable pd;

	private ANES_URN testURN;

	@Before
	public void initTest() throws URISyntaxException,
			InvalidAnnotationException {
		// Create instance of a class to be tested
		this.pd = ParameterizationWrapper
				.wrap(new ParameterizationWrapperTestClass());
		this.testURN = new ANES_URN("urn:tno:testVal");
	}

	@Test
	public void testListParameter() throws ParameterizationException,
			InvalidAnnotationException {
		assertTrue(ParameterizationWrapper.wrap("").listParameters().isEmpty());

		Set<ANES_URN> list = pd.listParameters();

		assertEquals(1, list.size());
		for (ANES_URN u : list)
			assertEquals(this.testURN, u);
	}

	/**
	 * Test the Set and Get functionality through the parameterization wrapper
	 * 
	 * @throws URISyntaxException
	 * @throws ParameterizationException
	 */
	@Test
	public void testSetGet() throws URISyntaxException,
			ParameterizationException {
		Object setValue = 3.0;
		// Set the value
		pd.setParameter(testURN, setValue);

		// Now get the same parameter back
		Object value = pd.getParameter(testURN);

		// Check if the value is of correct type
		assertEquals(setValue.getClass(), value.getClass());

		// ...and verify whether the value that is set is the same as the one
		// returned by the wrapper
		assertEquals(setValue, setValue.getClass().cast(value));
	}

	/**
	 * Test whether we get a proper Exception when trying to get a parameter
	 * using a non-existing URN or null
	 * 
	 * @throws URISyntaxException
	 * @throws ParameterizationException
	 */
	@Test
	public void testGetterException() throws URISyntaxException {
		// Define the correct corresponding urn of the setter method
		ANES_URN invalidURN = new ANES_URN("urn:tno:nonexisting");

		// URN null is not allowed
		try {
			pd.getParameter(null);
			fail("Expected ParameterizationURNException");
		} catch (ParameterizationException e) {
			assertEquals(ParameterizationURNException.class, e.getClass());
		}

		// Invalid URN should throw an exception
		try {
			pd.getParameter(new ANES_URN("ZomaarEenObject"));
			fail("Expected URISyntaxException");
		} catch (Exception e) {
			assertEquals(URISyntaxException.class, e.getClass());
		}
		
		// URN does not exist so now we expect a ParameterizationURNException
		try {
			pd.getParameter(invalidURN);
			fail("Expected ParameterizationURNException");
		} catch (ParameterizationException e) {
			assertEquals(ParameterizationURNException.class, e.getClass());
		}
	}

	/**
	 * Test whether we get a NullPointerException when trying to get a parameter
	 * using a non-existing URN
	 * 
	 * @throws URISyntaxException
	 * @throws ParameterizationException
	 */
	@Test
	public void testSetterException() throws URISyntaxException {
		// Define the correct corresponding urn of the setter method
		ANES_URN invalidURN = new ANES_URN("urn:tno:nonexisting");

		// URN does not exist so now we expect an exception
		try {
			pd.setParameter(null, 6.0);
			fail("Expected ParameterizationURNException");
		} catch (ParameterizationException e) {
			assertEquals(ParameterizationURNException.class, e.getClass());
		}
		
		// Invalid URN should throw an exception
		try {
			pd.setParameter(new ANES_URN("ZomaarEenObject"), 6.0);
			fail("Expected URISyntaxException");
		} catch (Exception e) {
			assertEquals(URISyntaxException.class, e.getClass());
		}

		// URN does not exist so now we expect an exception
		try {
			pd.setParameter(invalidURN, 6.0);
			fail("Expected ParameterizationURNException");
		} catch (ParameterizationException e) {
			assertEquals(ParameterizationURNException.class, e.getClass());
		}
	}

	/**
	 * Test whether we get a ParameterizationValueTypeException when trying to
	 * set a parameter using a parameter of a different type
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testValueException() throws URISyntaxException {

		try {
			double arg = 5.0;
			pd.setParameter(this.testURN, arg);
		} catch (ParameterizationException e) {
			fail("Did not expect a ParameterizationException");
		}

		try {
			Double arg = 4.1;
			pd.setParameter(this.testURN, arg);
		} catch (ParameterizationException e) {
			fail("Did not expect a ParameterizationException");
		}
		
		try {
			Short arg = 5;
			pd.setParameter(this.testURN, arg);
			fail("Expected a ParameterizationValueTypeException");
		} catch (ParameterizationException e) {
			assertEquals(e.getClass(), ParameterizationValueTypeException.class);
		}

		try {
			float arg = 3.0f;
			pd.setParameter(this.testURN, arg);
			fail("Expected a ParameterizationValueTypeException");
		} catch (ParameterizationException e) {
			assertEquals(e.getClass(), ParameterizationValueTypeException.class);
		}

		try {
			String arg = "Something else";
			pd.setParameter(this.testURN, arg);
			fail("Expected a ParameterizationValueTypeException");
		} catch (ParameterizationException e) {
			assertEquals(e.getClass(), ParameterizationValueTypeException.class);
		}

	}

	/**
	 * ParameterizationWrapperTestClass
	 * 
	 * @author leeuwencjv
	 * @version 0.1
	 * @since 11 jun. 2014
	 * 
	 */
	public class ParameterizationWrapperTestClass {

		private Double testDouble = 0.0;

		@DEM_GetParameter(urn = "urn:tno:testVal")
		public double getJulio() {
			return this.testDouble;
		}

		@DEM_SetParameter(urn = "urn:tno:testVal")
		public void setJulio(double value) {
			this.testDouble = value;
		}

	}

}
