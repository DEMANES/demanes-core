/**
 * File DefaultANES_BUNDLE.java
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
package eu.artemis.demanes.impl.datatypes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import eu.artemis.demanes.datatypes.ANES_BUNDLE;
import eu.artemis.demanes.exceptions.NonExistentKeyException;
import eu.artemis.demanes.exceptions.TypedRequestException;

/**
 * DefaultANES_BUNDLE
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 10 apr. 2014
 * 
 */
public final class DefaultANES_BUNDLE implements ANES_BUNDLE {

	/**
     * 
     */
	private static final long serialVersionUID = 1884318147243233751L;

	private final HashMap<String, Serializable> contentMap;

	public DefaultANES_BUNDLE() {
		this.contentMap = new HashMap<String, Serializable>();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return a shallow copy of the DefaultANES_BUNDLE
	 */
	@Override
	public DefaultANES_BUNDLE clone() {
		DefaultANES_BUNDLE clone = new DefaultANES_BUNDLE();

		@SuppressWarnings("unchecked")
		HashMap<String, Serializable> t = (HashMap<String, Serializable>) contentMap
				.clone();
		for (Entry<String, Serializable> key : t.entrySet())
			clone.put(key.getKey(), key.getValue());

		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsKey(String key) {
		return this.contentMap.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> boolean containsKey(String key, Class<T> clazz) {
		try {
			return clazz.isAssignableFrom(this.getType(key));
		} catch (NonExistentKeyException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(String key) throws NonExistentKeyException {
		if (this.containsKey(key))
			return this.contentMap.get(key);
		else
			throw new NonExistentKeyException(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T get(String key, Class<T> clazz) throws TypedRequestException,
			NonExistentKeyException {
		T ret = null;

		try {
			Object t = this.get(key);
			// Check the object type
			if (clazz.isAssignableFrom(t.getClass()))
				// Return nicely
				ret = clazz.cast(t);
			else
				// Otherwise throw error
				throw new TypedRequestException(t.getClass(), clazz);
		} catch (ClassCastException e) {
			// Wrap the exception
			throw new TypedRequestException(clazz, e);
		}

		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getType(String key) throws NonExistentKeyException {
		if (this.containsKey(key))
			return this.get(key).getClass();
		else
			throw new NonExistentKeyException(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(String key, Serializable val) {
		this.contentMap.put(key, val);
	}

	@Override
	public String toString() {
		String str = "ANES_BUNDLE: [";
		for (Entry<String, Serializable> entry : contentMap.entrySet())
			str += "[" + entry.getKey() + " => " + entry.getValue() + "] ";
		return str + "]";
	}
}
