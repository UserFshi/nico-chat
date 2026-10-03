package com.nicochat;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws comments sliding right-to-left across the whole game canvas.
 * Comments are assigned to horizontal "lanes" (rows); a new comment takes the
 * first lane whose previous comment has fully entered the screen, so
 * comments never overlap. If every lane is busy the comment waits briefly,
 * then is dropped, just like on NicoNico.
 */
@Singleton
public class NicoChatOverlay extends Overlay
{
	private static final int MAX_PENDING = 40;
	private static final int MAX_ACTIVE = 150;
	private static final long MAX_WAIT_NANOS = 4_000_000_000L;

	private static final class Comment
	{
		final TextLayout layout;
		final int width;
		final int lane;
		double x;

		Comment(TextLayout layout, double x, int width, int lane)
		{
			this.layout = layout;
			this.x = x;
			this.width = width;
			this.lane = lane;
		}
	}

	private static final class Pending
	{
		final String text;
		final long queuedAt = System.nanoTime();
		final boolean overlap;

		Pending(String text, boolean overlap)
		{
			this.text = text;
			this.overlap = overlap;
		}
	}

	private final Client client;
	private final NicoChatConfig config;

	private final List<Comment> active = new ArrayList<>();
	private final Deque<Pending> pending = new ArrayDeque<>();
	private long lastNanos;

	private int cachedFontSize = -1;
	private Font baseFont;
	private Font fallbackFont;

	@Inject
	private NicoChatOverlay(Client client, NicoChatConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	void addMessage(String text)
	{
		final int chance = config.overlapChance();
		final boolean overlap = chance > 0 && ThreadLocalRandom.current().nextInt(100) < chance;
		pending.addLast(new Pending(text, overlap));
		while (pending.size() > MAX_PENDING)
		{
			pending.removeFirst();
		}
	}

	void clear()
	{
		active.clear();
		pending.clear();
		lastNanos = 0;
	}

	/**
	 * Lays the text out character by character: the RuneScape font where it has
	 * the glyph, a general system font where it does not.
	 */
	private TextLayout buildLayout(String text, FontRenderContext frc)
	{
		final AttributedString as = new AttributedString(text);
		int i = 0;
		while (i < text.length())
		{
			final int cp = text.codePointAt(i);
			final int len = Character.charCount(cp);
			as.addAttribute(TextAttribute.FONT, baseFont.canDisplay(cp) ? baseFont : fallbackFont, i, i + len);
			i += len;
		}
		return new TextLayout(as.getIterator(), frc);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		final long now = System.nanoTime();
		final double dt = lastNanos == 0 ? 0 : Math.min((now - lastNanos) / 1e9, 0.25);
		lastNanos = now;

		if (active.isEmpty() && pending.isEmpty())
		{
			return null;
		}

		final int canvasW = client.getCanvasWidth();
		final int canvasH = client.getCanvasHeight();

		if (cachedFontSize != config.fontSize())
		{
			cachedFontSize = config.fontSize();
			baseFont = FontManager.getRunescapeBoldFont().deriveFont((float) cachedFontSize);
			// Logical "Dialog" font falls back to system fonts for CJK, symbols, etc.
			fallbackFont = new Font(Font.DIALOG, Font.BOLD, 1).deriveFont((float) cachedFontSize);
		}
		g.setFont(baseFont);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		final FontMetrics fm = g.getFontMetrics();
		final FontRenderContext frc = g.getFontRenderContext();

		final int laneHeight = fm.getHeight() + 2;
		final int offsetY = config.verticalOffset();
		final int usable = (int) (canvasH * (config.areaHeight() / 100.0)) - offsetY;
		final int lanes = Math.max(1, usable / laneHeight);
		final int gap = config.fontSize();

		// Move + cull
		final double dx = config.speed() * dt;
		for (Iterator<Comment> it = active.iterator(); it.hasNext(); )
		{
			Comment c = it.next();
			c.x -= dx;
			if (c.x + c.width < 0)
			{
				it.remove();
			}
		}

		// Right edge of the last comment in each lane (0 = lane is empty)
		final double[] tails = new double[lanes];
		for (Comment c : active)
		{
			if (c.lane < lanes)
			{
				tails[c.lane] = Math.max(tails[c.lane], c.x + c.width);
			}
		}

		// Admit waiting comments into free lanes, oldest first
		for (Iterator<Pending> it = pending.iterator(); it.hasNext(); )
		{
			Pending p = it.next();
			if (now - p.queuedAt > MAX_WAIT_NANOS)
			{
				it.remove();
				continue;
			}
			if (active.size() >= MAX_ACTIVE)
			{
				break;
			}

			int lane = -1;
			if (p.overlap)
			{
				// Ignore occupancy: any row, even a busy one
				lane = ThreadLocalRandom.current().nextInt(lanes);
			}
			else
			{
				int freeCount = 0;
				for (int l = 0; l < lanes; l++)
				{
					if (tails[l] + gap <= canvasW)
					{
						freeCount++;
					}
				}
				if (freeCount > 0)
				{
					int pick = config.randomLanes() ? ThreadLocalRandom.current().nextInt(freeCount) : 0;
					for (int l = 0; l < lanes; l++)
					{
						if (tails[l] + gap <= canvasW && pick-- == 0)
						{
							lane = l;
							break;
						}
					}
				}
			}
			if (lane < 0)
			{
				break; // all lanes busy; try again next frame
			}

			TextLayout layout = buildLayout(p.text, frc);
			int width = (int) Math.ceil(layout.getAdvance());
			active.add(new Comment(layout, canvasW, width, lane));
			tails[lane] = Math.max(tails[lane], canvasW + width);
			it.remove();
		}

		// Draw
		final Composite oldComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.opacity() / 100f));
		final Color main = config.textColor();

		for (Comment c : active)
		{
			final int x = (int) c.x;
			final int y = offsetY + c.lane * laneHeight + fm.getAscent();

			g.setColor(Color.BLACK);
			c.layout.draw(g, x + 1, y + 1);
			c.layout.draw(g, x - 1, y + 1);
			c.layout.draw(g, x + 1, y - 1);
			c.layout.draw(g, x - 1, y - 1);

			g.setColor(main);
			c.layout.draw(g, x, y);
		}

		g.setComposite(oldComposite);
		return null;
	}
}
