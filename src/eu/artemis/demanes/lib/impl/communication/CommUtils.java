/**
 * File CommUtils.java
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
package eu.artemis.demanes.lib.impl.communication;

import java.nio.ByteBuffer;
import java.util.Map;

import eu.artemis.demanes.datatypes.ANES_URN;
import eu.artemis.demanes.exceptions.ParameterizationValueTypeException;

/**
 * CommUtils
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 11 feb. 2015
 *
 */
public final class CommUtils {

	public static final byte END_OF_MESSAGE = 0x0A;

	/**
	 * Convert a byte array to a string of hexadecimal characters
	 * 
	 * @param val
	 * @return
	 */
	public static String asHex(byte[] val) {
		StringBuilder sb = new StringBuilder();
		for (byte b : val)
			sb.append(String.format("%02X ", b));
		return sb.toString();
	}

	/**
	 * @param buf
	 * @return
	 */
	public static String toString(ByteBuffer buf) {
		StringBuilder sb = new StringBuilder();
		StringBuilder hb = new StringBuilder();

		byte b;

		buf.mark();
		while (buf.hasRemaining()) {
			b = buf.get();
			hb.append(String.format("%02X ", b));
			sb.append(String.format("%c", (char) b));
		}
		buf.reset();

		sb.append(" (").append(hb).append(')');
		return sb.toString();
	}

	/**
	 * Public function to read an object from a byte buffer
	 * 
	 * @param <T>
	 * 
	 * @param input
	 * @return
	 * @throws PayloadParsingException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readObject(ByteBuffer input, Class<T> clazz)
			throws PayloadParsingException {
		int length = input.get();

		if (clazz == byte.class || clazz == Byte.class) {
			CommUtils.checkClassLength(clazz, length, 1);
			return (T) new Byte(input.get());
		} else if (clazz == int.class || clazz == Integer.class) {
			CommUtils.checkClassLength(clazz, length, 4);
			return (T) new Integer(input.getInt());
		} else if (clazz == long.class || clazz == Long.class) {
			CommUtils.checkClassLength(clazz, length, 8);
			return (T) new Long(input.getLong());
		} else if (clazz == float.class || clazz == Float.class) {
			CommUtils.checkClassLength(clazz, length, 4);
			return (T) new Float(input.getFloat());
		} else if (clazz == double.class || clazz == Double.class) {
			CommUtils.checkClassLength(clazz, length, 8);
			return (T) new Double(input.getDouble());
		} else if (clazz == char.class || clazz == Character.class) {
			CommUtils.checkClassLength(clazz, length, 2);
			return (T) new Character(input.getChar());
		} else if (clazz == String.class) {
			return (T) new String(readBytes(input, length));
		} else if (clazz == ByteBuffer.class) {			
			ByteBuffer bb = ByteBuffer.allocate(length);
			bb.put(input.array(), input.position(), length);
			return (T) bb;
		} else {
			throw new PayloadParsingException(
					"Unable to read object type " + clazz.getName());
		}
	}

	static public byte[] serialize(Object value)
			throws PayloadSerializationException {
		ByteBuffer buf;

		Class<?> clazz = value.getClass();
		if (clazz == byte.class || clazz == Byte.class) {
			buf = ByteBuffer.allocate(2).put((byte) 1).put((Byte) value);
		} else if (clazz == short.class || clazz == Short.class) {
			buf = ByteBuffer.allocate(3).put((byte) 2).putShort((Short) value);
		} else if (clazz == int.class || clazz == Integer.class) {
			buf = ByteBuffer.allocate(5).put((byte) 4).putInt((Integer) value);
		} else if (clazz == long.class || clazz == Long.class) {
			buf = ByteBuffer.allocate(9).put((byte) 8).putLong((Long) value);
		} else if (clazz == float.class || clazz == Float.class) {
			buf = ByteBuffer.allocate(5).put((byte) 4).putFloat((Float) value);
		} else if (clazz == double.class || clazz == Double.class) {
			buf = ByteBuffer.allocate(9).put((byte) 8)
					.putDouble((Double) value);
		} else if (clazz == char.class || clazz == Character.class) {
			buf = ByteBuffer.allocate(3).put((byte) 2)
					.putChar((Character) value);
		} else if (clazz == String.class) {
			String str = (String) value;
			buf = ByteBuffer.allocate(str.length() + 1)
					.put((byte) str.length()).put(str.getBytes());
		} else if (clazz == ByteBuffer.class) {
			ByteBuffer bb = (ByteBuffer) value;
			buf = ByteBuffer.allocate(bb.remaining() + 1)
					.put((byte) bb.remaining()).put(bb);
		} else {
			throw new PayloadSerializationException(
					"Unable to serialize value of type " + clazz.getName());
		}

		return buf.array();
	}

	/**
	 * @param value
	 * @return
	 * @throws ParameterizationValueTypeException
	 */
	static public byte[] serialize4(Object value)
			throws PayloadSerializationException {
		ByteBuffer buf = ByteBuffer.allocate(4);
		Class<?> clazz = value.getClass();
		if (clazz == byte.class || clazz == Byte.class) {
			buf.putShort((short) 0).put((byte) 0).put((Byte) value);
		} else if (clazz == short.class || clazz == Short.class) {
			buf.putShort((short) 0).putShort((Short) value);
		} else if (clazz == int.class || clazz == Integer.class) {
			buf.putInt((Integer) value);
		} else if (clazz == long.class || clazz == Long.class) {
			throw new PayloadSerializationException(
					"Can not serialize long into 4 bytes!");
		} else if (clazz == float.class || clazz == Float.class) {
			buf.putFloat((Float) value);
		} else if (clazz == double.class || clazz == Double.class) {
			throw new PayloadSerializationException(
					"Can not serialize double into 4 bytes!");
		} else if (clazz == char.class || clazz == Character.class) {
			buf.putShort((short) 0).putChar((Character) value);
		} else if (clazz == String.class) {
			throw new PayloadSerializationException(
					"Can not serialize string into 4 bytes!");
		} else if (clazz == ByteBuffer.class) {
			throw new PayloadSerializationException(
					"Can not serialize ByteBuffer into 4 bytes!");
		} else {
			throw new PayloadSerializationException(
					"Unable to serialize value of type " + clazz.getName());
		}

		return buf.array();
	}
	
