/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;

/**
 * What each tracked spell needs in the inventory to have anything to work on. A spell with no rule (alchemy, and the
 * spells cast on a farming patch) always counts as having an input.
 */
final class SpellInputs
{
	/** Unenchanted sapphire and opal jewellery, and the Silvthrill rod. */
	private static final Set<Integer> LVL_1_JEWELLERY = Set.of(
		ItemID.SAPPHIRE_RING, ItemID.SAPPHIRE_NECKLACE, ItemID.STRUNG_SAPPHIRE_AMULET, ItemID.JEWL_SAPPHIRE_BRACELET,
		ItemID.OPAL_RING, ItemID.OPAL_NECKLACE, ItemID.STRUNG_OPAL_AMULET, ItemID.OPAL_BRACELET,
		ItemID.BURGH_ROD_COMMAND1);

	/** Unenchanted emerald and jade jewellery, the pre-nature amulet and the emerald sickle (b). */
	private static final Set<Integer> LVL_2_JEWELLERY = Set.of(
		ItemID.EMERALD_RING, ItemID.EMERALD_NECKLACE, ItemID.STRUNG_EMERALD_AMULET, ItemID.JEWL_EMERALD_BRACELET,
		ItemID.JADE_RING, ItemID.JADE_NECKLACE, ItemID.STRUNG_JADE_AMULET, ItemID.JADE_BRACELET,
		ItemID.MAGIC_STRUNG_EMERALD_AMULET, ItemID.SILVER_SICKLE_EMERALD);

	/** Unenchanted ruby and topaz jewellery, and the ruby sickle (b). */
	private static final Set<Integer> LVL_3_JEWELLERY = Set.of(
		ItemID.RUBY_RING, ItemID.RUBY_NECKLACE, ItemID.STRUNG_RUBY_AMULET, ItemID.JEWL_RUBY_BRACELET, ItemID.TOPAZ_RING,
		ItemID.TOPAZ_NECKLACE, ItemID.STRUNG_TOPAZ_AMULET, ItemID.TOPAZ_BRACELET, ItemID.SILVER_SICKLE_RUBY);

	/** Unenchanted diamond jewellery. */
	private static final Set<Integer> LVL_4_JEWELLERY = Set.of(
		ItemID.DIAMOND_RING, ItemID.DIAMOND_NECKLACE, ItemID.STRUNG_DIAMOND_AMULET, ItemID.JEWL_DIAMOND_BRACELET);

	/** Unenchanted dragonstone jewellery. */
	private static final Set<Integer> LVL_5_JEWELLERY = Set.of(
		ItemID.DRAGONSTONE_RING, ItemID.DRAGONSTONE_NECKLACE, ItemID.STRUNG_DRAGONSTONE_AMULET,
		ItemID.JEWL_DRAGONSTONE_BRACELET);

	/** Unenchanted onyx jewellery. */
	private static final Set<Integer> LVL_6_JEWELLERY = Set.of(
		ItemID.ONYX_RING, ItemID.ONYX_NECKLACE, ItemID.STRUNG_ONYX_AMULET, ItemID.JEWL_ONYX_BRACELET);

	/** Unenchanted zenyte jewellery. */
	private static final Set<Integer> LVL_7_JEWELLERY = Set.of(
		ItemID.ZENYTE_RING, ItemID.ZENYTE_NECKLACE, ItemID.ZENYTE_AMULET, ItemID.ZENYTE_BRACELET);

	/** Ores Superheat Item can smelt. */
	private static final Set<Integer> ORES = Set.of(
		ItemID.COPPER_ORE, ItemID.TIN_ORE, ItemID.IRON_ORE, ItemID.SILVER_ORE, ItemID.GOLD_ORE, ItemID.PERFECT_GOLD_ORE,
		ItemID.MITHRIL_ORE, ItemID.ADAMANTITE_ORE, ItemID.RUNITE_ORE, ItemID.BLURITE_ORE, ItemID.QUEST_LUNAR_MAGIC_ORE,
		ItemID.LOVAKITE_ORE, ItemID.LEAD_ORE, ItemID.NICKEL_ORE);

