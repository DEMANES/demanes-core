/**
 * File ORAFactory.java
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
package eu.artemis.demanes.lib.impl.selfregistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import eu.artemis.demanes.datatypes.ANES_BUNDLE;
import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.ActionInvocationException;
import eu.artemis.demanes.exceptions.MissingArgumentAnnotationException;
import eu.artemis.demanes.exceptions.NonExistentKeyException;
import eu.artemis.demanes.exceptions.ObservationInvocationException;
import eu.artemis.demanes.exceptions.ReconfigurationAnnotationException;
import eu.artemis.demanes.exceptions.ReconfigurationAnnotationURNException;
import eu.artemis.demanes.impl.annotations.DEM_Action;
import eu.artemis.demanes.impl.annotations.DEM_Argument;
import eu.artemis.demanes.impl.annotations.DEM_Observation;
import eu.artemis.demanes.impl.datatypes.DefaultANES_BUNDLE;
import eu.artemis.demanes.reconfiguration.Action;
import eu.artemis.demanes.reconfiguration.Actuator;
import eu.artemis.demanes.reconfiguration.Observation;
import eu.artemis.demanes.reconfiguration.Observer;
import eu.artemis.demanes.reconfiguration.Reasoner;

/**
 * <p>
 * The ORAFactory provides functions to generate the {@link Observer},
 * {@link Reasoner} and {@link Actuator} objects that comply to the DEMANES
 * middleware specification.
 * </p>
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 9 apr. 2014
 * 
 */
public final class ORAFactory {

	/**
	 * <p>
	 * To help the user in creating reconfigurable modules, it can use the this
	 * function to transform his object into a DEMANES compatible
	 * {@link Actuator} object.
	 * </p>
	 * 
	 * <p>
	 * The function will wrap the object, looking for annotations specifying a
	 * point of reconfiguration. It will maintain a collection of these
	 * annotated methods, and complies to the Actuator interface to provide the
	 * actions.
	 * </p>
	 * 
	 * @see Actuator
	 * @see DEM_Action
	 * 
	 * @author leeuwencjv
	 * @version 0.1
	 * @since 9 apr. 2014
	 * 
	 * @param o
	 *            An object with {@link DEM_Action} annotations to indicate the
	 *            actuation points
	 * @return an Actuator object which will provide all the Actions that are
	 *         annotated in the argument object
	 * @throws ReconfigurationAnnotationException
	 */
	public static Actuator createActuator(Object o)
			throws ReconfigurationAnnotationException {
		return new WrappedObjectActuator(o);
	}

	/**
	 * <p>
	 * To help the user in creating reconfigurable modules, it can use this
	 * function to transform his object into a DEMANES compatible
	 * {@link Observer} object.
	 * </p>
	 * 
	 * <p>
	 * The function will wrap the object, looking for annotations specifying a
	 * point where observations are produced. It will maintain a collection of
	 * these annotated methods, and complies to the Observer interface to
	 * provide the observations.
	 * </p>
	 * 
	 * @see Observer
	 * @see DEM_Observation
	 * 
	 * @author leeuwencjv
	 * @version 0.1
	 * @since 9 apr. 2014
	 * 
	 * @param o
	 *            An object with {@link DEM_Observation} annotations to indicate
	 *            the observation points
	 * @return an Observer object which will provide all the Observations that
	 *         are annotated in the argument object
	 * @throws ReconfigurationAnnotationURNException
	 * 
	 */
	public static Observer createObserver(Object o)
			throws ReconfigurationAnnotationURNException {
		return new WrappedObjectObserver(o);
	}

	/**
	 * Only used internally by the {@link ORAFactory}. The WrappedObjectActuator
	 * is the object implementing the Actuator Interface
	 * 
	 * @author leeuwencjv
	 * @version 0.1
	 * @since 9 apr. 2014
	 * 
	 */
	private static final class WrappedObjectActuator implements Actuator {

		private HashSet<Action> actions;