	private static void checkClassLength(Class<?> clazz, int value,
			int expected) throws PayloadParsingException {
		if (value != expected)
			throw new PayloadParsingException("Expected length " + expected
					+ " while parsing " + clazz.getName() + ", instead found "
					+ value);
	}

	/**
	 * @param buf
	 * @return
	 * @throws PayloadParsingException
	 */
	public static String ReadStringFromByteBuffer(ByteBuffer buf)
			throws PayloadParsingException {
		return new String(readBytes(buf));
	}

	/**
	 * Public function to read an object from a byte buffer
	 * 
	 * @param input
	 * @return
	 * @throws PayloadParsingException
	 */
	public static byte[] readBytes(ByteBuffer input)
			throws PayloadParsingException {
		int length = input.get();
		return readBytes(input, length);
	}

	/**
	 * Internal function to read a specific number of bytes from a buffer and
	 * return it as a byte array
	 * 
	 * @param b
	 * @param length
	 * @return
	 * @throws PayloadParsingException
	 */
	public static byte[] readBytes(ByteBuffer b, int length)
			throws PayloadParsingException {
		if (length < 0)
			throw new PayloadParsingException("Invalid length in buffer");
		else if (length > b.remaining())
			throw new PayloadParsingException(
					"Length longer than buffer length");

		byte[] buf = new byte[length];
		b.get(buf);

		return buf;
	}
	
	/**
	 * Generate a JSON string representing a Map
	 * 
	 * @param map
	 * @return
	 */
	public static String asJSON(Map<ANES_URN, Object> map) {
		// Build JSON of properties
		StringBuilder sb = new StringBuilder("{");
		boolean appendcomma = false;
		
		for (ANES_URN key : map.keySet()) {
			if (appendcomma)
				sb.append(", ");
			else
				appendcomma = true;
				
			sb.append("\"").append(key.toString()).append("\":");

			Object value = map.get(key);
			if (value instanceof Number)
				sb.append(value.toString());
			else
				sb.append("\"").append(value.toString()).append("\"");
		}
		sb.append("}");
		
		return sb.toString();
	}
}
