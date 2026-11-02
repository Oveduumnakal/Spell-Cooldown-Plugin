/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import net.runelite.api.gameval.InterfaceID;

/**
 * One tracked spell: its spellbook widget, its casting speed from the OSRS wiki, and how it is cast. A spell with no
 * wiki timing has empty {@code castTicks} and finishes on its Magic XP drop instead. A targeted spell (alchemy,
 * enchant, Plank Make, ...) is armed by its icon click and cast by the click on its item or object.
 */
public final class SpellDef
{
	/** Casting speed marker for a spell with no wiki timing: it finishes on its Magic XP drop. */
	private static final int XP_DROP = -1;

	/** No parent icon: the spell's own icon is always where it is shown. */
	private static final int NO_PARENT = -1;

	/** Degrime: Arceuus, 70 Magic, cleans every grimy herb. Wiki casting speed is 8 ticks. */
	public static final SpellDef DEGRIME = oneClick("Degrime", InterfaceID.MagicSpellbook.DEGRIME, 8);

	/** Standard spellbook skilling spells. */
	private static final List<SpellDef> STANDARD = List.of(
		targeted("Low Level Alchemy", InterfaceID.MagicSpellbook.LOW_ALCHEMY, 3),
		targeted("High Level Alchemy", InterfaceID.MagicSpellbook.HIGH_ALCHEMY, 5),
		enchant(1, InterfaceID.MagicSpellbook.ENCHANT_1),
		enchant(2, InterfaceID.MagicSpellbook.ENCHANT_2),
		enchant(3, InterfaceID.MagicSpellbook.ENCHANT_3),
		enchant(4, InterfaceID.MagicSpellbook.ENCHANT_4),
		enchant(5, InterfaceID.MagicSpellbook.ENCHANT_5),
		enchant(6, InterfaceID.MagicSpellbook.ENCHANT_6),
		enchant(7, InterfaceID.MagicSpellbook.ENCHANT_7),
		targeted("Superheat Item", InterfaceID.MagicSpellbook.SUPERHEAT, 3),
		oneClick("Bones to Bananas", InterfaceID.MagicSpellbook.BONES_BANANAS, 1),
		oneClick("Bones to Peaches", InterfaceID.MagicSpellbook.BONES_PEACHES, 1),
		targeted("Charge Water Orb", InterfaceID.MagicSpellbook.CHARGE_WATER_ORB, 3),
		targeted("Charge Earth Orb", InterfaceID.MagicSpellbook.CHARGE_EARTH_ORB, 3),
		targeted("Charge Fire Orb", InterfaceID.MagicSpellbook.CHARGE_FIRE_ORB, 3),
		targeted("Charge Air Orb", InterfaceID.MagicSpellbook.CHARGE_AIR_ORB, 3));

	/** Lunar spellbook skilling spells. */
	private static final List<SpellDef> LUNAR = List.of(
		oneClick("Bake Pie", InterfaceID.MagicSpellbook.BAKE_PIE, 3),
		targeted("Plank Make", InterfaceID.MagicSpellbook.PLANK_MAKE, 3),
		oneClick("Superglass Make", InterfaceID.MagicSpellbook.SUPERGLASS, 3),
		oneClick("Humidify", InterfaceID.MagicSpellbook.HUMIDIFY, 4),
		oneClick("String Jewellery", InterfaceID.MagicSpellbook.STRING_JEWEL, 3),
		oneClick("Tan Leather", InterfaceID.MagicSpellbook.TAN_LEATHER, 3),
		oneClick("Spin Flax", InterfaceID.MagicSpellbook.SPIN_FLAX, 5),
		oneClick("Recharge Dragonstone", InterfaceID.MagicSpellbook.RECHARGE_DRAGONSTONE, XP_DROP),
		targeted("Fertile Soil", InterfaceID.MagicSpellbook.FERTILE_SOIL, XP_DROP),
		oneClick("Hunter Kit", InterfaceID.MagicSpellbook.HUNTER_KIT, 3),
		targeted("Cure Plant", InterfaceID.MagicSpellbook.CURE_PLANT, XP_DROP));

	/** Arceuus spellbook skilling spells. The offerings use their shared 9-tick cooldown as the casting speed. */
	private static final List<SpellDef> ARCEUUS = List.of(
		DEGRIME,
		oneClick("Demonic Offering", InterfaceID.MagicSpellbook.DEMONIC_OFFERING, 9),
		oneClick("Sinister Offering", InterfaceID.MagicSpellbook.SINISTER_OFFERING, 9),
		targeted("Resurrect Crops", InterfaceID.MagicSpellbook.RESURRECT_CROPS, XP_DROP),
		targeted("Basic Reanimation", InterfaceID.MagicSpellbook.REANIMATION_BASIC, XP_DROP),
		targeted("Adept Reanimation", InterfaceID.MagicSpellbook.REANIMATION_ADEPT, XP_DROP),
		targeted("Expert Reanimation", InterfaceID.MagicSpellbook.REANIMATION_EXPERT, XP_DROP),
		targeted("Master Reanimation", InterfaceID.MagicSpellbook.REANIMATION_MASTER, XP_DROP));

	/** Every spell the plugin tracks. */
	public static final List<SpellDef> ALL = concat(STANDARD, LUNAR, ARCEUUS);