		/**
		 * Creates an Actuator which will provide all actions that are annotated
		 * in the Object
		 * 
		 * @param obj
		 * @throws ReconfigurationAnnotationException
		 */
		private WrappedObjectActuator(Object obj)
				throws ReconfigurationAnnotationException {
			this.actions = new HashSet<Action>();

			for (Method m : obj.getClass().getMethods())
				if (m.isAnnotationPresent(DEM_Action.class)) {
					DEM_Action anno = m.getAnnotation(DEM_Action.class);
					try {
						actions.add(new WrappedAction(obj, m, anno.urn(),
								getArgumentNames(m)));
					} catch (URISyntaxException e) {
						throw new ReconfigurationAnnotationURNException(e);
					}
				}
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Set<Action> getActions() {
			return (Set<Action>) actions.clone();
		}

		/**
		 * @param m
		 * @return
		 * @throws MissingArgumentAnnotationException
		 */
		private String[] getArgumentNames(Method m)
				throws MissingArgumentAnnotationException {
			ArrayList<String> args = new ArrayList<String>();

			Annotation[][] arglist = m.getParameterAnnotations();

			for (Annotation[] argAnnos : arglist) {
				// Iterate over all annotations for one argument
				for (Annotation anno : argAnnos) {
					if (anno instanceof DEM_Argument) {
						// Found the correct annotation
						args.add(((DEM_Argument) anno).name());
						break;
					}
					throw new MissingArgumentAnnotationException();
				}
			}
			return args.toArray(new String[args.size()]);
		}

		/**
		 * The WrappedAction is the object implementing the Action interface.
		 * Specifically WrappedActions are generated during runtime whenever an
		 * annotated object is wrapper by an WrappedObjectActuator.
		 * 
		 * @author leeuwencjv
		 * @version 0.1
		 * @since 9 apr. 2014
		 * 
		 */
		private static final class WrappedAction implements Action {

			private final String[] argumentNames;

			private final ANES_URN urn;

			private final Method wrappedMethod;

			private final Object wrappedObject;

			/**
			 * Create an Action for the WrappedObjectActuator
			 * 
			 * @param o
			 *            the object that is wrapped
			 * @param m
			 *            the method that the Action will invoke
			 * @param urn
			 *            the String that identifies the Action
			 * @param strings
			 *            A string array of the names of the arguments
			 * @throws URISyntaxException
			 */
			private WrappedAction(Object o, Method m, String urn, String[] args)
					throws URISyntaxException {
				this.wrappedObject = o;
				this.wrappedMethod = m;
				this.urn = new ANES_URN(urn);
				this.argumentNames = args;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public ANES_URN getActionID() {
				return this.urn;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void invoke(ANES_BUNDLE arguments)
					throws ActionInvocationException {
				if (arguments == null)
					arguments = new DefaultANES_BUNDLE();

				Object[] args = new Object[this.argumentNames.length];

				for (int i = 0; i < this.argumentNames.length; i++)
					try {
						args[i] = arguments.get(this.argumentNames[i]);
					} catch (NonExistentKeyException e) {
						throw new ActionInvocationException(this.urn, e);
					}

				try {
					wrappedMethod.invoke(this.wrappedObject, args);
				} catch (Exception e) {
					throw new ActionInvocationException(this.urn, e);
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String toString() {
				return this.getClass().getSimpleName() + "$" + this.urn;
			}
		}
	}

	/**
	 * Only used internally by the {@link ORAFactory}. The WrappedObjectObserver
	 * is the object implementing the Actuator Interface
	 * 
	 * @author leeuwencjv
	 * @version 0.1
	 * @since 9 apr. 2014
	 * 
	 */
	private static final class WrappedObjectObserver implements Observer {

		private HashSet<Observation> observations;

		/**
		 * Creates an Observer which will provide all observations that are
		 * annotated in the Object
		 * 
		 * @param obj
		 * @throws ReconfigurationAnnotationURNException
		 */
		private WrappedObjectObserver(Object obj)
				throws ReconfigurationAnnotationURNException {
			this.observations = new HashSet<Observation>();

			for (Method m : obj.getClass().getMethods())
				if (m.isAnnotationPresent(DEM_Observation.class)) {
					DEM_Observation anno = m
							.getAnnotation(DEM_Observation.class);
					try {
						observations.add(new WrappedObservations(obj, m, anno
								.urn()));
					} catch (URISyntaxException e) {
						throw new ReconfigurationAnnotationURNException(e);
					}
				}
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Set<Observation> getObservations() {
			return (Set<Observation>) observations.clone();
		}

		/**
		 * The WrappedObservations is the object implementing the Observation
		 * interface. Specifically WrappedObservations are generated during
		 * runtime whenever an annotated object is wrapper by an
		 * WrappedObjectObserver.
		 * 
		 * @author leeuwencjv
		 * @version 0.1
		 * @since 9 apr. 2014
		 * 
		 */
		private static final class WrappedObservations implements Observation {

			private final ANES_URN urn;

			private final Method wrappedMethod;

			private final Object wrappedObject;

			/**
			 * Create an Observation for the WrappedObjectObserver
			 * 
			 * @param o
			 *            the object that is wrapped
			 * @param m
			 *            the method that the Observation will invoke
			 * @param urn
			 *            the String that identifies the Observation
			 * @throws URISyntaxException
			 */
			private WrappedObservations(Object o, Method m, String urn)
					throws URISyntaxException {
				this.wrappedObject = o;
				this.wrappedMethod = m;
				this.urn = new ANES_URN(urn);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public ANES_URN getObservationID() {
				return this.urn;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Object getValue() throws ObservationInvocationException {
				try {
					return wrappedMethod.invoke(this.wrappedObject);
				} catch (Exception e) {
					throw new ObservationInvocationException(this.urn, e);
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String toString() {
				return this.getClass().getSimpleName() + "$" + this.urn;
			}
		}
	}

}