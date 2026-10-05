package com.greenmod.mixin;

import com.greenmod.TotemPopState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Inject(method = {"renderItemActivationAnimation", "extractItemActivationAnimation"}, at = @At("HEAD"), cancellable = true, require = 0)
	private void kelp$popBefore(GuiGraphicsExtractor g, float partialTick, CallbackInfo ci) {
		if (TotemPopState.before(g)) ci.cancel();
	}

	@Inject(method = {"renderItemActivationAnimation", "extractItemActivationAnimation"}, at = @At("RETURN"), require = 0)
	private void kelp$popAfter(GuiGraphicsExtractor g, float partialTick, CallbackInfo ci) {
		TotemPopState.after(g);
	}
}
