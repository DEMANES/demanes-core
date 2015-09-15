/**
 * File JavaIOTranslator.java
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
package eu.artemis.demanes.lib.impl.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import aQute.bnd.annotation.component.Component;
import eu.artemis.demanes.lib.Serializer;
import eu.artemis.demanes.lib.exceptions.SerializationException;

/**
 * JavaIOTranslator
 * 
 * @author coenvl
 * @version 0.1
 * @since May 8, 2014
 * 
 */
@Component
public class JavaIOSerializer implements Serializer {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.artemis.demanes.lib.Serializer#deserialize(byte[])
	 */
	@Override
	public Object deserialize(byte[] b) throws SerializationException {
		try {
			// Create an input stream based on the input bytes
			ByteArrayInputStream bis = new ByteArrayInputStream(b);
			ObjectInputStream ois = new ObjectInputStream(bis);
			
			// Get an object from the bytes
			Object obj = ois.readObject();

			// Close the streams
			ois.close();
			bis.close();

			return obj;
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.artemis.demanes.lib.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(Object obj) throws SerializationException {
		try {
			// Put the object in a stream
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos;

			oos = new ObjectOutputStream(bos);

			// Get the byte representation of the object
			oos.writeObject(obj);
			oos.close();

			byte[] ret = bos.toByteArray();
			bos.close();

			return ret;
		} catch (IOException e) {
			throw new SerializationException(e);
		}
	}

}