	/** Bones that Bones to Bananas and Bones to Peaches convert. */
	private static final Set<Integer> FRUIT_BONES = Set.of(
		ItemID.BONES, ItemID.BONES_BURNT, ItemID.BAT_BONES, ItemID.BIG_BONES, ItemID.WOLF_BONES,
		ItemID.TBWT_JOGRE_BONES, ItemID.MM_NORMAL_MONKEY_BONES, ItemID.MAGICTRAINING_BONES1,
		ItemID.MAGICTRAINING_BONES2, ItemID.MAGICTRAINING_BONES3, ItemID.MAGICTRAINING_BONES4,
		ItemID.SHADE_BLEACHED_BONES, ItemID.ALAN_BONES, ItemID.MM_SMALL_NINJA_MONKEY_BONES,
		ItemID.MM_MEDIUM_NINJA_MONKEY_BONES, ItemID.MM_NORMAL_GORILLA_MONKEY_BONES,
		ItemID.MM_BEARDED_GORILLA_MONKEY_BONES, ItemID.MM_SMALL_ZOMBIE_MONKEY_BONES,
		ItemID.MM_LARGE_ZOMBIE_MONKEY_BONES, ItemID.MM_SKELETON_BONES);

	/** The unpowered orb every Charge Orb spell uses. */
	private static final Set<Integer> UNPOWERED_ORB = Set.of(
		ItemID.STAFFORB);

	/** Pies Bake Pie can bake. */
	private static final Set<Integer> UNCOOKED_PIES = Set.of(
		ItemID.UNCOOKED_REDBERRY_PIE, ItemID.UNCOOKED_MEAT_PIE, ItemID.UNCOOKED_MUD_PIE, ItemID.UNCOOKED_APPLE_PIE,
		ItemID.UNCOOKED_GARDEN_PIE, ItemID.UNCOOKED_FISH_PIE, ItemID.UNCOOKED_BOTANICAL_PIE,
		ItemID.UNCOOKED_MUSHROOM_PIE, ItemID.UNCOOKED_ADMIRAL_PIE, ItemID.UNCOOKED_DRAGONFRUIT_PIE,
		ItemID.UNCOOKED_WILD_PIE, ItemID.UNCOOKED_SUMMER_PIE, ItemID.HOSIDIUS_SERVERY_UNCOOKED_MEAT_PIE);

	/** Logs Plank Make turns into planks. */
	private static final Set<Integer> PLANK_LOGS = Set.of(
		ItemID.LOGS, ItemID.OAK_LOGS, ItemID.TEAK_LOGS, ItemID.MAHOGANY_LOGS, ItemID.CAMPHOR_LOGS, ItemID.IRONWOOD_LOGS,
		ItemID.ROSEWOOD_LOGS);

	/** The sand Superglass Make needs. */
	private static final Set<Integer> SAND = Set.of(
		ItemID.BUCKET_SAND);

	/** The alkalis Superglass Make takes with the sand. */
	private static final Set<Integer> ALKALI = Set.of(
		ItemID.SODA_ASH, ItemID.SEAWEED, ItemID.GIANT_SEAWEED, ItemID.DORGESH_SWAMP_WEED);

	/** Vessels, clay and unwatered seedlings Humidify fills or wets. */
	private static final Set<Integer> HUMIDIFY_ITEMS = Set.of(
		ItemID.BOWL_EMPTY, ItemID.BUCKET_EMPTY, ItemID.CLAY, ItemID.CUP_EMPTY, ItemID.JUG_EMPTY, ItemID.VIAL_EMPTY,
		ItemID.FISHBOWL_EMPTY, ItemID.WATERING_CAN_0, ItemID.WATERING_CAN_1, ItemID.WATERING_CAN_2,
		ItemID.WATERING_CAN_3, ItemID.WATERING_CAN_4, ItemID.WATERING_CAN_5, ItemID.WATERING_CAN_6,
		ItemID.WATERING_CAN_7, ItemID.ZEAH_WATERINGCAN, ItemID.WATER_SKIN0, ItemID.WATER_SKIN1, ItemID.WATER_SKIN2,
		ItemID.WATER_SKIN3, ItemID.PLANTPOT_ACORN, ItemID.PLANTPOT_WILLOW_SEED, ItemID.PLANTPOT_MAPLE_SEED,
		ItemID.PLANTPOT_YEW_SEED, ItemID.PLANTPOT_MAGIC_TREE_SEED, ItemID.PLANTPOT_APPLE_SEED,
		ItemID.PLANTPOT_BANANA_SEED, ItemID.PLANTPOT_ORANGE_SEED, ItemID.PLANTPOT_CURRY_SEED,
		ItemID.PLANTPOT_PAPAYA_SEED, ItemID.PLANTPOT_PINEAPPLE_SEED, ItemID.PLANTPOT_PALM_SEED,
		ItemID.PLANTPOT_DRAGONFRUIT_SEED, ItemID.PLANTPOT_TEAK_SEED, ItemID.PLANTPOT_MAHOGANY_SEED,
		ItemID.PLANTPOT_CAMPHOR_SEED, ItemID.PLANTPOT_IRONWOOD_SEED, ItemID.PLANTPOT_ROSEWOOD_SEED,
		ItemID.PLANTPOT_CALQUAT_SEED, ItemID.PLANTPOT_CELASTRUS_TREE_SEED, ItemID.PLANTPOT_REDWOOD_TREE_SEED,
		ItemID.PLANTPOT_SPIRIT_TREE_SEED, ItemID.PLANTPOT_CRYSTAL_TREE_SEED);

