package com.greenmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** RESTRICTED: highlights entities through walls. Never runs while the module is locked. */
public final class EntityEsp {
	private static final int[] RECT = new int[4];

	private EntityEsp() {}

	public static void render(GuiGraphicsExtractor g, LocalPlayer me, List<Entity> list) {
		if (!Modules.ESP.isActive()) return;
		double range = Modules.ESP_RANGE.value;
		for (Entity e : list) {
			int rgb;
			if (e instanceof Player p) {
				if (!Modules.ESP_PLAYERS.value || p.isSpectator()) continue;
				rgb = Modules.ESP_C_PLAYERS.rgb;
			} else if (e instanceof Enemy) {
				if (!Modules.ESP_HOSTILE.value) continue;
				rgb = Modules.ESP_C_HOSTILE.rgb;
			} else if (e instanceof LivingEntity) {
				if (!Modules.ESP_PASSIVE.value) continue;
				rgb = Modules.ESP_C_PASSIVE.rgb;
			} else continue;

			if (me.distanceToSqr(e) > range * range) continue;
			float pt = Projector.pt;
			double ox = Mth.lerp(pt, e.xOld, e.getX()) - e.getX();
			double oy = Mth.lerp(pt, e.yOld, e.getY()) - e.getY();
			double oz = Mth.lerp(pt, e.zOld, e.getZ()) - e.getZ();
			if (!Projector.rect(e.getBoundingBox(), ox, oy, oz, RECT)) continue;

			int c = Ui.opaque(rgb);
			Hitboxes.outline(g, RECT, Modules.ESP_LINE.i(), c);
			if (Modules.ESP_TRACERS.value) {
				Ui.line(g, Projector.w / 2, Projector.h, (RECT[0] + RECT[2]) / 2, RECT[3], Ui.a(rgb, 0.8));
			}
		}
	}
}
