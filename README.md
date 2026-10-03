# Nico Chat

A [RuneLite](https://runelite.net) plugin that scrolls chat messages across your screen from right to left, like the comments on NicoNico livestreams.

<!-- Add a screenshot or GIF here once you have one, e.g.:
![Nico Chat in action](docs/screenshot.png)
-->

## Features

- Scrolls chat messages across the whole game screen, over the chatbox and game world.
- Choose which messages scroll: public chat, friends chat, clan chat, clan announcements, private messages, game messages, and your own messages.
- Comments are placed on random free rows so single messages don't all stay at the top of the screen.
- Optional overlap: a configurable chance for a comment to land on top of another one, for a busier danmaku look.
- Special characters such as `@`, accented letters and non-Latin text are drawn using a fallback font when the RuneScape font doesn't have them.
- Optional sender name on each comment.

Private messages and game messages are off by default, so they won't appear on stream by accident.

## Settings

**Sources**

| Setting | What it does |
| --- | --- |
| Public chat | Scroll public chat messages |
| Friends chat | Scroll friends chat messages |
| Clan chat | Scroll clan, guest clan and GIM chat messages |
| Clan announcements | Scroll clan announcements such as achievements and member logins |
| Private messages | Scroll private messages (off by default) |
| Game messages | Scroll game messages (off by default) |
| Your own messages | Also scroll messages you send |
| Show sender name | Prefix each comment with the sender's name |

**Appearance**

| Setting | What it does |
| --- | --- |
| Font size | Size of the comment text |
| Speed (px/sec) | How fast comments cross the screen |
| Opacity (%) | Comment opacity |
| Screen area (%) | How much of the screen height, from the top, comments may use |
| Random rows | Place comments on a random free row instead of the topmost one |
| Overlap chance (%) | Chance a comment can overlap another. 0 means never |
| Top offset (px) | Gap between the top of the screen and the first row |
| Text color | Comment text color |

## Notes

- Only chat the game client already shows you is displayed. The plugin sends no data anywhere and makes no network requests.
- Emoji depend on the fonts installed on your system and may not render.

## License

BSD 2-Clause. See `LICENSE`.