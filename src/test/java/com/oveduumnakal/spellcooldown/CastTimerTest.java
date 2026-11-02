/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.util.Optional;
import java.util.OptionalInt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests {@link CastTimer}: the click, cast, flash, ready cycle driven by a fake clock. */
public class CastTimerTest
{
	private static final long START = 10_000;
	private static final long CAST_MS = 8 * CastTimer.TICK_MS;
	private static final long END = START + CAST_MS;
	private static final long CYCLE_MS = CastTimer.FLASH_ON_MS + CastTimer.FLASH_OFF_MS;
	private static final SpellDef FALLBACK = new SpellDef("Test", 1, OptionalInt.empty());
	private static final double DELTA = 1e-9;

	@Test
	public void aClickThenATickMovesFromReadyToPendingToCasting()
	{
		CastTimer timer = new CastTimer(2);
		assertEquals(CastPhase.READY, timer.phase(START - 300));
		assertEquals(Optional.empty(), timer.spell());

		timer.onClick(SpellDef.DEGRIME, START - 300);
		assertEquals(CastPhase.PENDING, timer.phase(START - 200));
		assertEquals(Optional.of(SpellDef.DEGRIME), timer.spell());
		assertTrue(timer.isTimed());

		timer.onTick(START);
		assertEquals(CastPhase.PENDING, timer.phase(START));

		timer.onCastConfirmed(START + 100);
		assertEquals(CastPhase.CASTING, timer.phase(START + 100));
	}

	@Test
	public void pendingShowsNoProgressAndNoTimeLeft()
	{
		CastTimer timer = new CastTimer(2);
		timer.onClick(SpellDef.DEGRIME, START - 300);

		assertEquals(0, timer.progress(START - 100), DELTA);
		assertEquals(0, timer.remainingMs(START - 100));
	}

