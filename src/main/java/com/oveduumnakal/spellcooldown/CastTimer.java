/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.util.Optional;

/**
 * Tracks one spell through its click, cast, flash, ready cycle. Time is always passed in as {@code nowMs}, never read
 * from a clock, so tests drive it with plain numbers. A spell with wiki timing flashes when its countdown ends and
 * ignores XP; a spell without it flashes on its Magic XP drop, or gives up after {@link #FALLBACK_TIMEOUT_TICKS}
 * ticks with no XP. Every call comes from the client thread.
 */
public final class CastTimer
{
	/** Length of one game tick in milliseconds. */
	public static final long TICK_MS = 600;

	/** Ticks an XP-fallback spell waits for its XP drop before it gives up with no flash. */
	public static final int FALLBACK_TIMEOUT_TICKS = 20;

	/**
	 * Ticks after the cast starts within which the cast must be confirmed by a cast animation or an XP drop. A click
	 * the server rejected (no herbs, no runes, moving) never confirms, so its cover clears with no flash.
	 */
	public static final int CONFIRM_TIMEOUT_TICKS = 2;

	/** Milliseconds the icon is lit during each flash cycle. */
	public static final long FLASH_ON_MS = 200;

	/** Milliseconds the icon is dark during each flash cycle. */
	public static final long FLASH_OFF_MS = 150;

	private int flashCount;
	private CastPhase phase = CastPhase.READY;
	private SpellDef spell;
	private long castStartMs;
	private int castTicks;
	private boolean confirmed;
	private long confirmedMs;
	private long flashStartMs;
	private int activeFlashCount;

	/**
	 * Creates a timer in {@link CastPhase#READY}.
	 *
	 * @param flashCount how many on/off cycles a flash lasts; negative counts as 0
	 */
	public CastTimer(int flashCount)
	{
		setFlashCount(flashCount);
	}

	/**
	 * Sets how many on/off cycles a flash lasts. A flash already running keeps its old count.
	 *
	 * @param flashCount how many cycles the next flash lasts; negative counts as 0
	 */
	public void setFlashCount(int flashCount)
	{
		this.flashCount = Math.max(0, flashCount);
	}

	/**
	 * Records a click on a tracked spell. Starts tracking when the timer is {@link CastPhase#READY} or
	 * {@link CastPhase#FLASH}, since the flash means the spell is ready again. A click while pending or casting is
	 * ignored, because the server rejects a second cast while one is running.
	 *
	 * @param spell the spell that was clicked
	 * @param nowMs the current time in milliseconds
	 */
	public void onClick(SpellDef spell, long nowMs)
	{
		advance(nowMs);
		if (phase != CastPhase.READY && phase != CastPhase.FLASH)
			return;

		this.spell = spell;
		phase = CastPhase.PENDING;
		confirmed = false;
	}

	/**
	 * Records a game tick. A pending click starts casting now. A cast not confirmed within
	 * {@link #CONFIRM_TIMEOUT_TICKS} ticks is dropped with no flash, and an XP-fallback spell gives up with no flash
	 * after {@link #FALLBACK_TIMEOUT_TICKS} ticks with no XP.
	 *
	 * @param nowMs the current time in milliseconds
	 */
	public void onTick(long nowMs)
	{
		advance(nowMs);
		if (phase == CastPhase.PENDING)
		{
			phase = CastPhase.CASTING;
			castStartMs = nowMs;
			castTicks = 0;
		}
		else if (phase == CastPhase.CASTING)
		{
			castTicks++;
			if (!confirmed && castTicks >= CONFIRM_TIMEOUT_TICKS)
				cancel();
			else if (!isTimed() && castTicks >= FALLBACK_TIMEOUT_TICKS)
				cancel();
		}
	}

	/**
	 * Records the player starting a cast animation, which confirms a pending or casting spell really cast. Until then
	 * the cast shows as {@link CastPhase#PENDING} with no countdown, so a click the server ignored never shows one.
	 *
	 * @param nowMs the current time in milliseconds
	 */
	public void onCastConfirmed(long nowMs)
	{
		if (phase == CastPhase.PENDING || phase == CastPhase.CASTING)
			confirm(nowMs);
	}

	/**
	 * Records a Magic XP drop. It confirms a casting spell. An XP-fallback spell also starts flashing now; spells
	 * with wiki timing keep their countdown, so XP never ends them early.
	 *
	 * @param nowMs the current time in milliseconds
	 */
	public void onXp(long nowMs)
	{
		advance(nowMs);
		if (phase != CastPhase.CASTING)
			return;

		confirm(nowMs);
		if (!isTimed())
			startFlash(nowMs);
	}

