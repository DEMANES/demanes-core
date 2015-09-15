/**
 * File TestDemo.java
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
package eu.artemis.demanes.test.lib;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.artemis.demanes.lib.impl.timedTriggerPolicy.TimedTriggerPolicy;
import eu.artemis.demanes.reconfiguration.TriggerPolicy;
import eu.artemis.demanes.reconfiguration.Triggerable;
import eu.artemis.demanes.test.ReconfigurableTestModule;

/**
 * TestDemo
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 9 apr. 2014
 * 
 */
public class TestTriggerPolicy implements Triggerable {

	private TriggerPolicy tp;
	
	private ReconfigurableTestModule rtm;
	
	@Before
	public void init() {
		this.tp = new TimedTriggerPolicy(1000);
		this.rtm = new ReconfigurableTestModule();
		tp.registerTriggerable(rtm);
	}
	
	@Test
	public void stateTest() throws InterruptedException {
		tp.start();
		Thread.sleep(4500);
		assertEquals(5, rtm.getNumAlerts());
		
		tp.suspend();
		Thread.sleep(4000);
		assertEquals(5, rtm.getNumAlerts());
		
		tp.resume();
		Thread.sleep(2000);
		assertEquals(7, rtm.getNumAlerts());	
		
		tp.stop();
		Thread.sleep(2000);
		assertEquals(7, rtm.getNumAlerts());
		
		tp.resume();
		Thread.sleep(2000);
		assertEquals(7, rtm.getNumAlerts());
		
		tp.start();
		Thread.sleep(2000);
		assertEquals(9, rtm.getNumAlerts());
	}
	
	@Test
	public void delayerTest() throws InterruptedException {
		tp.registerTriggerable(this);
		tp.start();
		Thread.sleep(7500);
		assertEquals(8, rtm.getNumAlerts());
	}

	@After
	public void stop() {
		tp.stop();
	}
	
	/* (non-Javadoc)
	 * @see eu.artemis.demanes.reconfiguration.Triggerable#trigger()
	 */
	@Override
	public void trigger() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			System.err.println("Ouch");
		}
		System.out.println("Huh wat?");
	}

}
