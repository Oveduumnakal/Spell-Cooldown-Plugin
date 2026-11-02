/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.awt.Color;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

/** Settings for Spell Cooldown: how the cover, the countdown and the finish flash are drawn on the spell icon. */
@ConfigGroup(SpellCooldownConfig.GROUP)
public interface SpellCooldownConfig extends Config
{
	/** Config group key, shared with the plugin so both agree on where settings are stored. */
	String GROUP = "spellcooldown";

	/**
	 * Colour of the cover drawn over the spell icon while the spell casts.
	 *
	 * @return the cover colour, with alpha
	 */
	@Alpha
	@ConfigItem(
		keyName = "coverColor",
		name = "Cover colour",
		description = "Colour of the cover over the spell icon while the spell casts. It shrinks from the top down "
			+ "as the cast runs out.",
		position = 0
	)
	default Color coverColor()
	{
		return new Color(0, 0, 0, 150);
	}

	/**
	 * Colour of the flash drawn over the spell icon when the cast finishes.
	 *
	 * @return the flash colour, with alpha
	 */
	@Alpha
	@ConfigItem(
		keyName = "flashColor",
		name = "Flash colour",
		description = "Colour the spell icon flashes when the cast finishes and the spell is ready again.",
		position = 1
	)
	default Color flashColor()
	{
		return new Color(0, 255, 0, 140);
	}

	/**
	 * Whether the seconds left are drawn in the middle of the icon.
	 *
	 * @return true to draw the countdown
	 */
	@ConfigItem(
		keyName = "showNumber",
		name = "Show countdown",
		description = "Draws the seconds left in the middle of the spell icon while it casts.",
		position = 2
	)
	default boolean showNumber()
	{
		return true;
	}

	/**
	 * How many times the icon flashes when the cast finishes.
	 *
	 * @return the number of flashes
	 */
	@Range(min = 1, max = 5)
	@ConfigItem(
		keyName = "flashCount",
		name = "Flash count",
		description = "How many times the spell icon flashes when the cast finishes.",
		position = 3
	)
	default int flashCount()
	{
		return 2;
	}

	/**
	 * Whether spell icons are tinted for the risky ticks after the bank closes.
	 *
	 * @return true to show the bank-close warning
	 */
	@ConfigItem(
		keyName = "bankCloseWarning",
		name = "Bank-close warning",
		description = "Tints spell icons right after the bank closes. A spell clicked then is often silently ignored "
			+ "by the game. Cast once the tint clears.",
		position = 4
	)
	default boolean bankCloseWarning()
	{
		return true;
	}

	/**
	 * Whether spell icons are greyed out while the inventory has nothing for the spell to work on.
	 *
	 * @return true to grey out spells with nothing to cast on
	 */
	@ConfigItem(
		keyName = "greyWithoutInputs",
		name = "Grey out with nothing to cast on",
		description = "Greys out a spell while your inventory has nothing for it to work on, e.g. Degrime with no "
			+ "grimy herbs, or Hunter Kit when you already have a kit or every tool in it.",
		position = 5
	)
	default boolean greyWithoutInputs()
	{
		return true;
	}
}
