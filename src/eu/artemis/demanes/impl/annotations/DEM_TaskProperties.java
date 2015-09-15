/**
 * File DEM_TaskProperties.java
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
package eu.artemis.demanes.impl.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DEM_TaskProperties
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 17 jun. 2014
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface DEM_TaskProperties {

	/**
	 * This value determines to what ParameterizableRegistries the Parameters in
	 * this task are added.
	 * 
	 * The brokerType refers to the "broker.type" property of the
	 * ParameterizableRegistry service. If left empty the Parameterizable
	 * objects will not be added to any ParameterizableRegistry. If any value is
	 * provided this value has to match the "broker.type" property.
	 * Alternatively if the value is set to "*", the parameters will be added to
	 * all available ParameterizableRegistries.
	 * 
	 * @return
	 */
	public String brokerType() default "";
}