	/** Amulets String Jewellery strings. */
	private static final Set<Integer> UNSTRUNG_AMULETS = Set.of(
		ItemID.UNSTRUNG_GOLD_AMULET, ItemID.UNSTRUNG_SAPPHIRE_AMULET, ItemID.UNSTRUNG_EMERALD_AMULET,
		ItemID.UNSTRUNG_RUBY_AMULET, ItemID.UNSTRUNG_DIAMOND_AMULET, ItemID.UNSTRUNG_DRAGONSTONE_AMULET,
		ItemID.UNSTRUNG_ONYX_AMULET, ItemID.UNSTRUNG_ZENYTE_AMULET, ItemID.UNSTRUNG_OPAL_AMULET,
		ItemID.UNSTRUNG_JADE_AMULET, ItemID.UNSTRUNG_TOPAZ_AMULET, ItemID.CRYSTALSHARD_NECKLACE_UNSTRUNG,
		ItemID.MM_AMULET_OF_MONKEY_SPEAK_WITHOUT_STRING, ItemID.NOSTRINGSTAR, ItemID.NOSTRINGSNAKE);

	/** Hides Tan Leather tans. */
	private static final Set<Integer> HIDES = Set.of(
		ItemID.COW_HIDE, ItemID.VILLAGE_SNAKE_HIDE, ItemID.TEMPLETREK_SWAMP_SNAKE_HIDE, ItemID.DRAGONHIDE_GREEN,
		ItemID.DRAGONHIDE_BLUE, ItemID.DRAGONHIDE_RED, ItemID.DRAGONHIDE_BLACK);

	/** Flax for Spin Flax. */
	private static final Set<Integer> FLAX = Set.of(
		ItemID.FLAX);

	/** Uncharged or part-charged dragonstone jewellery Recharge Dragonstone tops up. */
	private static final Set<Integer> DRAINED_DRAGONSTONE = Set.of(
		ItemID.AMULET_OF_GLORY, ItemID.AMULET_OF_GLORY_1, ItemID.AMULET_OF_GLORY_2, ItemID.AMULET_OF_GLORY_3,
		ItemID.JEWL_BRACELET_OF_COMBAT, ItemID.JEWL_BRACELET_OF_COMBAT_1, ItemID.JEWL_BRACELET_OF_COMBAT_2,
		ItemID.JEWL_BRACELET_OF_COMBAT_3, ItemID.JEWL_NECKLACE_OF_SKILLS, ItemID.JEWL_NECKLACE_OF_SKILLS_1,
		ItemID.JEWL_NECKLACE_OF_SKILLS_2, ItemID.JEWL_NECKLACE_OF_SKILLS_3, ItemID.TRAIL_AMULET_OF_GLORY,
		ItemID.TRAIL_AMULET_OF_GLORY_1, ItemID.TRAIL_AMULET_OF_GLORY_2, ItemID.TRAIL_AMULET_OF_GLORY_3);

	/** Every tool a Hunter kit gives. */
	private static final Set<Integer> HUNTER_TOOLS = Set.of(
		ItemID.NOOSE_WAND, ItemID.HUNTING_BUTTERFLY_NET, ItemID.HUNTING_OJIBWAY_BIRD_SNARE, ItemID.HUNTING_SNARE,
		ItemID.HUNTING_TEASING_STICK, ItemID.TORCH_UNLIT, ItemID.II_IMPLING_JAR, ItemID.HUNTING_BOX_TRAP,
		ItemID.NET_TRAP);

