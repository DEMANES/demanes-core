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

package eu.artemis.demanes.lib.impl.selfregistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.ParameterizationAccessorException;
import eu.artemis.demanes.exceptions.ParameterizationException;
import eu.artemis.demanes.exceptions.ParameterizationLinkException;
import eu.artemis.demanes.exceptions.ParameterizationURNException;
import eu.artemis.demanes.exceptions.ParameterizationValueTypeException;
import eu.artemis.demanes.impl.annotations.DEM_GetParameter;
import eu.artemis.demanes.impl.annotations.DEM_SetParameter;
import eu.artemis.demanes.parameterization.Parameterizable;

/**
 * ParameterizationWrapper
 * 
 * @author DEMANES
 * @version 0.2
 * @since 28 feb. 2014
 * 
 */
public class ParameterizationWrapper {

	/**
	 * @param argumentClass
	 * @param class1
	 * @return
	 */
	private static boolean isAssignableFrom(Class<?> to, Class<?> from) {
		if (to.isPrimitive()) {
			return ParameterizationWrapper.isPrimitiveFormOf(to, from);
		} else if (from.isPrimitive()) {
			return ParameterizationWrapper.isPrimitiveFormOf(from, to);
		} else {
			return to.isAssignableFrom(from);
		}
	}

	/**
	 * @param from
	 * @param to
	 * @return
	 */
	private static boolean isPrimitiveFormOf(Class<?> p, Class<?> o) {
		return (p == boolean.class && o == Boolean.class)
				|| (p == byte.class && o == Byte.class)
				|| (p == char.class && o == Character.class)
				|| (p == short.class && o == Short.class)
				|| (p == int.class && o == Integer.class)
				|| (p == long.class && o == Long.class)
				|| (p == float.class && o == Float.class)
				|| (p == double.class && o == Double.class);
	}

	/**
	 * Create a Parameterizable object out of any object annotated with
	 * {@link DEM_GetParameter} and {@link DEM_SetParameter} annotations.
	 * 
	 * @param o
	 * @return
	 * @throws InvalidAnnotationException
	 */
	public static Parameterizable wrap(Object o)
			throws InvalidAnnotationException {
		// Create the parameterizable
		return new WrappedObjectParameterizable(o);
	}

	/**
	 * Only used as an internally wrapped object. The user should never use this
	 * class.
	 * 
	 * @author coenvl
	 * @version 0.1
	 * @since Apr 30, 2014
	 * 
	 */
	private static final class WrappedObjectParameterizable implements
			Parameterizable {

		/**
		 * The mapping between managed URN identifiers and parameter access
		 * information.
		 */
		private Map<ANES_URN, Method> getterMap;

		private Map<ANES_URN, Method> setterMap;

		/**
		 * The object wrapped by this wrapper
		 */
		private Object wrappedObject;

		/**
		 * Constructs a parameterization wrapper.
		 * 
		 * A parameterization wrapper is an implementation of the DEMANES
		 * Parameterizable interface that wraps an object that offers its
		 * parameters publicly.
		 * 
		 * Publication and access to the parameters is done using ANES_URN
		 * identifiers that represent the parameters themselves, or accessors
		 * (getters and setters) for the parameters.
		 * 
		 * @param obj
		 *            the object to be wrapped.
		 * @param map
		 *            the access information and parameters identifiers for the
		 *            object being wrapped.
		 * @throws InvalidAnnotationException
		 */
		public WrappedObjectParameterizable(Object obj)
				throws InvalidAnnotationException {
			this.wrappedObject = obj;

			this.getterMap = new HashMap<ANES_URN, Method>();
			this.setterMap = new HashMap<ANES_URN, Method>();

			for (Method m : obj.getClass().getMethods()) {
				// For every method, check to see if there is a GET annotation
				if (m.isAnnotationPresent(DEM_GetParameter.class)) {
					DEM_GetParameter anno = m
							.getAnnotation(DEM_GetParameter.class);
					try {
						getterMap.put(new ANES_URN(anno.urn()), m);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}

				// And subsequently do the same for the SET annotations
				if (m.isAnnotationPresent(DEM_SetParameter.class)) {
					if (m.getParameterTypes().length != 1)
						throw new InvalidAnnotationException();

					DEM_SetParameter anno = m
							.getAnnotation(DEM_SetParameter.class);
					try {
						setterMap.put(new ANES_URN(anno.urn()), m);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * eu.demanes.applicationexample.wrappers.Parameterizable#getParameter
		 * (eu .demanes.applicatoinexample.annotations.ANES_URN)
		 */
		@Override
		public Object getParameter(ANES_URN urn)
				throws ParameterizationException {
			if (wrappedObject == null)
				throw new ParameterizationLinkException();

			if (!this.getterMap.containsKey(urn))
				throw new ParameterizationURNException();

			Method m = this.getterMap.get(urn);
			try {
				return m.invoke(this.wrappedObject);
			} catch (IllegalArgumentException e) {
				throw new ParameterizationAccessorException(e);
			} catch (IllegalAccessException e) {
				throw new ParameterizationAccessorException(e);
			} catch (InvocationTargetException e) {
				throw new ParameterizationAccessorException(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * eu.demanes.applicationexample.wrappers.Parameterizable#listParameters
		 * ()
		 */
		@Override
		public Set<ANES_URN> listParameters() {
			Set<ANES_URN> ret = new HashSet<ANES_URN>();

			ret.addAll(getterMap.keySet());
			ret.addAll(setterMap.keySet());

			return ret;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * eu.demanes.applicationexample.wrappers.Parameterizable#setParameter
		 * (eu .demanes.applicatoinexample.annotations.ANES_URN,
		 * java.lang.Object)
		 */
		@Override
		public void setParameter(ANES_URN urn, Object value)
				throws ParameterizationException {
			if (wrappedObject == null)
				throw new ParameterizationLinkException();

			if (!this.setterMap.containsKey(urn))
				throw new ParameterizationURNException();

			Method m = this.setterMap.get(urn);
			Class<?> argumentClass = m.getParameterTypes()[0];
			if (!ParameterizationWrapper.isAssignableFrom(argumentClass,
					value.getClass()))
				throw new ParameterizationValueTypeException();

			try {
				m.invoke(this.wrappedObject, value);
			} catch (IllegalArgumentException e) {
				throw new ParameterizationAccessorException(e);
			} catch (IllegalAccessException e) {
				throw new ParameterizationAccessorException(e);
			} catch (InvocationTargetException e) {
				throw new ParameterizationAccessorException(e);
			}
		}

	}
}
