/**
 * File TimedTriggerPolicyConfiguration.java
 * 
 * This file is part of the demanesImplementation project.
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

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * TimedTriggerPolicyConfiguration
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 10 jun. 2014
 * 
 */
@OCD(name = "Configuration for the Timed Triggering Policy")
public interface TimedTriggerPolicyConfiguration {

	@AD(name = "Timed Trigger Interval", description = "This value determines the period between triggers in milliseconds", deflt = DefaultTimedTriggerPolicyConfiguration.DEFAULT_TRIGGER_INTERVAL, required = false)
	public long triggerInterval();
}