	/** Drops whatever is tracked and returns to {@link CastPhase#READY} with no flash. */
	public void cancel()
	{
		phase = CastPhase.READY;
		spell = null;
		castStartMs = 0;
		castTicks = 0;
		confirmed = false;
		confirmedMs = 0;
		flashStartMs = 0;
		activeFlashCount = 0;
	}

	/**
	 * Gives the phase at this time, first moving a finished countdown to {@link CastPhase#FLASH} and a finished flash
	 * to {@link CastPhase#READY}. A cast not yet confirmed by its animation or XP shows as {@link CastPhase#PENDING}.
	 *
	 * @param nowMs the current time in milliseconds
	 * @return the current phase
	 */
	public CastPhase phase(long nowMs)
	{
		advance(nowMs);
		return phase == CastPhase.CASTING && !confirmed ? CastPhase.PENDING : phase;
	}

	/**
	 * Gives the spell being tracked.
	 *
	 * @return the tracked spell, or empty in {@link CastPhase#READY}
	 */
	public Optional<SpellDef> spell()
	{
		return Optional.ofNullable(spell);
	}

	/**
	 * Tells whether the tracked spell has a wiki casting speed.
	 *
	 * @return true when the tracked spell has {@code castTicks}; false for an XP-fallback spell or nothing tracked
	 */
	public boolean isTimed()
	{
		return spell != null && spell.getCastTicks().isPresent();
	}

	/**
	 * Gives the time left on a timed cast.
	 *
	 * @param nowMs the current time in milliseconds
	 * @return milliseconds until the countdown ends, never below 0; 0 outside a timed {@link CastPhase#CASTING}
	 */
	public long remainingMs(long nowMs)
	{
		advance(nowMs);
		if (phase != CastPhase.CASTING || !isTimed() || !confirmed)
			return 0;

		return Math.max(0, castEndMs() - nowMs);
	}

	/**
	 * Gives how far through its countdown a timed cast is.
	 *
	 * @param nowMs the current time in milliseconds
	 * @return elapsed over total time, from 0 to 1; 0 outside a timed {@link CastPhase#CASTING}
	 */
	public double progress(long nowMs)
	{
		advance(nowMs);
		if (phase != CastPhase.CASTING || !isTimed() || !confirmed)
			return 0;

		double fraction = (double) (nowMs - castStartMs) / castDurationMs();
		return Math.min(1, Math.max(0, fraction));
	}

	/**
	 * Tells whether the flash is lit at this time.
	 *
	 * @param nowMs the current time in milliseconds
	 * @return true during the lit part of each flash cycle; false outside {@link CastPhase#FLASH}
	 */
	public boolean flashOn(long nowMs)
	{
		advance(nowMs);
		if (phase != CastPhase.FLASH)
			return false;

		long intoCycle = Math.max(0, nowMs - flashStartMs) % (FLASH_ON_MS + FLASH_OFF_MS);
		return intoCycle < FLASH_ON_MS;
	}

	/**
	 * Applies the transitions that happen by time alone.
	 *
	 * @param nowMs the current time in milliseconds
	 */
	private void advance(long nowMs)
	{
		if (phase == CastPhase.CASTING && isTimed() && confirmed && nowMs >= castEndMs())
			startFlash(Math.max(castEndMs(), confirmedMs));

		if (phase == CastPhase.FLASH && nowMs >= flashStartMs + activeFlashCount * (FLASH_ON_MS + FLASH_OFF_MS))
			cancel();
	}

	/**
	 * Marks the cast as really happening, the first time only.
	 *
	 * @param nowMs when it was confirmed, in milliseconds
	 */
	private void confirm(long nowMs)
	{
		if (confirmed)
			return;

		confirmed = true;
		confirmedMs = nowMs;
	}

	/**
	 * Starts the flash with the flash count set right now.
	 *
	 * @param startMs when the flash starts, in milliseconds
	 */
	private void startFlash(long startMs)
	{
		phase = CastPhase.FLASH;
		flashStartMs = startMs;
		activeFlashCount = flashCount;
	}

	/**
	 * Gives the full length of a timed cast.
	 *
	 * @return the cast ticks times {@link #TICK_MS}
	 */
	private long castDurationMs()
	{
		return spell.getCastTicks().getAsInt() * TICK_MS;
	}

	/**
	 * Gives when a timed cast ends.
	 *
	 * @return the cast start plus its full length, in milliseconds
	 */
	private long castEndMs()
	{
		return castStartMs + castDurationMs();
	}
}
