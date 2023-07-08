# Better hiden chat
Hides the chat, whilst allowing to send messages and run commands.

Hide chat with a keybinding, or via command: `/hidechat`

Configuration is done via `/hidechat` subcommands:
- `persistency` - keep the chat hidden the next time you open the game,
if you had it hidden when you closed it. Off by default.
- `notifyChatShow` - toggles whether to send a notification when the chat is shown. On by default.
- `notifyAsOverlay` - toggles the notification between a chat message and an overlay message. Chat message by default.
- `showWhileTyping` - toggles whether to show the chat when typing if the chat is hidden. Off by default.

Requires [Fabric API](https://modrinth.com/mod/fabric-api)
