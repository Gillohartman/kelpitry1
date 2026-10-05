package com.greenmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/** Flight and Elytra Boost. Both are RESTRICTED; isActive() is false while locked. */
public final class MovementFeatures {
	private MovementFeatures() {}

	public static void tick(Minecraft mc) {
		LocalPlayer p = mc.player;
		if (p == null) return;

		if (Modules.FLIGHT.isActive() && !p.isFallFlying() && mc.screen == null) {
			double yaw = Math.toRadians(p.getYRot());
			double fwd = 0, side = 0, up = 0;
			if (mc.options.keyUp.isDown()) fwd += 1;
			if (mc.options.keyDown.isDown()) fwd -= 1;
			if (mc.options.keyLeft.isDown()) side += 1;
			if (mc.options.keyRight.isDown()) side -= 1;
			if (mc.options.keyJump.isDown()) up += 1;
			if (mc.options.keyShift.isDown()) up -= 1;

			double sp = Modules.FLIGHT_SPEED.value;
			double vx = (-Math.sin(yaw) * fwd + Math.cos(yaw) * side) * sp;
			double vz = (Math.cos(yaw) * fwd + Math.sin(yaw) * side) * sp;
			p.setDeltaMovement(vx, up * Modules.FLIGHT_VSPEED.value, vz);
			p.resetFallDistance();
		}

		if (Modules.ELYTRA_BOOST.isActive() && p.isFallFlying() && mc.screen == null && Keys.down(Modules.EB_KEY.key)) {
			Vec3 look = p.getLookAngle();
			Vec3 v = p.getDeltaMovement().add(look.scale(Modules.EB_POWER.value));
			double max = Modules.EB_MAX.value;
			if (v.length() > max) v = v.scale(max / v.length());
			p.setDeltaMovement(v);
		}
	}
}
