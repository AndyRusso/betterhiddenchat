package io.github.andyrusso.betterhiddenchat.mixin;

import io.github.andyrusso.betterhiddenchat.BetterHiddenChat;
import io.github.andyrusso.betterhiddenchat.BetterHiddenChatConfig;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatHud.class)
public class MixinChatHud {
    @Inject(method = "isChatHidden", at = @At("HEAD"), cancellable = true)
    private void hideChat(CallbackInfoReturnable<Boolean> cir) {
        boolean showWhileTyping = !BetterHiddenChatConfig.getInstance().showWhileTyping;
        boolean isTyping = showWhileTyping || !((ChatHud) (Object) this).isChatFocused();

        cir.setReturnValue(BetterHiddenChat.isChatHidden && isTyping);
    }
}
