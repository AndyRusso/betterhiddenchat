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
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

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
				)
		);

		KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(
				new KeyBinding(
						"betterhiddenchat.toggle",
						InputUtil.UNKNOWN_KEY.getCode(),
						"betterhiddenchat.category"
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