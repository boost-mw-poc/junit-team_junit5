/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static org.junit.jupiter.migrationsupport.rules.FailAfterAllHelper.fail;

import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.Verifier;

@SuppressWarnings("removal")
@ExtendWith(ExternalResourceSupport.class)
public class WrongExtendWithForVerifierMethodTests {

	private static boolean afterOfRule1WasExecuted = false;

	private Verifier verifier1 = new Verifier() {

		@Override
		protected void verify() {
			afterOfRule1WasExecuted = true;
		}
	};

	@Rule
	public Verifier getVerifier1() {
		return verifier1;
	}

	@Test
	void testNothing() {
		//needed to start the test process at all
	}

	@AfterAll
	static void afterMethodsOfBothRulesWereExecuted() {
		if (afterOfRule1WasExecuted) {
			fail();
		}
	}

}
