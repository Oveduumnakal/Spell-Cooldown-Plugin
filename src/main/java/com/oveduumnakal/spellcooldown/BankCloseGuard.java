/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import javax.inject.Singleton;

/**
 * Tracks the risky window after the bank closes. The server silently ignores a spell it handles in the tick right
 * after it handles the bank close, so spells are flagged as unsafe for {@link #LOCKOUT_MS} after the close. The
 * failures measured in game all landed inside that window.
 */
@Singleton
public class BankCloseGuard
{
	/** How long after the bank closes casting is flagged as unsafe: one game tick. */
	public static final long LOCKOUT_MS = 600;

	private long safeAtMs;

	/**
	 * Records the bank closing.
	 *
	 * @param nowMs the current time in milliseconds
	 */
	public void onBankClosed(long nowMs)
	{
		safeAtMs = nowMs + LOCKOUT_MS;
	}

	/** Ends the window early, e.g. on logout. */
	public void reset()
	{
		safeAtMs = 0;
	}

	/**
	 * Tells whether a cast now risks being silently ignored.
	 *
	 * @param nowMs the current time in milliseconds
	 * @return true for {@link #LOCKOUT_MS} after the bank closes
	 */
	public boolean isActive(long nowMs)
	{
		return nowMs < safeAtMs;
	}
}
