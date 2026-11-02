/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.Test;

import net.runelite.api.gameval.InterfaceID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests the {@link SpellDef} table and its lookups. */
public class SpellDefTest
{
	@Test
	public void everySpellHasItsOwnIconAndName()
	{
		Set<Integer> widgets = new HashSet<>();
		Set<String> names = new HashSet<>();
		for (SpellDef spell : SpellDef.ALL)
		{
			assertTrue(spell.getName(), widgets.add(spell.getWidgetId()));
			assertTrue(spell.getName(), names.add(spell.getName().toLowerCase()));
		}

		assertEquals(35, SpellDef.ALL.size());
	}

	@Test
	public void alchemyIsTargetedWithItsWikiTiming()
	{
		SpellDef high = SpellDef.byWidgetId(InterfaceID.MagicSpellbook.HIGH_ALCHEMY).get();
		SpellDef low = SpellDef.byName("Low Level Alchemy").get();

		assertTrue(high.isTargeted());
		assertEquals(OptionalInt.of(5), high.getCastTicks());
		assertTrue(low.isTargeted());
		assertEquals(OptionalInt.of(3), low.getCastTicks());
	}

	@Test
	public void everyEnchantFallsBackToTheJewelleryEnchantmentsButton()
	{
		for (int level = 1; level <= 7; level++)
		{
			SpellDef enchant = SpellDef.byName("Lvl-" + level + " Enchant").get();
			assertTrue(enchant.isTargeted());
			assertEquals(OptionalInt.of(3), enchant.getCastTicks());
			assertEquals(InterfaceID.MagicSpellbook.ENCHANT_JEWELLERY, enchant.getParentWidgetId());
		}
	}

	@Test
	public void oneClickSpellsHaveNoParentIcon()
	{
		assertFalse(SpellDef.DEGRIME.isTargeted());
		assertEquals(-1, SpellDef.DEGRIME.getParentWidgetId());
		assertEquals(Optional.of(SpellDef.DEGRIME), SpellDef.byWidgetId(InterfaceID.MagicSpellbook.DEGRIME));
	}

	@Test
	public void spellsWithNoWikiTimingFinishOnTheirXpDrop()
	{
		assertEquals(OptionalInt.empty(), ticks("Fertile Soil"));
		assertEquals(OptionalInt.empty(), ticks("Master Reanimation"));
	}

	@Test
	public void nameLookupIgnoresCaseAndUnknownNamesFindNothing()
	{
		SpellDef plankMake = SpellDef.byName("plank make").get();
		assertEquals("Plank Make", plankMake.getName());
		assertEquals(Optional.empty(), SpellDef.byName("Wind Strike"));
		assertEquals(Optional.empty(), SpellDef.byWidgetId(0));
	}

	private static OptionalInt ticks(String name)
	{
		return SpellDef.byName(name)
			.get()
			.getCastTicks();
	}
}
