package com.nicochat;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Nico Chat",
	description = "Scrolls chat messages across the screen like NicoNico comments",
	tags = {"chat", "niconico", "danmaku", "scroll", "overlay"}
)
public class NicoChatPlugin extends Plugin
{
	private static final int MAX_LENGTH = 150;

	@Inject
	private Client client;

	@Inject
	private NicoChatConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NicoChatOverlay overlay;

	@Provides
	NicoChatConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NicoChatConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlay.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			overlay.clear();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("nicochat".equals(event.getGroup()))
		{
			// font size changes would make cached widths wrong
			overlay.clear();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		final ChatMessageType type = event.getType();
		if (!isEnabled(type))
		{
			return;
		}

		String message = Text.removeTags(event.getMessage())
			.replace('\u00A0', ' ')
			.replaceAll("\\p{Cntrl}", " ")
			.trim();
		if (message.isEmpty())
		{
			return;
		}

		final String name = event.getName() == null ? "" : Text.toJagexName(Text.removeTags(event.getName()));

		if (!config.showOwn() && isOwn(type, name))
		{
			return;
		}

		if (message.length() > MAX_LENGTH)
		{
			message = message.substring(0, MAX_LENGTH - 1) + "\u2026";
		}

		if (config.showSender() && !name.isEmpty() && !isAnnouncement(type))
		{
			message = name + ": " + message;
		}

		overlay.addMessage(message);
	}

	private boolean isAnnouncement(ChatMessageType type)
	{
		return type == ChatMessageType.CLAN_MESSAGE
			|| type == ChatMessageType.CLAN_GUEST_MESSAGE
			|| type == ChatMessageType.CLAN_GIM_MESSAGE
			|| type == ChatMessageType.GAMEMESSAGE;
	}

	private boolean isOwn(ChatMessageType type, String name)
	{
		if (type == ChatMessageType.PRIVATECHATOUT)
		{
			return true;
		}
		Player local = client.getLocalPlayer();
		return local != null && local.getName() != null
			&& name.equalsIgnoreCase(Text.toJagexName(local.getName()));
	}

	private boolean isEnabled(ChatMessageType type)
	{
		switch (type)
		{
			case PUBLICCHAT:
			case MODCHAT:
			case AUTOTYPER:
			case MODAUTOTYPER:
				return config.showPublic();
			case FRIENDSCHAT:
				return config.showFriendsChat();
			case CLAN_CHAT:
			case CLAN_GUEST_CHAT:
			case CLAN_GIM_CHAT:
				return config.showClan();
			case CLAN_MESSAGE:
			case CLAN_GUEST_MESSAGE:
			case CLAN_GIM_MESSAGE:
				return config.showClanAnnouncements();
			case PRIVATECHAT:
			case MODPRIVATECHAT:
			case PRIVATECHATOUT:
				return config.showPrivate();
			case GAMEMESSAGE:
				return config.showGame();
			default:
				return false;
		}
	}
}
