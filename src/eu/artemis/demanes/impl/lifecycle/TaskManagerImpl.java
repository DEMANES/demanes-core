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

package eu.artemis.demanes.impl.lifecycle;

import java.util.HashMap;
import java.util.Map;

import eu.artemis.demanes.datatypes.ANES_BUNDLE;
import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.TaskLifeCycleException;
import eu.artemis.demanes.exceptions.TaskManagerException;
import eu.artemis.demanes.lifecycle.LifeCycleState;
import eu.artemis.demanes.lifecycle.TaskActivator;
import eu.artemis.demanes.lifecycle.TaskManager;

public class TaskManagerImpl implements TaskManager {

	/**
	 * A container for the task registry system
	 */
	private final Map<ANES_URN, TaskActivator> registry;

	public TaskManagerImpl() {
		this.registry = new HashMap<ANES_URN, TaskActivator>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activate(ANES_URN id, ANES_BUNDLE activationParameters)
			throws TaskManagerException {
		exceptOnNullID(id);
		exceptOnNonExistingID(id);
		TaskActivator ta = registry.get(id);
		if ((ta.getState() != LifeCycleState.IDLE)
				|| (ta.getState() != LifeCycleState.UNINSTALLED)) {
			throw new TaskManagerException(
					"Task must be in state INSTALLED or IDLE before activating it.",
					new Throwable(TaskManagerException.STATEFAILURE));
		}
		if (ta.getState() == LifeCycleState.INSTALLED) {
			try {
				ta.create(activationParameters);
			} catch (TaskLifeCycleException e) {
				throw new TaskManagerException(e);
			}
		}
		if (ta.getState() == LifeCycleState.IDLE) {
			try {
				ta.start(activationParameters);
			} catch (TaskLifeCycleException e) {
				throw new TaskManagerException("Activation of task failed.",
						new Throwable(TaskManagerException.STARTFAILED));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deactivate(ANES_URN id, ANES_BUNDLE activationParameters)
			throws TaskManagerException {
		exceptOnNullID(id);
		exceptOnNonExistingID(id);
		TaskActivator ta = registry.get(id);
		if ((ta.getState() != LifeCycleState.ACTIVE)
				|| (ta.getState() != LifeCycleState.IDLE)) {
			throw new TaskManagerException(
					"Task must be in state ACTIVE or IDLE before deactivating it.",
					new Throwable(TaskManagerException.STATEFAILURE));
		}
		if (ta.getState() == LifeCycleState.ACTIVE) {
			try {
				ta.stop(activationParameters);
			} catch (TaskLifeCycleException e) {
				throw new TaskManagerException("Deactivation of task failed.",
						e);
			}
		}
		if (ta.getState() == LifeCycleState.IDLE) {
			try {
				ta.destroy(activationParameters);
			} catch (TaskLifeCycleException e) {
				throw new TaskManagerException("Deactivation of task failed.",
						new Throwable(TaskManagerException.STARTFAILED));
			}
		}

	}

	/**
	 * Throws adequate exception if id is not known in this manager.
	 * 
	 * @param id
	 * @throws TaskManagerException
	 */
	private void exceptOnNonExistingID(ANES_URN id) throws TaskManagerException {
		if (!registry.containsKey(id)) {
			throw new TaskManagerException("Identifer: " + id + " is unknown.",
					new Throwable(TaskManagerException.REGISTERUNKNOW));
		}
	}

	/**
	 * Throws adequate exception if id is null.
	 * 
	 * @param id
	 * @throws TaskManagerException
	 */
	private void exceptOnNullID(ANES_URN id) throws TaskManagerException {
		if (id == null) {
			throw new TaskManagerException("Identifier: " + id + ".",
					new Throwable(TaskManagerException.INVALIDURN));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LifeCycleState getTaskState(ANES_URN id) throws TaskManagerException {
		exceptOnNullID(id);
		return registry.get(id).getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void install(ANES_URN id, TaskActivator ta, ANES_BUNDLE options)
			throws TaskManagerException {
		if (id == null) {
			throw new TaskManagerException("Identifier: " + id + ".",
					new Throwable(TaskManagerException.INVALIDURN));
		} else if (ta == null) {
			throw new TaskManagerException("Task activator reference: " + ta,
					new Throwable(TaskManagerException.INVALIDACTIVATOR));
		}
		if (registry.containsKey(id)) {
			throw new TaskManagerException("The urn " + id
					+ " already exists in the system.", new Throwable(
					TaskManagerException.REGISTEREXISTS));
		}
		try {
			ta.install(options);
		} catch (TaskLifeCycleException e) {
			throw new TaskManagerException(e);
		}
		this.registry.put(id, ta);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void uninstall(ANES_URN id, ANES_BUNDLE options)
			throws TaskManagerException {
		exceptOnNullID(id);
		exceptOnNonExistingID(id);
		TaskActivator ta = registry.get(id);
		if (ta.getState() != LifeCycleState.INSTALLED) {
			throw new TaskManagerException(
					"Task must be in state INSTALLED before uninstalling it.",
					new Throwable(TaskManagerException.STATEFAILURE));
		}
		try {
			ta.uninstall(options);
		} catch (TaskLifeCycleException e) {
			throw new TaskManagerException(e);
		}
		if (ta.getState() == LifeCycleState.UNINSTALLED) {
			this.registry.remove(id);
		}
	}

}