	/** Grimy herbs Degrime can clean. */
	private static final Set<Integer> GRIMY_HERBS = Set.of(
		ItemID.UNIDENTIFIED_GUAM, ItemID.UNIDENTIFIED_MARENTILL, ItemID.UNIDENTIFIED_TARROMIN,
		ItemID.UNIDENTIFIED_HARRALANDER, ItemID.UNIDENTIFIED_RANARR, ItemID.UNIDENTIFIED_IRIT,
		ItemID.UNIDENTIFIED_AVANTOE, ItemID.UNIDENTIFIED_KWUARM, ItemID.UNIDENTIFIED_CADANTINE,
		ItemID.UNIDENTIFIED_DWARF_WEED, ItemID.UNIDENTIFIED_TORSTOL, ItemID.UNIDENTIFIED_LANTADYME,
		ItemID.UNIDENTIFIED_TOADFLAX, ItemID.UNIDENTIFIED_SNAPDRAGON, ItemID.UNIDENTIFIED_HUASCA,
		ItemID.UNIDENTIFIED_SNAKE_WEED, ItemID.UNIDENTIFIED_ARDRIGAL, ItemID.UNIDENTIFIED_SITO_FOIL,
		ItemID.UNIDENTIFIED_VOLENCIA_MOSS, ItemID.UNIDENTIFIED_ROGUES_PURSE, ItemID.RAIDS_GRIMY_NOXIFER,
		ItemID.RAIDS_GRIMY_GOLPAR, ItemID.RAIDS_GRIMY_BUCHULEAF);

	/** Ashes Demonic Offering takes. */
	private static final Set<Integer> DEMONIC_ASHES = Set.of(
		ItemID.FIENDISH_ASHES, ItemID.VILE_ASHES, ItemID.MALICIOUS_ASHES, ItemID.ABYSSAL_ASHES, ItemID.INFERNAL_ASHES);

	/** Bones Sinister Offering takes. */
	private static final Set<Integer> OFFERING_BONES = Set.of(
		ItemID.BONES, ItemID.BONES_BURNT, ItemID.BAT_BONES, ItemID.BIG_BONES, ItemID.BABYDRAGON_BONES,
		ItemID.DRAGON_BONES, ItemID.WOLF_BONES, ItemID.TBWT_BEAST_BONES, ItemID.TBWT_JOGRE_BONES,
		ItemID.MM_SMALL_NINJA_MONKEY_BONES, ItemID.MM_MEDIUM_NINJA_MONKEY_BONES, ItemID.MM_NORMAL_GORILLA_MONKEY_BONES,
		ItemID.MM_BEARDED_GORILLA_MONKEY_BONES, ItemID.MM_NORMAL_MONKEY_BONES, ItemID.MM_SMALL_ZOMBIE_MONKEY_BONES,
		ItemID.MM_LARGE_ZOMBIE_MONKEY_BONES, ItemID.MM_SKELETON_BONES, ItemID.ZOGRE_BONES,
		ItemID.ZOGRE_ANCESTRAL_BONES_FAYG, ItemID.ZOGRE_ANCESTRAL_BONES_RAURG, ItemID.ZOGRE_ANCESTRAL_BONES_OURG,
		ItemID.DAGANNOTH_KING_BONES, ItemID.WYVERN_BONES, ItemID.LAVA_DRAGON_BONES, ItemID.DRAGON_BONES_SUPERIOR,
		ItemID.WYRM_BONES, ItemID.DRAKE_BONES, ItemID.HYDRA_BONES, ItemID.BABYWYRM_BONES, ItemID.STRYKEWYRM_BONES,
		ItemID.FROST_DRAGON_BONES, ItemID.SHADE_BLEACHED_BONES, ItemID.ALAN_BONES);

	/** Ensouled heads Basic Reanimation can raise. */
	private static final Set<Integer> BASIC_HEADS = Set.of(
		ItemID.ARCEUUS_CORPSE_GOBLIN_INITIAL, ItemID.ARCEUUS_CORPSE_GOBLIN, ItemID.ARCEUUS_CORPSE_MONKEY_INITIAL,
		ItemID.ARCEUUS_CORPSE_MONKEY, ItemID.ARCEUUS_CORPSE_IMP_INITIAL, ItemID.ARCEUUS_CORPSE_IMP,
		ItemID.ARCEUUS_CORPSE_MINOTAUR_INITIAL, ItemID.ARCEUUS_CORPSE_MINOTAUR, ItemID.ARCEUUS_CORPSE_SCORPION_INITIAL,
		ItemID.ARCEUUS_CORPSE_SCORPION, ItemID.ARCEUUS_CORPSE_BEAR_INITIAL, ItemID.ARCEUUS_CORPSE_BEAR,
		ItemID.ARCEUUS_CORPSE_UNICORN_INITIAL, ItemID.ARCEUUS_CORPSE_UNICORN);

