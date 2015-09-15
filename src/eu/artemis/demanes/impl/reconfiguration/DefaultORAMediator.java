/**
 * File DefaultORAMediator.java
 * Created by DEMANES
 * 
 * This file was created for the DEMANES project 2013.
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
package eu.artemis.demanes.impl.reconfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import eu.artemis.demanes.datatypes.ANES_BUNDLE;
import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.ActionInvocationException;
import eu.artemis.demanes.exceptions.InexistentActionID;
import eu.artemis.demanes.exceptions.InexistentObservationID;
import eu.artemis.demanes.exceptions.ObservationInvocationException;
import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;
import eu.artemis.demanes.reconfiguration.Action;
import eu.artemis.demanes.reconfiguration.ActionProvider;
import eu.artemis.demanes.reconfiguration.Actuator;
import eu.artemis.demanes.reconfiguration.ORAMediator;
import eu.artemis.demanes.reconfiguration.Observation;
import eu.artemis.demanes.reconfiguration.ObservationProvider;
import eu.artemis.demanes.reconfiguration.Observer;
import eu.artemis.demanes.reconfiguration.Reasoner;
import eu.artemis.demanes.reconfiguration.TriggerPolicy;

/**
 * DefaultORAMediator
 * 
 * This is merely an example class implementing all interfaces to create a valid
 * ORAMediator.
 * 
 * @author DEMANES
 * @version 0.1
 * @since 27 nov. 2013
 * 
 */
@Component(immediate = true, provide = { ORAMediator.class })
public final class DefaultORAMediator implements ObservationProvider,
		ActionProvider, ORAMediator {

	private final Logger logger = Logger.getLogger("dmns:log");

	private final Map<ANES_URN, Action> actionMap;

	private final Set<Actuator> actuatorSet;

	private final Map<ANES_URN, Observation> observationMap;

	private final Set<Observer> observerSet;

	private Reasoner reasoner;

	private TriggerPolicy triggerPolicy;

	public DefaultORAMediator() {
		this.actuatorSet = new HashSet<Actuator>();
		this.observerSet = new HashSet<Observer>();

		this.actionMap = new HashMap<ANES_URN, Action>();
		this.observationMap = new HashMap<ANES_URN, Observation>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<ANES_URN> getActions() {
		return this.actionMap.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<ANES_URN> getObservations() {
		return this.observationMap.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(ANES_URN id) throws InexistentObservationID,
			ObservationInvocationException {
		UUID uid = UUID.randomUUID();
		logger.trace(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_TRACE, "Reconfig", "Getting value " + id
						+ " (" + uid + ")"));

		if (observationMap.containsKey(id)) {
			Object value = observationMap.get(id).getValue();
			logger.trace(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_TRACE, "Reconfig", "Obtained value "
							+ id + ": " + value + " (" + uid + ")"));

			return value;
		} else {
			logger.trace(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_TRACE, "Reconfig",
					"Attempt to get unknown value " + id));

			throw new InexistentObservationID(id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void invoke(ANES_URN id, ANES_BUNDLE arguments)
			throws ActionInvocationException, InexistentActionID {
		UUID uid = UUID.randomUUID();
		logger.trace(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_TRACE, "Reconfig", "Invoking action "
						+ id + " (" + uid + ")"));

		if (actionMap.containsKey(id)) {
			this.actionMap.get(id).invoke(arguments);
			
			logger.trace(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_TRACE, "Reconfig", "Finished invoking action "
							+ id + " (" + uid + ")"));
		}
		else {
			logger.error(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_ERROR, "Reconfig",
					"Attempt to invoke unknown Action " + id + " (" + uid + ")"));
			throw new InexistentActionID(id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Reference(type = '*')
	public void registerActuator(Actuator a) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reference",
				"Registering Actuator " + a));

		if (this.actuatorSet.add(a)) {
			// If the set updated, add the actions to the actionMap
			for (Action action : a.getActions())
				actionMap.put(action.getActionID(), action);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Reference(type = '*')
	public void registerObserver(Observer o) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reference",
				"Registering Observer " + o));

		if (this.observerSet.add(o)) {
			// If the set updated, add the observations to the observationMap
			for (Observation observation : o.getObservations())
				observationMap.put(observation.getObservationID(), observation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Reference
	public void setReasoner(Reasoner r) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reference", "Setting Reasoner "
						+ r));

		// First unregister the current reasoner from the triggerPolicy
		if (this.triggerPolicy != null)
			this.triggerPolicy.unregisterTriggerable(this.reasoner);

		// Set the actuation provider of the reasoner to the current ORAMediator
		if (r != null) {
			r.setActuationProvider(this);
			r.setObservationProvider(this);
		}

		// Set current reasoner
		this.reasoner = r;

		// Register the reasoner with the TriggerPolicy
		if (this.triggerPolicy != null)
			this.triggerPolicy.registerTriggerable(r);
	}

	/**
	 * Any trigger policies from active bundles are automatically registered
	 * 
	 * {@inheritDoc}
	 */
	@Override
	@Reference
	public void setTriggeringPolicy(TriggerPolicy t) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reference",
				"Setting TriggerPolicy " + t));

		// First unregister the current reasoner from the old triggerPolicy
		if (this.triggerPolicy != null) {
			this.triggerPolicy.stop();
			this.triggerPolicy.unregisterTriggerable(this.reasoner);
		}

		// Set current reasoner
		this.triggerPolicy = t;

		// Register the reasoner with the new TriggerPolicy
		if (this.triggerPolicy != null) {
			this.triggerPolicy.registerTriggerable(this.reasoner);
			this.triggerPolicy.start();
		}
	}

	@Deactivate
	public void stop() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "LifeCycle", "Stopping module"));

		if (this.triggerPolicy != null) {
			this.triggerPolicy.unregisterTriggerable(this.reasoner);
			this.triggerPolicy.stop();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterActuator(Actuator a) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reference", "Removing Actuator "
						+ a));

		if (this.actuatorSet.remove(a)) {
			// If the set updated, remove all its actions to the actionMap
			for (Action action : a.getActions())
				actionMap.remove(action.getActionID());
			/*
			 * TODO: At this point we can check if another actuator could
			 * provide the action. We may want to rebuild the actionMap to see
			 * if this is true. If so we can add the action immediately again
			 * from that actuator
			 */
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterObserver(Observer o) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reference", "Removing Observer "
						+ o));

		if (this.observerSet.remove(o)) {
			// If the set updated, add the observations to the observationMap
			for (Observation observation : o.getObservations())
				observationMap.remove(observation.getObservationID());
			/*
			 * TODO: In the same way as in unregisterActuator, we could now
			 * rebuild the observationMap
			 */
		}
	}
}