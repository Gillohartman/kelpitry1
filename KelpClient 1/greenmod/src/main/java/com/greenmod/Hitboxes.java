package com.greenmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** Flat 2D hitbox rectangles. Uses the same interpolation as entity rendering to avoid jitter. */
public final class Hitboxes {
	private static final int[] RECT = new int[4];

	private Hitboxes() {}

	public static void render(GuiGraphicsExtractor g, LocalPlayer me, List<Entity> list) {
		boolean any = Modules.H_PLAYERS.module.isActive() || Modules.H_HOSTILE.module.isActive()
				|| Modules.H_PASSIVE.module.isActive() || Modules.H_ITEMS.module.isActive();
		if (!any) return;
		for (Entity e : list) box(g, me, e);
	}

	private static void box(GuiGraphicsExtractor g, LocalPlayer me, Entity e) {
		Modules.Hit hit = null;
		if (e instanceof ItemEntity) hit = Modules.H_ITEMS;
		else if (e instanceof Player p) {
			if (p.isSpectator()) return;
			hit = Modules.H_PLAYERS;
		} else if (e instanceof Enemy) hit = Modules.H_HOSTILE;
		else if (e instanceof LivingEntity) hit = Modules.H_PASSIVE;
		if (hit == null || !hit.module.isActive()) return;

		double range = hit.range.value;
		if (me.distanceToSqr(e) > range * range) return;
		if (hit.visibleOnly.value && !me.hasLineOfSight(e)) return;

		float pt = Projector.pt;
		double ox = Mth.lerp(pt, e.xOld, e.getX()) - e.getX();
		double oy = Mth.lerp(pt, e.yOld, e.getY()) - e.getY();
		double oz = Mth.lerp(pt, e.zOld, e.getZ()) - e.getZ();
		if (!Projector.rect(e.getBoundingBox(), ox, oy, oz, RECT)) return;

		outline(g, RECT, hit.thickness.i(), Ui.a(hit.color.rgb, hit.opacity.value / 100.0));
	}

	public static void outline(GuiGraphicsExtractor g, int[] r, int t, int c) {
		int x1 = r[0], y1 = r[1], x2 = r[2], y2 = r[3];
		g.fill(x1, y1, x2, y1 + t, c);
		g.fill(x1, y2 - t, x2, y2, c);
		g.fill(x1, y1 + t, x1 + t, y2 - t, c);
		g.fill(x2 - t, y1 + t, x2, y2 - t, c);
	}
}
