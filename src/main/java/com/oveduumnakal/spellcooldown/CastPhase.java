/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

/** Where a tracked spell is in its click, cast, flash, ready cycle. */
public enum CastPhase
{
	/** Nothing tracked; the spell can be cast. */
	READY,
	/** Clicked, waiting for the next game tick when the server starts the cast. */
	PENDING,
	/** The cast is running. */
	CASTING,
	/** The cast finished; the icon flashes before returning to {@link #READY}. */
	FLASH
}
