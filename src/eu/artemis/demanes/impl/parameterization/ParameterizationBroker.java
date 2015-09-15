/**
 * File ParameterizationBroker.java
 * Created on 30 apr. 2014 by oliveirafilhojad
 * 
 * This file was created for DEMANES project.
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
package eu.artemis.demanes.impl.parameterization;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import aQute.bnd.annotation.component.Component;
import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.ParameterizationException;
import eu.artemis.demanes.exceptions.ParameterizationURNException;
import eu.artemis.demanes.lib.ParameterizableRegistry;
import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;
import eu.artemis.demanes.parameterization.Parameterizable;

/**
 * ParameterizationBroker
 * 
 * A ParameterizationBroker works as a facade element for a set of
 * parameterizables. Note that it is in principle NOT a component as it should
 * only be used as an internal class within another module.
 * 
 * This class implements the parameterizable interface such that it can be
 * invoked as any other parameterizable. The following results are obtained:
 * 
 * a) When invoking listParameter, this broker will return a list with all the
 * URNs found on its registered parameterizable;
 * 
 * b) When invoking a getParameter, this broker will search which of its
 * registered parameterizable has the referred parameter (by looking up for the
 * URN), and invoke the corresponding getParameter. It returns the corresponding
 * value;
 * 
 * c) When invoking a setParameter, this broker will search which of its
 * registered parameterizables has the referred parameter (by looking up for the
 * URN). It then invokes the corresponding setParameter with the passed value;
 * 
 * @author oliveirafilhojad
 * @version 0.3
 * @since 30 apr. 2014
 * 
 */
@Component(immediate = true, properties = "broker.type=LOCAL")
public final class ParameterizationBroker implements ParameterizableRegistry,
		Parameterizable {

	private final Logger logger = Logger.getLogger("dmns:log");

	/**
	 * Container for the parameterizable in this broker
	 */
	private final Set<Parameterizable> parSet;

	/**
	 * Constructor
	 */
	public ParameterizationBroker() {
		this.parSet = new HashSet<Parameterizable>();
	}

	/**
	 * Finds a parameterizable that owns a parameter with the indicated URN.
	 * 
	 * @return the parameterizable or null if no owner is found.
	 */
	private Parameterizable findURNOwner(ANES_URN urn) {
		for (Parameterizable parameterizable : parSet)
			try {
				if (parameterizable.listParameters().contains(urn))
					return parameterizable;
			} catch (ParameterizationException e) {
				// Do nothing, search next one
			}
		return null;
	}

	/**
	 * Gets the value of the parameter indicated by the urn.
	 * 
	 * The parameter is to be found in one of the registered parameterizable.
	 * Otherwise, a ParameterizationException will be thrown.
	 * 
	 * @see eu.artemis.demanes.parameterization.Parameterizable#setParameter(eu.artemis
	 *      .demanes.datatypes.ANES_URN, java.lang.Object)
	 */
	@Override
	public Object getParameter(ANES_URN urn) throws ParameterizationException {
		UUID uid = UUID.randomUUID();
		logger.trace(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_TRACE, "Param", "Getting Parameter "
						+ urn + " from broker (" + uid + ")"));

		Parameterizable parameterizable = findURNOwner(urn);
		if (parameterizable != null) {
			Object response = parameterizable.getParameter(urn);
			logger.trace(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_TRACE, "Param", "Parameter " + urn
							+ " obtained (" + uid + ")"));

			return response;
		} else {
			logger.error(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_ERROR, "Param",
					"Unable to find parameter " + urn));

			throw new ParameterizationURNException();
		}

	}

	/**
	 * Lists all urns accessible through this broker.
	 * 
	 * @return an array with all the urns accessible through this
	 *         parameterizable.
	 * 
	 * @see eu.artemis.demanes.parameterization.Parameterizable#setParameter(eu.artemis
	 *      .demanes.datatypes.ANES_URN, java.lang.Object)
	 */
	@Override
	public Set<ANES_URN> listParameters() {
		UUID uid = UUID.randomUUID();
		logger.trace(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_TRACE, "Param", "Listing parameters ("
						+ uid + ")"));

		final Set<ANES_URN> urnList = new HashSet<ANES_URN>();
		for (Parameterizable it : this.parSet)
			try {
				urnList.addAll(it.listParameters());
			} catch (ParameterizationException e) {
				logger.error(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_ERROR, "Param",
						"Error Listing parameters", e));
				// Do nothing
			}

		logger.trace(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_TRACE, "Param", "Finished listing ("
						+ uid + ")"));
		return urnList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(Parameterizable p) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Param",
				"Adding Parameterizable " + p));
		this.parSet.add(p);
	}

	/**
	 * Sets the parameter indicated by the urn, with the given value.
	 * 
	 * The parameter is to be found in one of the registered parameterizable.
	 * Otherwise, a ParameterizationException will be thrown.
	 * 
	 * @see eu.artemis.demanes.parameterization.Parameterizable#setParameter(eu.artemis
	 *      .demanes.datatypes.ANES_URN, java.lang.Object)
	 */
	@Override
	public void setParameter(ANES_URN urn, Object value)
			throws ParameterizationException {
		UUID uid = UUID.randomUUID();
		logger.trace(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_TRACE, "Param", "Setting parameter "
						+ urn + " from broker (" + uid + ")"));

		Parameterizable parameterizable = findURNOwner(urn);
		if (parameterizable != null) {
			parameterizable.setParameter(urn, value);
		} else {
			logger.error(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_ERROR, "Param",
					"Setting unknown parameter " + urn));
			throw new ParameterizationURNException();
		}

		logger.trace(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_TRACE, "Param", "Parameter set (" + uid
						+ ")"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregister(Parameterizable p) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Param",
				"Removing Parameterizable " + p));
		this.parSet.remove(p);
	}

}
