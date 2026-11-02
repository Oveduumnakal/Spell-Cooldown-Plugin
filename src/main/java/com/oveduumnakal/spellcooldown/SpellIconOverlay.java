/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.spellcooldown;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws the cast state on the tracked spell's spellbook icon: a full cover after the click, a cover that wipes away
 * from the top down with a smooth countdown while the spell casts, and a coloured flash when it finishes. The cover
 * and flash are shaped like the icon itself, not the clickable square around it. Every other tracked spell icon gets
 * the same grey cover right after the bank closes, and whenever the inventory has nothing for that spell to work on.
 */
class SpellIconOverlay extends Overlay
{
	private final Client client;
	private final CastTimer timer;
	private final SpellCooldownConfig config;
	private final SpriteManager spriteManager;
	private final BankCloseGuard bankCloseGuard;
	private final SpellAvailability availability;
	private final TintedIcon cover = new TintedIcon();
	private final TintedIcon flash = new TintedIcon();
	private final Map<Integer, TintedIcon> warnings = new HashMap<>();

	/**
	 * Creates the overlay above the widgets, positioned on the spell icon each frame.
	 *
	 * @param client the game client, for the spell icon widget
	 * @param timer the cast state machine
	 * @param config the plugin settings
	 * @param spriteManager the sprite cache, for the icon's shape
	 * @param bankCloseGuard the unsafe window after the bank closes
	 * @param availability which spells have something in the inventory to work on
	 */
	@Inject
	SpellIconOverlay(Client client, CastTimer timer, SpellCooldownConfig config, SpriteManager spriteManager,
		BankCloseGuard bankCloseGuard, SpellAvailability availability)
	{
		this.client = client;
		this.timer = timer;
		this.config = config;
		this.spriteManager = spriteManager;
		this.bankCloseGuard = bankCloseGuard;
		this.availability = availability;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	/**
	 * Greys out spells that cannot be cast right now, then draws the cover, countdown or flash for the current phase
	 * over the cast spell's icon.
	 *
	 * @param graphics the overlay graphics
	 * @return always null; the overlay draws in place on the widgets
	 */
	@Override
	public Dimension render(Graphics2D graphics)
	{
		long now = System.currentTimeMillis();
		CastPhase phase = timer.phase(now);
		Optional<SpellDef> active = phase == CastPhase.READY ? Optional.empty() : timer.spell();
		boolean bankClose = bankCloseGuard.isActive(now) && config.bankCloseWarning();
		boolean inputs = config.greyWithoutInputs();
		if (bankClose || inputs)
			drawBlockedIcons(graphics, active, bankClose, inputs);

		active.ifPresent(spell -> drawCast(graphics, spell, phase, now));
		return null;
	}

	/**
	 * Greys out every visible tracked spell icon that cannot be cast right now, except the one being cast. An icon
	 * several spells share (the Jewellery Enchantments button) is greyed only when none of them can be cast.
	 *
	 * @param graphics the overlay graphics
	 * @param active the spell being cast, whose icon shows its own state instead
	 * @param bankClose true while casting right after the bank closed is unsafe
	 * @param inputs true to grey out spells with nothing in the inventory to work on
	 */
	private void drawBlockedIcons(Graphics2D graphics, Optional<SpellDef> active, boolean bankClose, boolean inputs)
	{
		Map<Integer, Widget> icons = new HashMap<>();
		Map<Integer, Boolean> blocked = new HashMap<>();
		Set<Integer> busy = new HashSet<>();
		for (SpellDef spell : SpellDef.ALL)
		{
			Widget widget = visibleIcon(spell);
			if (widget == null)
				continue;

			if (active.equals(Optional.of(spell)))
				busy.add(widget.getId());

			boolean spellBlocked = bankClose || inputs && !availability.canCast(spell);
			icons.put(widget.getId(), widget);
			blocked.merge(widget.getId(), spellBlocked, Boolean::logicalAnd);
		}

		for (Map.Entry<Integer, Boolean> entry : blocked.entrySet())
		{
			if (entry.getValue() && !busy.contains(entry.getKey()))
				drawGreyIcon(graphics, icons.get(entry.getKey()));
		}
	}

	/**
	 * Covers one icon with the grey cover.
	 *
	 * @param graphics the overlay graphics
	 * @param widget the icon to cover
	 */
	private void drawGreyIcon(Graphics2D graphics, Widget widget)
	{
		Rectangle bounds = widget.getBounds();
		int spriteId = widget.getSpriteId();
		BufferedImage sprite = spriteManager.getSprite(spriteId, 0);
		TintedIcon tint = warnings.computeIfAbsent(widget.getId(), id -> new TintedIcon());
		drawBottom(graphics, bounds, tint.image(sprite, spriteId, bounds, config.coverColor()), config.coverColor(),
			1.0);
	}

	/**
	 * Draws the cast state on the cast spell's icon.
	 *
	 * @param graphics the overlay graphics
	 * @param spell the spell being cast
	 * @param phase the cast phase, never {@link CastPhase#READY}
	 * @param now the current time in ms
	 */
	private void drawCast(Graphics2D graphics, SpellDef spell, CastPhase phase, long now)
	{
		Widget widget = visibleIcon(spell);
		if (widget == null)
			return;

		Rectangle bounds = widget.getBounds();
		int spriteId = widget.getSpriteId();
		BufferedImage sprite = spriteManager.getSprite(spriteId, 0);
		if (phase == CastPhase.CASTING && timer.isTimed())
		{
			drawCountdown(graphics, bounds, cover.image(sprite, spriteId, bounds, config.coverColor()), now);
		}
		else if (phase == CastPhase.PENDING || phase == CastPhase.CASTING)
		{
			drawBottom(graphics, bounds, cover.image(sprite, spriteId, bounds, config.coverColor()),
				config.coverColor(), 1.0);
		}
		else if (phase == CastPhase.FLASH && timer.flashOn(now))
		{
			drawBottom(graphics, bounds, flash.image(sprite, spriteId, bounds, config.flashColor()),
				config.flashColor(), 1.0);
		}
	}

	/**
	 * Finds the icon to draw on: the spell's own icon, or its parent button (e.g. the Jewellery Enchantments button)
	 * while the spell sits in a collapsed submenu.
	 *
	 * @param spell the tracked spell
	 * @return the visible icon widget, or null when neither is shown
	 */
	private Widget visibleIcon(SpellDef spell)
	{
		Widget widget = client.getWidget(spell.getWidgetId());
		if (widget != null && !widget.isHidden())
			return widget;

		if (spell.getParentWidgetId() < 0)
			return null;

		Widget parent = client.getWidget(spell.getParentWidgetId());
		return parent == null || parent.isHidden() ? null : parent;
	}

	/**
	 * Draws the shrinking cover over the bottom share of the icon still to run, and the seconds left on top.
	 *
	 * @param graphics the overlay graphics
	 * @param bounds the spell icon bounds
	 * @param tinted the icon-shaped cover, or null to fall back to a rectangle
	 * @param now the current time in ms
	 */
	private void drawCountdown(Graphics2D graphics, Rectangle bounds, BufferedImage tinted, long now)
	{
		double left = Math.max(0.0, Math.min(1.0, 1.0 - timer.progress(now)));
		drawBottom(graphics, bounds, tinted, config.coverColor(), left);
		if (config.showNumber())
			drawOutlinedText(graphics, bounds, secondsText(timer.remainingMs(now)));
	}

	/**
	 * Draws the bottom share of a tinted icon over the bounds. With no tinted icon (sprite not loaded yet), it fills
	 * the same share of the bounds as a plain rectangle instead.
	 *
	 * @param graphics the overlay graphics
	 * @param bounds the spell icon bounds
	 * @param tinted the icon-shaped tint, or null to fall back to a rectangle
	 * @param fallback the rectangle colour when there is no tinted icon
	 * @param share how much of the height to cover, from the bottom, 0 to 1
	 */
	private static void drawBottom(Graphics2D graphics, Rectangle bounds, BufferedImage tinted, Color fallback,
		double share)
	{
		int height = (int) Math.round(bounds.height * share);
		if (height <= 0)
			return;

		int top = bounds.y + bounds.height - height;
		if (tinted == null)
		{
			graphics.setColor(fallback);
			graphics.fillRect(bounds.x, top, bounds.width, height);
			return;
		}

		graphics.drawImage(tinted, bounds.x, top, bounds.x + bounds.width, bounds.y + bounds.height,
			0, bounds.height - height, bounds.width, bounds.height, null);
	}

	/**
	 * Formats the time left as seconds to one decimal place, e.g. {@code 4.8}.
	 *
	 * @param remainingMs the ms left in the cast
	 * @return the text to draw
	 */
	private static String secondsText(long remainingMs)
	{
		return String.format(Locale.ROOT, "%.1f", Math.max(0L, remainingMs) / 1000.0);
	}

	/**
	 * Draws white text with a 1 px black outline on all sides, centred in the bounds, so it reads on any icon.
	 *
	 * @param graphics the overlay graphics
	 * @param bounds the area to centre in
	 * @param text the text to draw
	 */
	private static void drawOutlinedText(Graphics2D graphics, Rectangle bounds, String text)
	{
		graphics.setFont(FontManager.getRunescapeBoldFont());
		FontMetrics metrics = graphics.getFontMetrics();
		int x = (int) bounds.getCenterX() - metrics.stringWidth(text) / 2;
		int y = (int) bounds.getCenterY() + (metrics.getAscent() - metrics.getDescent()) / 2;
		graphics.setColor(Color.BLACK);
		for (int dx = -1; dx <= 1; dx++)
		{
			for (int dy = -1; dy <= 1; dy++)
			{
				if (dx != 0 || dy != 0)
					graphics.drawString(text, x + dx, y + dy);
			}
		}

		graphics.setColor(Color.WHITE);
		graphics.drawString(text, x, y);
	}

	/**
	 * One icon-shaped tint: the sprite scaled to the widget, with every visible pixel recoloured. Rebuilt only when
	 * the sprite, size or colour changes, so a frame normally allocates nothing.
	 */
	private static final class TintedIcon
	{
		private BufferedImage image;
		private int spriteId = -1;
		private int width;
		private int height;
		private int argb;

		/**
		 * Gives the tinted icon, building it when the inputs changed.
		 *
		 * @param sprite the icon's sprite, or null while it is not loaded
		 * @param spriteId the sprite's ID, used to notice a changed sprite
		 * @param bounds the widget bounds the sprite is drawn into
		 * @param color the tint, alpha included
		 * @return the tinted icon, or null when the sprite is not loaded
		 */
		BufferedImage image(BufferedImage sprite, int spriteId, Rectangle bounds, Color color)
		{
			if (sprite == null || bounds.width <= 0 || bounds.height <= 0)
				return null;

			if (image != null && spriteId == this.spriteId && bounds.width == width && bounds.height == height
				&& color.getRGB() == argb)
				return image;

			BufferedImage tinted = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = tinted.createGraphics();
			g.drawImage(sprite, 0, 0, bounds.width, bounds.height, null);
			g.setComposite(AlphaComposite.SrcIn);
			g.setColor(color);
			g.fillRect(0, 0, bounds.width, bounds.height);
			g.dispose();
			image = tinted;
			this.spriteId = spriteId;
			width = bounds.width;
			height = bounds.height;
			argb = color.getRGB();
			return image;
		}
	}
}
