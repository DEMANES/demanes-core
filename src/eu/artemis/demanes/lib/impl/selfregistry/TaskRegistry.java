/**
 * File TaskRegistry.java
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
package eu.artemis.demanes.lib.impl.selfregistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import eu.artemis.demanes.exceptions.ParameterizationException;
import eu.artemis.demanes.exceptions.ReconfigurationAnnotationException;
import eu.artemis.demanes.exceptions.TaskRegistryException;
import eu.artemis.demanes.impl.annotations.DEM_TaskProperties;
import eu.artemis.demanes.lib.ParameterizableRegistry;
import eu.artemis.demanes.lib.SelfRegister;
import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;
import eu.artemis.demanes.parameterization.Parameterizable;
import eu.artemis.demanes.reconfiguration.Actuator;
import eu.artemis.demanes.reconfiguration.Observer;

/**
 * TaskRegistry
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 17 jun. 2014
 * 
 */
@Component
public class TaskRegistry {

	private final Logger logger = Logger.getLogger("dmns:log");

	private HashMap<SelfRegister, HashSet<ServiceRegistration<?>>> serviceMap;

	private HashMap<SelfRegister, ServiceTracker> trackerMap;

	@Activate
	public void start() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "LifeCycle",
				"Activating module"));
	}
	
	public TaskRegistry() {
		this.serviceMap = new HashMap<SelfRegister, HashSet<ServiceRegistration<?>>>();
		this.trackerMap = new HashMap<SelfRegister, ServiceTracker>();
	}

	/**
	 * Register the DEMANES task in the task registry. It will create the
	 * Parameterizable objects, Actuators and Observers that can be generated
	 * based on the Annotations in the user class, and subsequently register
	 * those wrapped objects in the appropriate places.
	 * 
	 * If the Task is already present in the registry, an Exception will be
	 * thrown
	 * 
	 * @param t
	 *            The task to add to the registry
	 * @throws TaskRegistryException
	 */
	@Reference(type = '*')
	public void registerTask(SelfRegister t) throws TaskRegistryException {
		try {
			logger.debug(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_DEBUG, "Reference",
					"Registering task " + t));

			if (serviceMap.containsKey(t)) {
				logger.warn(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_WARN,
						"Unable to add task because it already exists"));

				throw new TaskRegistryException(
						"Unable to add task because it already exists");
			}

			// Create a set to contain the service registrations
			HashSet<ServiceRegistration<?>> serviceSet = new HashSet<ServiceRegistration<?>>();

			// Create the DEMANES objects
			Observer obs = ORAFactory.createObserver(t);
			Actuator act = ORAFactory.createActuator(t);

			BundleContext context = FrameworkUtil.getBundle(this.getClass())
					.getBundleContext();

			addParameterizableServiceTracker(context, t);

			Hashtable<String, String> props = new Hashtable<String, String>();
			// Register the observations in the OSGI environment
			if (!obs.getObservations().isEmpty()) {
				logger.trace(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_TRACE,
						"Dynamically adding Observer from task " + t));

				serviceSet.add(context.registerService(
						Observer.class.getName(), obs, props));
			}

			// Register the actions
			if (!act.getActions().isEmpty()) {
				logger.trace(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_TRACE,
						"Dynamically adding Actuator from task " + t));

				serviceSet.add(context.registerService(
						Actuator.class.getName(), act, props));
			}

			// Add the service Set to the map
			serviceMap.put(t, serviceSet);

			// TODO: In the next step, we should also create an automatic
			// TaskActivator which should call the @Install, @Start similar to
			// the OSGI @Activate etc...

		} catch (ReconfigurationAnnotationException e) {
			logger.error(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_ERROR,
					"Invalid Reconfiguration Annotation in class", e));

			throw new TaskRegistryException(
					"Invalid Reconfiguration Annotation in class", e);
		}
	}

	/**
	 * This function instantiates a ServiceTracker for the Parameterizable
	 * object that is created from the Task. The ServiceTracker searches for
	 * ParameterizableRegistries with (possibly) the correct broker.type
	 * property
	 * 
	 * @param context
	 * @param t
	 * @throws TaskRegistryException
	 */
	private void addParameterizableServiceTracker(BundleContext context,
			SelfRegister t) throws TaskRegistryException {
		try {
			// Wrap the task into a parameterizable object
			Parameterizable par = ParameterizationWrapper.wrap(t);

			if (!par.listParameters().isEmpty()) {
				logger.trace(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_TRACE,
						"Dynamically adding Parameterizable from task " + t));

				// Get the task properties
				if (t.getClass().isAnnotationPresent(DEM_TaskProperties.class)) {
					DEM_TaskProperties anno = t.getClass().getAnnotation(
							DEM_TaskProperties.class);

					// Create a filter to find the correct
					// ParameterizableRegistry
					// for this task
					String filter;
					if (anno.brokerType() != "")
						filter = "(&(broker.type=" + anno.brokerType() + ")("
								+ Constants.OBJECTCLASS + "="
								+ ParameterizableRegistry.class.getName()
								+ "))";
					else if (anno.brokerType() == "*")
						filter = "(" + Constants.OBJECTCLASS + "="
								+ ParameterizableRegistry.class.getName() + ")";
					else
						return;

					logger.trace(new LogEntry(this.getClass().getName(),
							LogConstants.LOG_LEVEL_TRACE,
							"Dynamic Parameterizable will register at broker.type "
									+ anno.brokerType()));

					ServiceTracker st = new ServiceTracker(context,
							context.createFilter(filter),
							new InternalServiceTracker(par));
					st.open();
					trackerMap.put(t, st);
				}

			}

		} catch (InvalidAnnotationException e) {
			logger.error(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_ERROR,
					"Error creating Parameterizable from class", e));
			
			throw new TaskRegistryException(
					"Error creating Parameterizable from class", e);
		} catch (ParameterizationException e) {
			logger.error(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_ERROR,
					"Unable to list parameters from dynamic parameterizable", e));
			
			throw new TaskRegistryException(
					"Unable to list parameters from dynamic parameterizable", e);
		} catch (InvalidSyntaxException e) {
			logger.error(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_ERROR,
					"Invalid ServiceTrackerFilter syntax", e));
			
			throw new TaskRegistryException(
					"Invalid ServiceTrackerFilter syntax", e);
		}
	}

	/**
	 * Unregisters the task from the service registry. If the task is not
	 * registered in the first place, an exception will be thrown.
	 * 
	 * @param t
	 *            The task to unregister
	 * @throws TaskRegistryException
	 */
	public void unregisterTask(SelfRegister t) throws TaskRegistryException {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reference",
				"Unregistering task " + t));

		if (!serviceMap.containsKey(t)) {
			logger.warn(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_WARN,
					"Unable to remove task because it already exists"));

			throw new TaskRegistryException(
					"Unable to unregister task because it doesn't exist");
		}

		HashSet<ServiceRegistration<?>> serviceSet = serviceMap.get(t);

		// Unregister all services that the task registered
		for (ServiceRegistration<?> sr : serviceSet)
			sr.unregister();

		// Remove it from the registry
		serviceMap.remove(t);

		// Also remove the service tracker if it was created
		if (trackerMap.containsKey(t)) {
			trackerMap.get(t).close();
			trackerMap.remove(t);
		}
	}

	/**
	 * The InternalServiceTracker is used to get the ParameterizableRegistry to
	 * which a parameterizable (from a user-Task) should potentially be added.
	 * 
	 * @author leeuwencjv
	 * @version 0.1
	 * @since 17 jun. 2014
	 * 
	 */
	@SuppressWarnings("rawtypes")
	private final class InternalServiceTracker implements
			ServiceTrackerCustomizer {

		private final Parameterizable par;

		/**
		 * @param par
		 */
		public InternalServiceTracker(Parameterizable par) {
			this.par = par;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.
		 * osgi.framework.ServiceReference)
		 */
		@Override
		public Object addingService(ServiceReference reference) {
			@SuppressWarnings("unchecked")
			Object o = FrameworkUtil.getBundle(getClass()).getBundleContext()
					.getService(reference);

			// Extra check to see if the object is actually a
			// ParameterizableRegistry
			if (ParameterizableRegistry.class.isAssignableFrom(o.getClass()))
				((ParameterizableRegistry) o).register(par);

			return o;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		@Override
		public void modifiedService(ServiceReference reference, Object service) {
			// Do nothing?
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		@Override
		public void removedService(ServiceReference reference, Object service) {
			@SuppressWarnings("unchecked")
			Object o = FrameworkUtil.getBundle(getClass()).getBundleContext()
					.getService(reference);

			// Extra check to see if the object is actually a
			// ParameterizableRegistry
			if (ParameterizableRegistry.class.isAssignableFrom(o.getClass()))
				((ParameterizableRegistry) o).unregister(par);
		}

	}
}
