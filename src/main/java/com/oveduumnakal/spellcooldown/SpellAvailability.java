/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.util.Set;
import javax.inject.Singleton;

/**
 * Knows what is in the inventory and so which tracked spells have something to work on. Until the inventory is
 * known, every spell counts as castable, so nothing is greyed out by mistake.
 */
@Singleton
public class SpellAvailability
{
	private Set<Integer> inventory;

	/**
	 * Records the inventory's contents.
	 *
	 * @param itemIds the IDs of every item in the inventory
	 */
	public void update(Set<Integer> itemIds)
	{
		inventory = Set.copyOf(itemIds);
	}

	/** Forgets the inventory, e.g. on logout, so every spell counts as castable again. */
	public void clear()
	{
		inventory = null;
	}

	/**
	 * Tells whether the inventory holds something this spell can work on.
	 *
	 * @param spell the spell
	 * @return true when it has an input, has no inventory rule, or the inventory is not known yet
	 */
	public boolean canCast(SpellDef spell)
	{
		return inventory == null || SpellInputs.hasInput(spell, inventory);
	}
}
