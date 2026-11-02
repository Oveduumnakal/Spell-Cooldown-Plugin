/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests {@link BankCloseGuard}: the unsafe window after the bank closes. */
public class BankCloseGuardTest
{
	private static final long CLOSE = 10_000;

	@Test
	public void startsInactive()
	{
		assertFalse(new BankCloseGuard().isActive(CLOSE));
	}

	@Test
	public void staysActiveForOneTickAfterTheClose()
	{
		BankCloseGuard guard = new BankCloseGuard();
		guard.onBankClosed(CLOSE);

		assertTrue(guard.isActive(CLOSE));
		assertTrue(guard.isActive(CLOSE + 348));
		assertTrue(guard.isActive(CLOSE + BankCloseGuard.LOCKOUT_MS - 1));
		assertFalse(guard.isActive(CLOSE + BankCloseGuard.LOCKOUT_MS));
	}

	@Test
	public void closingAgainRestartsTheWindow()
	{
		BankCloseGuard guard = new BankCloseGuard();
		guard.onBankClosed(CLOSE);
		guard.onBankClosed(CLOSE + 400);

		assertTrue(guard.isActive(CLOSE + 900));
		assertFalse(guard.isActive(CLOSE + 1000));
	}

	@Test
	public void resetEndsTheWindow()
	{
		BankCloseGuard guard = new BankCloseGuard();
		guard.onBankClosed(CLOSE);

		guard.reset();

		assertFalse(guard.isActive(CLOSE + 100));
	}
}
