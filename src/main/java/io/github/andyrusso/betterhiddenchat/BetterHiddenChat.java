package io.github.andyrusso.betterhiddenchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class BetterHiddenChat implements ClientModInitializer {
	public static boolean isChatHidden = false;

	@Override
	public void onInitializeClient() {
		BetterHiddenChatConfig config = BetterHiddenChatConfig.getInstance();
		if (config.persist && config.chatHideState) isChatHidden = true;

		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> dispatcher.register(
						ClientCommandManager.literal("hidechat")
								.executes(
										context -> {
											isChatHidden = !isChatHidden;
											return 0;
										}
								)
								.then(
										ClientCommandManager.literal("togglePersistency")
												.executes(
														context -> {
															config.persist = !config.persist;
															context
																	.getSource()
																	.sendFeedback(
																			Text.translatable(
																					config.persist ?
																							"betterhiddenchat.on"
																							:
																							"betterhiddenchat.off"
																			)
																	);
															return 0;
														}
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
					while (keyBinding.wasPressed()) isChatHidden = !isChatHidden;
				}
		);

		ClientLifecycleEvents.CLIENT_STOPPING.register(
				client -> {
					config.chatHideState = isChatHidden;
					config.save();
				}
		);
	}
}