	/** Ensouled heads Adept Reanimation can raise. */
	private static final Set<Integer> ADEPT_HEADS = Set.of(
		ItemID.ARCEUUS_CORPSE_DOG_INITIAL, ItemID.ARCEUUS_CORPSE_DOG, ItemID.ARCEUUS_CORPSE_CHAOSDRUID_INITIAL,
		ItemID.ARCEUUS_CORPSE_CHAOSDRUID, ItemID.ARCEUUS_CORPSE_GIANT_INITIAL, ItemID.ARCEUUS_CORPSE_GIANT,
		ItemID.ARCEUUS_CORPSE_OGRE_INITIAL, ItemID.ARCEUUS_CORPSE_OGRE, ItemID.ARCEUUS_CORPSE_ELF_INITIAL,
		ItemID.ARCEUUS_CORPSE_ELF, ItemID.ARCEUUS_CORPSE_TROLL_INITIAL, ItemID.ARCEUUS_CORPSE_TROLL,
		ItemID.ARCEUUS_CORPSE_HORROR_INITIAL, ItemID.ARCEUUS_CORPSE_HORROR);

	/** Ensouled heads Expert Reanimation can raise. */
	private static final Set<Integer> EXPERT_HEADS = Set.of(
		ItemID.ARCEUUS_CORPSE_KALPHITE_INITIAL, ItemID.ARCEUUS_CORPSE_KALPHITE, ItemID.ARCEUUS_CORPSE_DAGANNOTH_INITIAL,
		ItemID.ARCEUUS_CORPSE_DAGANNOTH, ItemID.ARCEUUS_CORPSE_BLOODVELD_INITIAL, ItemID.ARCEUUS_CORPSE_BLOODVELD,
		ItemID.ARCEUUS_CORPSE_TZHAAR_INITIAL, ItemID.ARCEUUS_CORPSE_TZHAAR, ItemID.ARCEUUS_CORPSE_DEMON_INITIAL,
		ItemID.ARCEUUS_CORPSE_DEMON, ItemID.ARCEUUS_CORPSE_HELLHOUND_INITIAL, ItemID.ARCEUUS_CORPSE_HELLHOUND);

	/** Ensouled heads Master Reanimation can raise. */
	private static final Set<Integer> MASTER_HEADS = Set.of(
		ItemID.ARCEUUS_CORPSE_AVIANSIE_INITIAL, ItemID.ARCEUUS_CORPSE_AVIANSIE, ItemID.ARCEUUS_CORPSE_ABYSSAL_INITIAL,
		ItemID.ARCEUUS_CORPSE_ABYSSAL, ItemID.ARCEUUS_CORPSE_DRAGON_INITIAL, ItemID.ARCEUUS_CORPSE_DRAGON);

