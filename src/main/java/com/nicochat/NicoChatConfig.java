package com.nicochat;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("nicochat")
public interface NicoChatConfig extends Config
{
	@ConfigSection(name = "Sources", description = "Which messages get scrolled", position = 0)
	String sourcesSection = "sources";

	@ConfigSection(name = "Appearance", description = "How the comments look and move", position = 1)
	String appearanceSection = "appearance";

	@ConfigItem(keyName = "showPublic", name = "Public chat", description = "Scroll public chat messages", section = sourcesSection, position = 0)
	default boolean showPublic()
	{
		return true;
	}

	@ConfigItem(keyName = "showFriendsChat", name = "Friends chat", description = "Scroll friends chat messages", section = sourcesSection, position = 1)
	default boolean showFriendsChat()
	{
		return true;
	}

	@ConfigItem(keyName = "showClan", name = "Clan chat", description = "Scroll clan, guest clan and GIM chat messages", section = sourcesSection, position = 2)
	default boolean showClan()
	{
		return true;
	}

	@ConfigItem(keyName = "showClanAnnouncements", name = "Clan announcements", description = "Scroll clan announcements such as achievements, level-ups and member logins", section = sourcesSection, position = 3)
	default boolean showClanAnnouncements()
	{
		return true;
	}

	@ConfigItem(keyName = "showPrivate", name = "Private messages", description = "Scroll private messages (off by default - they'd be visible to anyone watching your screen)", section = sourcesSection, position = 4)
	default boolean showPrivate()
	{
		return false;
	}

	@ConfigItem(keyName = "showGame", name = "Game messages", description = "Scroll game messages such as 'You catch a shark.'", section = sourcesSection, position = 5)
	default boolean showGame()
	{
		return false;
	}

	@ConfigItem(keyName = "showOwn", name = "Your own messages", description = "Also scroll messages you send", section = sourcesSection, position = 6)
	default boolean showOwn()
	{
		return true;
	}

	@ConfigItem(keyName = "showSender", name = "Show sender name", description = "Prefix each comment with the sender's name", section = sourcesSection, position = 7)
	default boolean showSender()
	{
		return false;
	}

	@Range(min = 10, max = 64)
	@ConfigItem(keyName = "fontSize", name = "Font size", description = "Comment text size", section = appearanceSection, position = 0)
	default int fontSize()
	{
		return 24;
	}

	@Range(min = 50, max = 1000)
	@ConfigItem(keyName = "speed", name = "Speed (px/sec)", description = "How fast comments travel across the screen", section = appearanceSection, position = 1)
	default int speed()
	{
		return 220;
	}

	@Range(min = 10, max = 100)
	@ConfigItem(keyName = "opacity", name = "Opacity (%)", description = "Comment opacity", section = appearanceSection, position = 2)
	default int opacity()
	{
		return 90;
	}

	@Range(min = 10, max = 100)
	@ConfigItem(keyName = "areaHeight", name = "Screen area (%)", description = "How much of the screen height (from the top) comments may use", section = appearanceSection, position = 3)
	default int areaHeight()
	{
		return 60;
	}

	@ConfigItem(keyName = "randomLanes", name = "Random rows", description = "Place each comment on a random free row instead of the topmost free one", section = appearanceSection, position = 4)
	default boolean randomLanes()
	{
		return true;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(keyName = "overlapChance", name = "Overlap chance (%)", description = "Chance that a comment ignores free rows and can overlap another comment. 0 = never overlap", section = appearanceSection, position = 5)
	default int overlapChance()
	{
		return 15;
	}

	@Range(min = 0, max = 500)
	@ConfigItem(keyName = "verticalOffset", name = "Top offset (px)", description = "Gap between the top of the screen and the first row", section = appearanceSection, position = 6)
	default int verticalOffset()
	{
		return 30;
	}

	@ConfigItem(keyName = "textColor", name = "Text color", description = "Comment text color", section = appearanceSection, position = 7)
	default Color textColor()
	{
		return Color.WHITE;
	}
}
