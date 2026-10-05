package com.greenmod.mixin;

import com.greenmod.ZoomFeature;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

	/** While zooming, the scroll wheel changes the zoom instead of the hotbar. */
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true, require = 0)
	private void kelp$scroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
		if (ZoomFeature.onScroll(yOffset)) ci.cancel();
	}
}