	/** Rules keyed by spell icon widget ID. */
	private static final Map<Integer, Predicate<Set<Integer>>> RULES = Map.ofEntries(
		Map.entry(InterfaceID.MagicSpellbook.ENCHANT_1, anyOf(LVL_1_JEWELLERY)),
		Map.entry(InterfaceID.MagicSpellbook.ENCHANT_2, anyOf(LVL_2_JEWELLERY)),
		Map.entry(InterfaceID.MagicSpellbook.ENCHANT_3, anyOf(LVL_3_JEWELLERY)),
		Map.entry(InterfaceID.MagicSpellbook.ENCHANT_4, anyOf(LVL_4_JEWELLERY)),
		Map.entry(InterfaceID.MagicSpellbook.ENCHANT_5, anyOf(LVL_5_JEWELLERY)),
		Map.entry(InterfaceID.MagicSpellbook.ENCHANT_6, anyOf(LVL_6_JEWELLERY)),
		Map.entry(InterfaceID.MagicSpellbook.ENCHANT_7, anyOf(LVL_7_JEWELLERY)),
		Map.entry(InterfaceID.MagicSpellbook.SUPERHEAT, anyOf(ORES)),
		Map.entry(InterfaceID.MagicSpellbook.BONES_BANANAS, anyOf(FRUIT_BONES)),
		Map.entry(InterfaceID.MagicSpellbook.BONES_PEACHES, anyOf(FRUIT_BONES)),
		Map.entry(InterfaceID.MagicSpellbook.CHARGE_WATER_ORB, anyOf(UNPOWERED_ORB)),
		Map.entry(InterfaceID.MagicSpellbook.CHARGE_EARTH_ORB, anyOf(UNPOWERED_ORB)),
		Map.entry(InterfaceID.MagicSpellbook.CHARGE_FIRE_ORB, anyOf(UNPOWERED_ORB)),
		Map.entry(InterfaceID.MagicSpellbook.CHARGE_AIR_ORB, anyOf(UNPOWERED_ORB)),
		Map.entry(InterfaceID.MagicSpellbook.BAKE_PIE, anyOf(UNCOOKED_PIES)),
		Map.entry(InterfaceID.MagicSpellbook.PLANK_MAKE, anyOf(PLANK_LOGS)),
		Map.entry(InterfaceID.MagicSpellbook.SUPERGLASS, oneFromEach(SAND, ALKALI)),
		Map.entry(InterfaceID.MagicSpellbook.HUMIDIFY, anyOf(HUMIDIFY_ITEMS)),
		Map.entry(InterfaceID.MagicSpellbook.STRING_JEWEL, anyOf(UNSTRUNG_AMULETS)),
		Map.entry(InterfaceID.MagicSpellbook.TAN_LEATHER, anyOf(HIDES)),
		Map.entry(InterfaceID.MagicSpellbook.SPIN_FLAX, anyOf(FLAX)),
		Map.entry(InterfaceID.MagicSpellbook.RECHARGE_DRAGONSTONE, anyOf(DRAINED_DRAGONSTONE)),
		Map.entry(InterfaceID.MagicSpellbook.HUNTER_KIT, hunterKit(ItemID.DREAM_HUNTER_BOX, HUNTER_TOOLS)),
		Map.entry(InterfaceID.MagicSpellbook.DEGRIME, anyOf(GRIMY_HERBS)),
		Map.entry(InterfaceID.MagicSpellbook.DEMONIC_OFFERING, anyOf(DEMONIC_ASHES)),
		Map.entry(InterfaceID.MagicSpellbook.SINISTER_OFFERING, anyOf(OFFERING_BONES)),
		Map.entry(InterfaceID.MagicSpellbook.REANIMATION_BASIC, anyOf(BASIC_HEADS)),
		Map.entry(InterfaceID.MagicSpellbook.REANIMATION_ADEPT, anyOf(ADEPT_HEADS)),
		Map.entry(InterfaceID.MagicSpellbook.REANIMATION_EXPERT, anyOf(EXPERT_HEADS)),
		Map.entry(InterfaceID.MagicSpellbook.REANIMATION_MASTER, anyOf(MASTER_HEADS)));

	private SpellInputs()
	{
	}

	/**
	 * Tells whether the inventory holds something this spell can work on.
	 *
	 * @param spell the spell
	 * @param inventory the IDs of every item in the inventory
	 * @return true when the spell has an input or has no rule
	 */
	static boolean hasInput(SpellDef spell, Set<Integer> inventory)
	{
		Predicate<Set<Integer>> rule = RULES.get(spell.getWidgetId());
		return rule == null || rule.test(inventory);
	}

	/**
	 * A rule met when the inventory holds any of these items.
	 *
	 * @param items the item IDs
	 * @return the rule
	 */
	private static Predicate<Set<Integer>> anyOf(Set<Integer> items)
	{
		return inventory -> items.stream().anyMatch(inventory::contains);
	}

	/**
	 * A rule met when the inventory holds at least one item from every group.
	 *
	 * @param groups the item ID groups
	 * @return the rule
	 */
	@SafeVarargs
	private static Predicate<Set<Integer>> oneFromEach(Set<Integer>... groups)
	{
		return inventory -> Arrays.stream(groups)
			.allMatch(group -> group.stream().anyMatch(inventory::contains));
	}

	/**
	 * The Hunter Kit rule: castable unless a kit is already held or every tool it contains is already held.
	 *
	 * @param kit the Hunter kit item ID
	 * @param tools the item IDs the kit gives
	 * @return the rule
	 */
	private static Predicate<Set<Integer>> hunterKit(int kit, Set<Integer> tools)
	{
		return inventory -> !inventory.contains(kit) && !inventory.containsAll(tools);
	}
}
