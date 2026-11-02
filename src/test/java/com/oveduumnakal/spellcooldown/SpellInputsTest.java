/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.util.Set;

import org.junit.Test;

import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests {@link SpellInputs}: which spells have something in the inventory to work on. */
public class SpellInputsTest
{
	private static final Set<Integer> EMPTY = Set.of();

	@Test
	public void degrimeNeedsAGrimyHerb()
	{
		assertFalse(SpellInputs.hasInput(SpellDef.DEGRIME, Set.of(ItemID.SNAPDRAGON)));
		assertTrue(SpellInputs.hasInput(SpellDef.DEGRIME, Set.of(ItemID.UNIDENTIFIED_SNAPDRAGON)));
	}

	@Test
	public void hunterKitIsBlockedByAKitInTheInventory()
	{
		SpellDef kit = spell(InterfaceID.MagicSpellbook.HUNTER_KIT);

		assertTrue(SpellInputs.hasInput(kit, EMPTY));
		assertFalse(SpellInputs.hasInput(kit, Set.of(ItemID.DREAM_HUNTER_BOX)));
	}

	@Test
	public void hunterKitIsBlockedOnlyWhenEveryToolIsHeld()
	{
		SpellDef kit = spell(InterfaceID.MagicSpellbook.HUNTER_KIT);
		Set<Integer> allTools = Set.of(ItemID.NOOSE_WAND, ItemID.HUNTING_BUTTERFLY_NET,
			ItemID.HUNTING_OJIBWAY_BIRD_SNARE, ItemID.HUNTING_SNARE, ItemID.HUNTING_TEASING_STICK, ItemID.TORCH_UNLIT,
			ItemID.II_IMPLING_JAR, ItemID.HUNTING_BOX_TRAP, ItemID.NET_TRAP);

		assertTrue(SpellInputs.hasInput(kit, Set.of(ItemID.NOOSE_WAND, ItemID.HUNTING_BOX_TRAP)));
		assertFalse(SpellInputs.hasInput(kit, allTools));
	}

	@Test
	public void superglassNeedsSandAndAnAlkali()
	{
		SpellDef superglass = spell(InterfaceID.MagicSpellbook.SUPERGLASS);

		assertFalse(SpellInputs.hasInput(superglass, Set.of(ItemID.BUCKET_SAND)));
		assertFalse(SpellInputs.hasInput(superglass, Set.of(ItemID.GIANT_SEAWEED)));
		assertTrue(SpellInputs.hasInput(superglass, Set.of(ItemID.BUCKET_SAND, ItemID.GIANT_SEAWEED)));
	}

	@Test
	public void eachEnchantLevelNeedsItsOwnJewellery()
	{
		SpellDef lvl1 = spell(InterfaceID.MagicSpellbook.ENCHANT_1);
		SpellDef lvl5 = spell(InterfaceID.MagicSpellbook.ENCHANT_5);
		Set<Integer> sapphireRing = Set.of(ItemID.SAPPHIRE_RING);

		assertTrue(SpellInputs.hasInput(lvl1, sapphireRing));
		assertFalse(SpellInputs.hasInput(lvl5, sapphireRing));
	}

	@Test
	public void spellsWithNoRuleAlwaysHaveAnInput()
	{
		assertTrue(SpellInputs.hasInput(spell(InterfaceID.MagicSpellbook.HIGH_ALCHEMY), EMPTY));
		assertTrue(SpellInputs.hasInput(spell(InterfaceID.MagicSpellbook.FERTILE_SOIL), EMPTY));
	}

	@Test
	public void everyInventorySpellIsGreyWithAnEmptyInventory()
	{
		for (SpellDef spell : SpellDef.ALL)
		{
			boolean ruleFree = spell.getWidgetId() == InterfaceID.MagicSpellbook.LOW_ALCHEMY
				|| spell.getWidgetId() == InterfaceID.MagicSpellbook.HIGH_ALCHEMY
				|| spell.getWidgetId() == InterfaceID.MagicSpellbook.FERTILE_SOIL
				|| spell.getWidgetId() == InterfaceID.MagicSpellbook.CURE_PLANT
				|| spell.getWidgetId() == InterfaceID.MagicSpellbook.RESURRECT_CROPS
				|| spell.getWidgetId() == InterfaceID.MagicSpellbook.HUNTER_KIT;
			assertTrue(spell.getName(), SpellInputs.hasInput(spell, EMPTY) == ruleFree);
		}
	}

	@Test
	public void availabilityTreatsAnUnknownInventoryAsCastable()
	{
		SpellAvailability availability = new SpellAvailability();
		assertTrue(availability.canCast(SpellDef.DEGRIME));

		availability.update(EMPTY);
		assertFalse(availability.canCast(SpellDef.DEGRIME));

		availability.clear();
		assertTrue(availability.canCast(SpellDef.DEGRIME));
	}

	private static SpellDef spell(int widgetId)
	{
		return SpellDef.byWidgetId(widgetId)
			.get();
	}
}
