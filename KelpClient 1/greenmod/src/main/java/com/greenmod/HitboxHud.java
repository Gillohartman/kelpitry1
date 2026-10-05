package com.greenmod;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;

/** The main overlay: draws every world-space and HUD feature once per frame. */
public final class HitboxHud {
	private HitboxHud() {}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer me = mc.player;
		ClientLevel level = mc.level;
		if (me == null || level == null) return;

		ZoomFeature.update();

		boolean needWorld = Modules.H_PLAYERS.module.isActive() || Modules.H_HOSTILE.module.isActive()
				|| Modules.H_PASSIVE.module.isActive() || Modules.H_ITEMS.module.isActive()
				|| Modules.PLAYER_TAGS.isActive() || Modules.MOB_TAGS.isActive() || Modules.ITEM_TAGS.isActive()
				|| Modules.ESP.isActive();

		if (needWorld && Projector.begin(delta)) {
			List<Entity> list = level.getEntities(me, me.getBoundingBox().inflate(128.0), e -> true);
			Hitboxes.render(g, me, list);
			EntityEsp.render(g, me, list);
			Nametags.render(g, mc, me, list);
		}

		TotemFeatures.renderHotbarGlow(g, mc, me);
		LowFire.render(g, mc);
		ArmorHud.render(g, mc, me);
		TotemFeatures.renderCounter(g, mc, me);
		InfoHud.render(g, mc, me);
		OnlineHub.render(g, mc);
		Notifications.render(g, mc);
	}

	public static void crosshair(GuiGraphicsExtractor g) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.options.getCameraType().isFirstPerson()) return;
		int cx = mc.getWindow().getGuiScaledWidth() / 2;
		int cy = mc.getWindow().getGuiScaledHeight() / 2;
		int c = Ui.opaque(Modules.CROSS_COLOR.rgb);
		int gap = Modules.CROSS_GAP.i();
		int len = Modules.CROSS_LEN.i();
		int t = Modules.CROSS_THICK.i();
		int lo = t / 2;
		g.fill(cx - gap - len, cy - lo, cx - gap, cy - lo + t, c);
		g.fill(cx + gap + 1, cy - lo, cx + gap + 1 + len, cy - lo + t, c);
		g.fill(cx - lo, cy - gap - len, cx - lo + t, cy - gap, c);
		g.fill(cx - lo, cy + gap + 1, cx - lo + t, cy + gap + 1 + len, c);
		if (Modules.CROSS_DOT.value) g.fill(cx - lo, cy - lo, cx - lo + t, cy - lo + t, c);
	}
}
