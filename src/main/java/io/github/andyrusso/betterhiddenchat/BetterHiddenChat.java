package io.github.andyrusso.betterhiddenchat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.net.URI;
import java.util.function.Supplier;

public class BetterHiddenChat implements ClientModInitializer {
	public static boolean isChatHidden = false;
	private static boolean wasPressed = false;

	@Override
	public void onInitializeClient() {
		BetterHiddenChatConfig config = BetterHiddenChatConfig.getInstance();
		if (config.persistency && config.chatHideState) isChatHidden = true;

		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> dispatcher.register(
						ClientCommandManager.literal("hidechat")
								.executes(
										context -> {
											toggleIsChatHidden();
											return 0;
										}
								)
								.then(
										configOption(
												"persistency",
												() -> config.persistency = !config.persistency
										)
								)
								.then(
										configOption(
												"notifyChatShow",
												() -> config.notifyChatShow = !config.notifyChatShow
										)
								)
								.then(
										configOption(
												"notifyAsOverlay",
												() -> config.notifyAsOverlay = !config.notifyAsOverlay
										)
								)
								.then(
										configOption(
												"showWhileTyping",
												() -> config.showWhileTyping = !config.showWhileTyping
										)
								)
								.then(help())
				)
		);

		KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(
				new KeyBinding(
						"betterhiddenchat.toggle",
						InputUtil.UNKNOWN_KEY.getCode(),
						KeyBinding.Category.create(Identifier.of("betterhiddenchat:category"))
				)
		);

		ClientTickEvents.END_CLIENT_TICK.register(
				client -> {
					boolean isPressed = keyBinding.isPressed();
					if (isPressed && !wasPressed) toggleIsChatHidden();
					wasPressed = isPressed;
				}
		);

		ClientLifecycleEvents.CLIENT_STOPPING.register(
				client -> {
					config.chatHideState = isChatHidden;
					config.save();
				}
		);
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> configOption(
			String key,
			Supplier<Boolean> toggleAndGetValue
	) {
		return ClientCommandManager.literal(key)
				.executes(
						context -> {
							context
									.getSource()
									.sendFeedback(
											Text.translatable(
													"betterhiddenchat." +
															key +
															"." +
															// The actual state has been inverted above,
															// so that's why if the state is `true`
															// this sends an "off" message etc.
															(toggleAndGetValue.get() ? "on" : "off")
											)
									);

							return 0;
						}
				);
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> help(
	) {
		return ClientCommandManager.literal("help")
				.executes(
						context -> {
							context
									.getSource()
									.sendFeedback(
											Text.literal(
													Formatting.GOLD + Formatting.BOLD.toString()
													+ I18n.translate("betterhiddenchat.help.1")
													+ Formatting.RESET
													+ I18n.translate("betterhiddenchat.help.2")
											)
											.append(Text.translatable("betterhiddenchat.help.3"))
											.append(Text.translatable("betterhiddenchat.help.4"))
											.append(Text.translatable("betterhiddenchat.help.5"))
											.append(Text.translatable("betterhiddenchat.help.6"))
											.append(Text.translatable("betterhiddenchat.help.7"))
											.append(Text.translatable("betterhiddenchat.help.8"))
											.append(Text.translatable("betterhiddenchat.help.9"))
											.append(
													translatableWithClickEvent(
															"betterhiddenchat.help.10",
															"https://modrinth.com/mod/betterhiddenchat"
													)
											)
											.append(
													translatableWithClickEvent(
															"betterhiddenchat.help.11",
															"https://boosty.to/AndyRusso"
													)
											)
											.append(
													translatableWithClickEvent(
															"betterhiddenchat.help.12",
															"https://modrinth.com/user/AndyRusso"
													)
											)
									);

							return 0;
						}
				);
	}

	private MutableText translatableWithClickEvent(String translationKey, String url) {
		return Text.translatable(translationKey).fillStyle(
				Style.EMPTY.withClickEvent(
						new ClickEvent.OpenUrl(URI.create(
								url)
						)
				)
		);
	}

	public void toggleIsChatHidden() {
		isChatHidden = !isChatHidden;

		MinecraftClient client = MinecraftClient.getInstance();
		BetterHiddenChatConfig config = BetterHiddenChatConfig.getInstance();
		if (config.notifyChatShow && !isChatHidden && client.player != null) {
			Text text = Text.translatable("betterhiddenchat.shown");
			client.player.sendMessage(text, config.notifyAsOverlay);
		}
	}
}