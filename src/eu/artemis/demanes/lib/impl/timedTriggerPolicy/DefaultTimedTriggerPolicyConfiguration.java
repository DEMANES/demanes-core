/**
 * File DefaultTimedTriggerPolicyConfiguration.java
 * 
 * This file is part of the eu.artemis.demanes.lib.TimedTrigger project.
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
package eu.artemis.demanes.lib.impl.timedTriggerPolicy;

/**
 * DefaultTimedTriggerPolicyConfiguration
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 10 jun. 2014
 * 
 */
public class DefaultTimedTriggerPolicyConfiguration implements
		TimedTriggerPolicyConfiguration {

	public static final String DEFAULT_TRIGGER_INTERVAL = "5000";

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.artemis.demanes.lib.TimedTrigger.TimedTriggerPolicyConfiguration#
	 * triggerInterval()
	 */
	@Override
	public long triggerInterval() {
		return Long.parseLong(DEFAULT_TRIGGER_INTERVAL);
	}

}
