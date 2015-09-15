/**
 * File TimedTriggerPolicy.java
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
package eu.artemis.demanes.lib.impl.timedTriggerPolicy;

import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.log4j.Logger;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.metatype.Configurable;
import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;
import eu.artemis.demanes.reconfiguration.TriggerPolicy;
import eu.artemis.demanes.reconfiguration.Triggerable;

/**
 * A TimedTriggerPolicy
 * 
 * @author DEMANES
 * @version 0.1
 * @since 27 nov. 2013
 * 
 */
@Component(immediate = true, designate = TimedTriggerPolicyConfiguration.class, configurationPolicy = ConfigurationPolicy.optional)
public class TimedTriggerPolicy implements TriggerPolicy {

	private final static Logger logger = Logger.getLogger("dmns:log");

	private volatile boolean doTrigger = true;

	private long interval;

	private Timer policyTimer;

	private volatile HashSet<Triggerable> triggers;

	/**
	 * Create a TimedTriggerPolicy that triggers it's triggerables with a fixed
	 * interval. This default constructor taking no arguments will create a
	 * timing policy with the default interval defined in the
	 * DefaultTimedTriggerPolicyConfiguration
	 */
	public TimedTriggerPolicy() {
		this.interval = (new DefaultTimedTriggerPolicyConfiguration())
				.triggerInterval();
		this.triggers = new HashSet<Triggerable>();
	}

	/**
	 * Create a TimedTriggerPolicy that triggers it's triggerables with a fixed
	 * interval.
	 * 
	 * @param intervalms
	 *            The interval in milliseconds
	 */
	public TimedTriggerPolicy(long interval) {
		this.interval = interval;
		this.triggers = new HashSet<Triggerable>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.artemis.demanes.reconfiguration.TriggerPolicy#registerTriggerable(
	 * eu.artemis.demanes.reconfiguration.Triggerable)
	 */
	@Override
	public void registerTriggerable(Triggerable t) {
		this.triggers.add(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.artemis.demanes.reconfiguration.TriggerPolicy#resume()
	 */
	@Override
	public void resume() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reconfig",
				"Resuming Trigger Policy"));

		this.doTrigger = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.artemis.demanes.reconfiguration.TriggerPolicy#start()
	 */
	@Override
	public void start() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reconfig",
				"Starting Trigger Policy"));

		this.policyTimer = new Timer();
		this.doTrigger = true;
		policyTimer.scheduleAtFixedRate(new TimedTriggerTimerTask(), 0,
				this.interval);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.artemis.demanes.reconfiguration.TriggerPolicy#stop()
	 */
	@Override
	@Deactivate
	public void stop() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reconfig",
				"Stopping Trigger Policy"));

		this.doTrigger = false;
		policyTimer.cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.artemis.demanes.reconfiguration.TriggerPolicy#suspend()
	 */
	@Override
	public void suspend() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reconfig",
				"Suspending Trigger Policy"));

		this.doTrigger = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.artemis.demanes.reconfiguration.TriggerPolicy#unregisterTriggerable
	 * (eu.artemis.demanes.reconfiguration.Triggerable)
	 */
	@Override
	public void unregisterTriggerable(Triggerable t) {
		this.triggers.remove(t);
	}

	@Activate
	@Modified
	public void updateConfig(Map<?, ?> properties) {
		TimedTriggerPolicyConfiguration config = Configurable
				.createConfigurable(TimedTriggerPolicyConfiguration.class,
						properties);

		if (config == null)
			config = new DefaultTimedTriggerPolicyConfiguration();

		this.interval = config.triggerInterval();
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Config",
				"Using timing interval " + this.interval));
		
		if (policyTimer != null && this.doTrigger) {
			this.stop();
			this.start();
		}
	}

	/**
	 * Small wrapper function to create a Thread for each Triggerable object so
	 * that they won't have to wait for each other
	 * 
	 * @param triggerObj
	 *            the object to wrap
	 * @return A thread which's run() function is to trigger the Triggerable
	 */
	private static Thread wrapTriggerable(final Triggerable triggerObj) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					UUID uid = UUID.randomUUID();
					logger.trace(new LogEntry(this.getClass().getName(),
							LogConstants.LOG_LEVEL_DEBUG, "Reconfig",
							"Triggering Triggerable " + triggerObj + " (" + uid
									+ ")"));

					triggerObj.trigger();

					logger.trace(new LogEntry(this.getClass().getName(),
							LogConstants.LOG_LEVEL_DEBUG, "Reconfig",
							"Triggerable " + triggerObj + " finished (" + uid
									+ ")"));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}

	private final class TimedTriggerTimerTask extends TimerTask {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			if (doTrigger)
				for (Triggerable t : triggers) {
					wrapTriggerable(t).start();
				}
		}

	}

}