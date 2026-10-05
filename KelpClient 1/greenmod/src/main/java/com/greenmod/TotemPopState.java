package com.greenmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Shared by the totem pop mixins: shrink (or hide) the animation around the screen center. */
public final class TotemPopState {
	private static boolean pushed;

	private TotemPopState() {}

	/** Returns true if the animation should be cancelled. */
	public static boolean before(GuiGraphicsExtractor g) {
		if (!Modules.TOTEM_POP.isActive()) return false;
		if (Modules.TP_HIDE.value) return true;
		Minecraft mc = Minecraft.getInstance();
		float cx = mc.getWindow().getGuiScaledWidth() / 2f;
		float cy = mc.getWindow().getGuiScaledHeight() / 2f;
		float s = Modules.TP_SCALE.f();
		var p = g.pose();
		p.pushMatrix();
		p.translate(cx, cy);
		p.scale(s, s);
		p.translate(-cx, -cy);
		pushed = true;
		return false;
	}

	public static void after(GuiGraphicsExtractor g) {
		if (pushed) {
			g.pose().popMatrix();
			pushed = false;
		}
	}
}