	private final String name;
	private final int widgetId;
	private final OptionalInt castTicks;
	private final boolean targeted;
	private final int parentWidgetId;

	/**
	 * Creates a one-click spell row with no parent icon.
	 *
	 * @param name the spell name as shown in the spellbook
	 * @param widgetId the packed widget ID of the spell's spellbook icon
	 * @param castTicks the wiki casting speed in game ticks, or empty to finish on the Magic XP drop
	 */
	public SpellDef(String name, int widgetId, OptionalInt castTicks)
	{
		this(name, widgetId, castTicks, false, NO_PARENT);
	}

	/**
	 * Creates a spell row.
	 *
	 * @param name the spell name as shown in the spellbook
	 * @param widgetId the packed widget ID of the spell's spellbook icon
	 * @param castTicks the wiki casting speed in game ticks, or empty to finish on the Magic XP drop
	 * @param targeted true when the spell is cast on an item or object after its icon is clicked
	 * @param parentWidgetId the icon shown instead while the spell sits in a collapsed submenu, or -1
	 */
	public SpellDef(String name, int widgetId, OptionalInt castTicks, boolean targeted, int parentWidgetId)
	{
		this.name = name;
		this.widgetId = widgetId;
		this.castTicks = castTicks;
		this.targeted = targeted;
		this.parentWidgetId = parentWidgetId;
	}

	/**
	 * Returns the spell name.
	 *
	 * @return the spell name as shown in the spellbook
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the spellbook icon's widget ID.
	 *
	 * @return the packed widget ID
	 */
	public int getWidgetId()
	{
		return widgetId;
	}

	/**
	 * Returns the wiki casting speed.
	 *
	 * @return the casting speed in game ticks, or empty when the spell finishes on its XP drop
	 */
	public OptionalInt getCastTicks()
	{
		return castTicks;
	}

	/**
	 * Tells whether the spell is cast on a target after its icon is clicked.
	 *
	 * @return true for targeted spells such as alchemy and enchant
	 */
	public boolean isTargeted()
	{
		return targeted;
	}

	/**
	 * Returns the icon shown instead of the spell's own while it sits in a collapsed submenu.
	 *
	 * @return the parent icon's packed widget ID, or -1 when there is none
	 */
	public int getParentWidgetId()
	{
		return parentWidgetId;
	}

	/**
	 * Finds the tracked spell whose spellbook icon has this widget ID.
	 *
	 * @param widgetId the packed widget ID from a menu click
	 * @return the spell, or empty when the widget is not a tracked spell
	 */
	public static Optional<SpellDef> byWidgetId(int widgetId)
	{
		return ALL.stream()
			.filter(spell -> spell.widgetId == widgetId)
			.findFirst();
	}

	/**
	 * Finds the tracked spell with this name, ignoring case.
	 *
	 * @param name the spell name, e.g. from a menu target with its colour tags removed
	 * @return the spell, or empty when no tracked spell has this name
	 */
	public static Optional<SpellDef> byName(String name)
	{
		return ALL.stream()
			.filter(spell -> spell.name.equalsIgnoreCase(name))
			.findFirst();
	}

	/**
	 * Builds a spell that casts when its icon is clicked.
	 *
	 * @param name the spell name
	 * @param widgetId the spell icon's widget ID
	 * @param ticks the wiki casting speed, or {@link #XP_DROP}
	 * @return the spell row
	 */
	private static SpellDef oneClick(String name, int widgetId, int ticks)
	{
		return new SpellDef(name, widgetId, timing(ticks), false, NO_PARENT);
	}

	/**
	 * Builds a spell that casts on an item or object after its icon is clicked.
	 *
	 * @param name the spell name
	 * @param widgetId the spell icon's widget ID
	 * @param ticks the wiki casting speed, or {@link #XP_DROP}
	 * @return the spell row
	 */
	private static SpellDef targeted(String name, int widgetId, int ticks)
	{
		return new SpellDef(name, widgetId, timing(ticks), true, NO_PARENT);
	}

	/**
	 * Builds a jewellery enchant spell: cast on an item, 3 ticks, shown on the Jewellery Enchantments button while its
	 * submenu is collapsed.
	 *
	 * @param level the enchant level, 1 to 7
	 * @param widgetId the spell icon's widget ID
	 * @return the spell row
	 */
	private static SpellDef enchant(int level, int widgetId)
	{
		return new SpellDef("Lvl-" + level + " Enchant", widgetId, OptionalInt.of(3), true,
			InterfaceID.MagicSpellbook.ENCHANT_JEWELLERY);
	}

	/**
	 * Turns a table casting speed into the stored form.
	 *
	 * @param ticks the wiki casting speed, or {@link #XP_DROP}
	 * @return the ticks, or empty for {@link #XP_DROP}
	 */
	private static OptionalInt timing(int ticks)
	{
		return ticks == XP_DROP ? OptionalInt.empty() : OptionalInt.of(ticks);
	}

	/**
	 * Joins the spellbook tables into one read-only list.
	 *
	 * @param books the spellbook tables
	 * @return every spell, in table order
	 */
	@SafeVarargs
	private static List<SpellDef> concat(List<SpellDef>... books)
	{
		List<SpellDef> all = new ArrayList<>();
		for (List<SpellDef> book : books)
			all.addAll(book);

		return Collections.unmodifiableList(all);
	}
}
