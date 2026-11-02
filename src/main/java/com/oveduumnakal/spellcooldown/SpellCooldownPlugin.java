/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

/** Entry point: feeds spell clicks, ticks, XP drops and failure messages into the cast timer and draws the icon. */
@PluginDescriptor(
	name = "Spell Cooldown",
	description = "Greys out a spell icon while it casts, wipes the grey away with a smooth countdown, and flashes "
		+ "green when the spell is ready again",
	tags = {"spell", "cooldown", "degrime", "arceuus", "magic", "herblore", "timer", "overlay"}
)
public class SpellCooldownPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(SpellCooldownPlugin.class);

	/**
	 * Game messages that mean the click did not start a cast. Matched as prefixes. A failure with no known message
	 * is still caught: the cast is never confirmed, so {@link CastTimer} drops it.
	 */
	private static final List<String> FAILURE_MESSAGES = List.of(
		"Your last spell is still in progress!",
		"You do not have enough ",
		"You don't have any suitable herbs to clean.",
		"You need to cast superheat item on ore.",
		"Even this spell is not hot enough to heat this item.",
		"You aren't holding any bones!",
		"The bones you're holding aren't suitable for peachifying.",
		"You don't have any flax to spin.",
		"Why do you want to create a kit?",
		"You can only carry one Hunter kit at a time.",
		"Composting it isn't going to make it any bigger.",
		"No, that would be silly.",
		"No, I think I'd better compost it the old-fashioned way.",
		"It's growing just fine.",
		"That's not diseased.",
		"Bins don't often get diseased.",
		"The weeds are healthy enough already.",
		"There's nothing there to cure.",
		"It says 'Cure' not 'Resurrect'.",
		"You already resurrected these crops once.");

	/**
	 * Ticks after a click during which a failure message cancels the cast. Later messages belong to a different
	 * click, e.g. a spam-click on a cast that is already running.
	 */
	private static final int FAILURE_WINDOW_TICKS = 1;

	/** Menu actions that use an armed spell on a target: an item, object, NPC, ground item or player. */
	private static final Set<MenuAction> TARGET_ACTIONS = EnumSet.of(
		MenuAction.WIDGET_TARGET_ON_WIDGET,
		MenuAction.WIDGET_TARGET_ON_GAME_OBJECT,
		MenuAction.WIDGET_TARGET_ON_NPC,
		MenuAction.WIDGET_TARGET_ON_GROUND_ITEM,
		MenuAction.WIDGET_TARGET_ON_PLAYER);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SpellCooldownConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CastTimer timer;

	@Inject
	private SpellIconOverlay overlay;

	@Inject
	private BankCloseGuard bankCloseGuard;

	@Inject
	private SpellAvailability availability;

	/** Magic XP at the last {@link StatChanged}, so only real XP gains count as an XP drop; -1 until known. */
	private int lastMagicXp = -1;

	/** Game tick of the last click that started a tracked cast. */
	private int clickTick;

	/** Wall-clock time of the last {@link GameTick}, so debug lines can say how far into a tick an event landed. */
	private long lastTickMs;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		timer.setFlashCount(config.flashCount());
		clientThread.invokeLater(this::readMagicXp);
		clientThread.invokeLater(this::readInventory);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		timer.cancel();
		bankCloseGuard.reset();
		availability.clear();
		lastMagicXp = -1;
	}

	/**
	 * Builds the one cast timer shared by the plugin and the overlay.
	 *
	 * @param config the plugin config, for the flash count
	 * @return the cast timer
	 */
	@Provides
	@Singleton
	CastTimer provideCastTimer(SpellCooldownConfig config)
	{
		return new CastTimer(config.flashCount());
	}

	@Provides
	SpellCooldownConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpellCooldownConfig.class);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		log.debug("{}: menu click {} {}", when(), event.getMenuOption(), Text.removeTags(event.getMenuTarget()));
		clickedSpell(event).ifPresent(this::startCast);
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Player player = client.getLocalPlayer();
		if (event.getActor() != player || player.getAnimation() == -1)
			return;

		log.debug("{}: local player animation {} (cast phase {})", when(), player.getAnimation(),
			timer.phase(System.currentTimeMillis()));
		timer.onCastConfirmed(System.currentTimeMillis());
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		lastTickMs = System.currentTimeMillis();
		timer.onTick(System.currentTimeMillis());
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.MAGIC)
			return;

		int xp = event.getXp();
		if (lastMagicXp >= 0 && xp > lastMagicXp)
		{
			log.debug("{}: Magic XP +{}", when(), xp - lastMagicXp);
			timer.onXp(System.currentTimeMillis());
		}

		lastMagicXp = xp;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE)
			log.debug("{}: game message {}", when(), event.getMessage());

		if (event.getType() != ChatMessageType.GAMEMESSAGE
			|| client.getTickCount() - clickTick > FAILURE_WINDOW_TICKS)
			return;

		String message = Text.removeTags(event.getMessage());
		if (FAILURE_MESSAGES.stream().anyMatch(message::startsWith))
			timer.cancel();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			timer.cancel();
			bankCloseGuard.reset();
			availability.clear();
			lastMagicXp = -1;
		}
		else if (state == GameState.LOGGED_IN)
		{
			readMagicXp();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INV)
			return;

		log.debug("{}: inventory changed", when());
		availability.update(itemIds(event.getItemContainer()));
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() != InterfaceID.BANKMAIN)
			return;

		log.debug("{}: bank closed", when());
		if (config.bankCloseWarning())
			bankCloseGuard.onBankClosed(System.currentTimeMillis());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() == VarbitID.SPELLBOOK)
			timer.cancel();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (SpellCooldownConfig.GROUP.equals(event.getGroup()))
			timer.setFlashCount(config.flashCount());
	}

	/**
	 * Works out which tracked spell a click casts. A one-click spell casts when its icon is clicked. A targeted spell
	 * (alchemy, enchant, Plank Make, ...) only arms on its icon click and casts on the click on its target, so it is
	 * read from the armed spell widget there, or from the spell name in the menu target if no widget is armed.
	 *
	 * @param event the menu click
	 * @return the spell this click casts, or empty
	 */
	private Optional<SpellDef> clickedSpell(MenuOptionClicked event)
	{
		if (!TARGET_ACTIONS.contains(event.getMenuAction()))
		{
			return SpellDef.byWidgetId(event.getParam1())
				.filter(spell -> !spell.isTargeted());
		}

		Widget selected = client.getSelectedWidget();
		Optional<SpellDef> spell = selected == null ? Optional.empty() : SpellDef.byWidgetId(selected.getId());
		if (spell.isEmpty())
			spell = SpellDef.byName(Text.removeTags(event.getMenuTarget()).split(" -> ")[0].trim());

		return spell.filter(SpellDef::isTargeted);
	}

	/**
	 * Starts tracking a cast, remembering its tick so a failure message right after it can cancel it.
	 *
	 * @param spell the spell being cast
	 */
	private void startCast(SpellDef spell)
	{
		long now = System.currentTimeMillis();
		CastPhase phase = timer.phase(now);
		log.debug("{}: cast click {} (cast phase {})", when(), spell.getName(), phase);
		if (phase == CastPhase.READY || phase == CastPhase.FLASH)
			clickTick = client.getTickCount();

		timer.onClick(spell, now);
	}

	/**
	 * Records the current Magic XP so the first {@link StatChanged} after login is not read as an XP drop. Right after
	 * login the client still reports 0 XP, so 0 is treated as unknown and the next {@link StatChanged} sets the
	 * baseline instead of counting as a gain.
	 */
	private void readMagicXp()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
			return;

		int xp = client.getSkillExperience(Skill.MAGIC);
		lastMagicXp = xp > 0 ? xp : -1;
	}

	/** Reads the inventory once, for a plugin started while already logged in. */
	private void readInventory()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory != null)
			availability.update(itemIds(inventory));
	}

	/**
	 * Lists the item IDs in a container, skipping empty slots.
	 *
	 * @param container the container
	 * @return the IDs of the items in it
	 */
	private static Set<Integer> itemIds(ItemContainer container)
	{
		Set<Integer> ids = new HashSet<>();
		for (Item item : container.getItems())
		{
			if (item.getId() > 0 && item.getQuantity() > 0)
				ids.add(item.getId());
		}

		return ids;
	}

	/**
	 * Describes the current moment for debug lines: the game tick and the milliseconds since that tick started.
	 *
	 * @return e.g. {@code Tick 287 +412ms}
	 */
	private String when()
	{
		return "Tick " + client.getTickCount() + " +" + (System.currentTimeMillis() - lastTickMs) + "ms";
	}
}