	@Test
	public void progressRisesLinearlyOverTheWholeCast()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);

		assertEquals(0, timer.progress(START), DELTA);
		assertEquals(0.25, timer.progress(START + CAST_MS / 4), DELTA);
		assertEquals(0.5, timer.progress(START + CAST_MS / 2), DELTA);
		assertEquals(1, timer.progress(END - 1), 0.001);
	}

	@Test
	public void remainingTimeCountsDownToTheEnd()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);

		assertEquals(4800, timer.remainingMs(START));
		assertEquals(2400, timer.remainingMs(START + 2400));
		assertEquals(1, timer.remainingMs(END - 1));
		assertEquals(0, timer.remainingMs(END));
	}

	@Test
	public void aTimedCastFlashesAtItsEndThenReturnsToReady()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);

		assertEquals(CastPhase.CASTING, timer.phase(END - 1));
		assertEquals(CastPhase.FLASH, timer.phase(END));
		assertEquals(CastPhase.FLASH, timer.phase(END + 2 * CYCLE_MS - 1));
		assertEquals(CastPhase.READY, timer.phase(END + 2 * CYCLE_MS));
		assertEquals(Optional.empty(), timer.spell());
	}

	@Test
	public void theFlashStartsAtTheCountdownEndNotWhenFirstAsked()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);

		assertEquals(CastPhase.FLASH, timer.phase(END + 250));
		assertFalse(timer.flashOn(END + 250));
		assertTrue(timer.flashOn(END + CYCLE_MS + 10));
		assertEquals(CastPhase.READY, timer.phase(END + 2 * CYCLE_MS));
	}

	@Test
	public void theFlashAlternatesOnAndOff()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);

		assertFalse(timer.flashOn(END - 1));
		assertTrue(timer.flashOn(END));
		assertTrue(timer.flashOn(END + 199));
		assertFalse(timer.flashOn(END + 200));
		assertFalse(timer.flashOn(END + 349));
		assertTrue(timer.flashOn(END + 350));
		assertTrue(timer.flashOn(END + 549));
		assertFalse(timer.flashOn(END + 550));
		assertFalse(timer.flashOn(END + 699));
		assertFalse(timer.flashOn(END + 700));
	}

	@Test
	public void xpMidCastOnATimedSpellChangesNothing()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);

		timer.onXp(START + 1200);

		assertEquals(CastPhase.CASTING, timer.phase(START + 1200));
		assertEquals(3600, timer.remainingMs(START + 1200));
		assertEquals(CastPhase.FLASH, timer.phase(END));
	}

	@Test
	public void aFallbackSpellStaysCastingUntilItsXpThenFlashes()
	{
		CastTimer timer = casting(FALLBACK, 2);
		assertFalse(timer.isTimed());

		long now = START;
		for (int tick = 1; tick < CastTimer.FALLBACK_TIMEOUT_TICKS; tick++)
		{
			now += CastTimer.TICK_MS;
			timer.onTick(now);
		}

		assertEquals(CastPhase.CASTING, timer.phase(now + 100));
		assertEquals(0, timer.progress(now + 100), DELTA);
		assertEquals(0, timer.remainingMs(now + 100));

		timer.onXp(now + 100);
		assertEquals(CastPhase.FLASH, timer.phase(now + 100));
		assertTrue(timer.flashOn(now + 100));
		assertEquals(CastPhase.READY, timer.phase(now + 100 + 2 * CYCLE_MS));
	}

	@Test
	public void aFallbackSpellWithNoXpGivesUpWithoutFlashing()
	{
		CastTimer timer = casting(FALLBACK, 2);

		long now = START;
		for (int tick = 1; tick < CastTimer.FALLBACK_TIMEOUT_TICKS; tick++)
		{
			now += CastTimer.TICK_MS;
			timer.onTick(now);
		}

		assertEquals(CastPhase.CASTING, timer.phase(now));

		now += CastTimer.TICK_MS;
		timer.onTick(now);
		assertEquals(CastPhase.READY, timer.phase(now));
		assertFalse(timer.flashOn(now));
		assertEquals(Optional.empty(), timer.spell());
	}

	@Test
	public void aCastWithNoAnimationOrXpIsDroppedWithoutFlashing()
	{
		CastTimer timer = new CastTimer(2);
		timer.onClick(SpellDef.DEGRIME, START - 300);
		timer.onTick(START);

		timer.onTick(START + CastTimer.TICK_MS);
		assertEquals(CastPhase.PENDING, timer.phase(START + CastTimer.TICK_MS));

		timer.onTick(START + 2 * CastTimer.TICK_MS);
		assertEquals(CastPhase.READY, timer.phase(START + 2 * CastTimer.TICK_MS));
		assertEquals(CastPhase.READY, timer.phase(END));
		assertFalse(timer.flashOn(END));
	}

	@Test
	public void anUnconfirmedCastShowsAsPendingWithNoCountdown()
	{
		CastTimer timer = new CastTimer(2);
		timer.onClick(SpellDef.DEGRIME, START - 300);
		timer.onTick(START);

		assertEquals(CastPhase.PENDING, timer.phase(START + 400));
		assertEquals(0, timer.remainingMs(START + 400));
		assertEquals(0, timer.progress(START + 400), DELTA);
	}

	@Test
	public void confirmationRevealsTheCountdownFromTheCastStart()
	{
		CastTimer timer = new CastTimer(2);
		timer.onClick(SpellDef.DEGRIME, START - 300);
		timer.onTick(START);

		timer.onCastConfirmed(START + 500);

		assertEquals(CastPhase.CASTING, timer.phase(START + 500));
		assertEquals(CAST_MS - 500, timer.remainingMs(START + 500));
	}

	@Test
	public void anUnconfirmedCastNeverFlashesEvenPastItsEnd()
	{
		SpellDef oneTick = new SpellDef("One tick", 2, OptionalInt.of(1));
		CastTimer timer = new CastTimer(2);
		timer.onClick(oneTick, START - 300);
		timer.onTick(START);

		assertEquals(CastPhase.PENDING, timer.phase(START + 700));
		assertFalse(timer.flashOn(START + 700));
	}

	@Test
	public void aShortCastConfirmedAfterItsEndFlashesFromTheConfirmation()
	{
		SpellDef oneTick = new SpellDef("One tick", 2, OptionalInt.of(1));
		CastTimer timer = new CastTimer(2);
		timer.onClick(oneTick, START - 300);
		timer.onTick(START);

		timer.onCastConfirmed(START + 700);

		assertEquals(CastPhase.FLASH, timer.phase(START + 700));
		assertTrue(timer.flashOn(START + 700));
		assertEquals(CastPhase.FLASH, timer.phase(START + 700 + 2 * CYCLE_MS - 1));
		assertEquals(CastPhase.READY, timer.phase(START + 700 + 2 * CYCLE_MS));
	}

	@Test
	public void anAnimationBeforeTheCastStartsConfirmsIt()
	{
		CastTimer timer = new CastTimer(2);
		timer.onClick(SpellDef.DEGRIME, START - 300);
		timer.onCastConfirmed(START - 200);
		timer.onTick(START);

		timer.onTick(START + CastTimer.TICK_MS);
		timer.onTick(START + 2 * CastTimer.TICK_MS);
		timer.onTick(START + 3 * CastTimer.TICK_MS);

		assertEquals(CastPhase.CASTING, timer.phase(START + 3 * CastTimer.TICK_MS));
		assertEquals(CastPhase.FLASH, timer.phase(END));
	}

	@Test
	public void xpConfirmsATimedCastWithoutEndingIt()
	{
		CastTimer timer = new CastTimer(2);
		timer.onClick(SpellDef.DEGRIME, START - 300);
		timer.onTick(START);
		timer.onXp(START + 100);

		timer.onTick(START + CastTimer.TICK_MS);
		timer.onTick(START + 2 * CastTimer.TICK_MS);

		assertEquals(CastPhase.CASTING, timer.phase(START + 2 * CastTimer.TICK_MS));
		assertEquals(CastPhase.FLASH, timer.phase(END));
	}

	@Test
	public void aConfirmationWhileReadyDoesNotCarryIntoTheNextCast()
	{
		CastTimer timer = new CastTimer(2);
		timer.onCastConfirmed(START - 400);
		timer.onClick(SpellDef.DEGRIME, START - 300);
		timer.onTick(START);
		timer.onTick(START + CastTimer.TICK_MS);
		timer.onTick(START + 2 * CastTimer.TICK_MS);

		assertEquals(CastPhase.READY, timer.phase(START + 2 * CastTimer.TICK_MS));
	}

	@Test
	public void xpBeforeTheCastStartsIsIgnored()
	{
		CastTimer timer = new CastTimer(2);
		timer.onClick(FALLBACK, START - 300);

		timer.onXp(START - 100);

		assertEquals(CastPhase.PENDING, timer.phase(START - 100));
	}

	@Test
	public void aClickWhileCastingChangesNothing()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);

		timer.onClick(FALLBACK, START + 1200);
		timer.onTick(START + 1200);

		assertEquals(CastPhase.CASTING, timer.phase(START + 1200));
		assertEquals(Optional.of(SpellDef.DEGRIME), timer.spell());
		assertEquals(3600, timer.remainingMs(START + 1200));
		assertEquals(CastPhase.FLASH, timer.phase(END));
	}

	@Test
	public void aClickWhilePendingChangesNothing()
	{
		CastTimer timer = new CastTimer(2);
		timer.onClick(SpellDef.DEGRIME, START - 300);
		timer.onClick(FALLBACK, START - 200);

		assertEquals(CastPhase.PENDING, timer.phase(START - 200));
		assertEquals(Optional.of(SpellDef.DEGRIME), timer.spell());
	}

	@Test
	public void aClickWhileFlashingStartsANewCast()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);
		assertEquals(CastPhase.FLASH, timer.phase(END + 100));

		timer.onClick(FALLBACK, END + 100);

		assertEquals(CastPhase.PENDING, timer.phase(END + 100));
		assertEquals(Optional.of(FALLBACK), timer.spell());
		assertFalse(timer.flashOn(END + 100));
	}

	@Test
	public void aClickAfterTheFlashEndsStartsANewCast()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);

		timer.onClick(SpellDef.DEGRIME, END + 2 * CYCLE_MS);

		assertEquals(CastPhase.PENDING, timer.phase(END + 2 * CYCLE_MS));
	}

	@Test
	public void cancelReturnsToReadyFromEveryPhase()
	{
		CastTimer ready = new CastTimer(2);
		ready.cancel();
		assertEquals(CastPhase.READY, ready.phase(START));

		CastTimer pending = new CastTimer(2);
		pending.onClick(SpellDef.DEGRIME, START - 300);
		pending.cancel();
		assertEquals(CastPhase.READY, pending.phase(START));
		assertEquals(Optional.empty(), pending.spell());

		CastTimer castingTimer = casting(SpellDef.DEGRIME, 2);
		castingTimer.cancel();
		assertEquals(CastPhase.READY, castingTimer.phase(START + 1200));
		assertEquals(Optional.empty(), castingTimer.spell());
		assertEquals(CastPhase.READY, castingTimer.phase(END));
		assertFalse(castingTimer.flashOn(END));

		CastTimer flashing = casting(SpellDef.DEGRIME, 2);
		assertEquals(CastPhase.FLASH, flashing.phase(END));
		flashing.cancel();
		assertEquals(CastPhase.READY, flashing.phase(END + 10));
		assertFalse(flashing.flashOn(END + 10));
	}

	@Test
	public void theFlashCountSettingSetsHowLongTheFlashLasts()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 3);

		assertEquals(CastPhase.FLASH, timer.phase(END + 3 * CYCLE_MS - 1));
		assertTrue(timer.flashOn(END + 2 * CYCLE_MS));
		assertEquals(CastPhase.READY, timer.phase(END + 3 * CYCLE_MS));

		CastTimer none = casting(SpellDef.DEGRIME, 0);
		assertEquals(CastPhase.READY, none.phase(END));
	}

	@Test
	public void aNewFlashCountAppliesToTheNextFlashOnly()
	{
		CastTimer timer = casting(SpellDef.DEGRIME, 2);
		assertEquals(CastPhase.FLASH, timer.phase(END));

		timer.setFlashCount(5);
		assertEquals(CastPhase.READY, timer.phase(END + 2 * CYCLE_MS));

		long nextStart = END + 2 * CYCLE_MS + 100;
		timer.onClick(SpellDef.DEGRIME, nextStart - 100);
		timer.onTick(nextStart);
		timer.onCastConfirmed(nextStart);
		long nextEnd = nextStart + CAST_MS;
		assertEquals(CastPhase.FLASH, timer.phase(nextEnd + 5 * CYCLE_MS - 1));
		assertEquals(CastPhase.READY, timer.phase(nextEnd + 5 * CYCLE_MS));
	}

	private static CastTimer casting(SpellDef spell, int flashCount)
	{
		CastTimer timer = new CastTimer(flashCount);
		timer.onClick(spell, START - 300);
		timer.onTick(START);
		timer.onCastConfirmed(START);
		return timer;
	}
}
