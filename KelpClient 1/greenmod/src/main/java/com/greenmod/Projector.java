package com.greenmod;

import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Projects world positions onto the GUI-scaled screen. Works in first and third person. */
public final class Projector {
	private static double ex, ey, ez, fx, fy, fz, rx, ry, rz, ux, uy, uz, tanHalf, aspect;
	public static int w, h;
	public static float pt;
	public static boolean thirdPerson;
	public static double sx, sy, depth;

	private Projector() {}

	public static boolean begin(DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer me = mc.player;
		if (me == null) return false;

		CameraType ct = mc.options.getCameraType();
		thirdPerson = !ct.isFirstPerson();
		pt = delta.getGameTimeDeltaPartialTick(false);
		Vec3 eye = me.getEyePosition(pt);

		double yawDeg = me.getViewYRot(pt);
		double pitchDeg = me.getViewXRot(pt);
		if (thirdPerson && ct.isMirrored()) {
			yawDeg += 180.0;
			pitchDeg = -pitchDeg;
		}
		double yaw = Math.toRadians(yawDeg);
		double pitch = Math.toRadians(pitchDeg);

		fx = -Math.sin(yaw) * Math.cos(pitch);
		fy = -Math.sin(pitch);
		fz = Math.cos(yaw) * Math.cos(pitch);
		rx = -Math.cos(yaw); ry = 0; rz = -Math.sin(yaw);
		ux = ry * fz - rz * fy;
		uy = rz * fx - rx * fz;
		uz = rx * fy - ry * fx;

		if (thirdPerson) {
			ex = eye.x - fx * 4.0; ey = eye.y - fy * 4.0; ez = eye.z - fz * 4.0;
		} else {
			ex = eye.x; ey = eye.y; ez = eye.z;
		}

		w = mc.getWindow().getGuiScaledWidth();
		h = mc.getWindow().getGuiScaledHeight();
		tanHalf = Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
		aspect = w / (double) h;
		return true;
	}

	public static boolean project(double x, double y, double z) {
		double dx = x - ex, dy = y - ey, dz = z - ez;
		double d = dx * fx + dy * fy + dz * fz;
		if (d < 0.1) return false;
		double px = dx * rx + dy * ry + dz * rz;
		double py = dx * ux + dy * uy + dz * uz;
		sx = w / 2.0 + (px / (d * tanHalf * aspect)) * (w / 2.0);
		sy = h / 2.0 - (py / (d * tanHalf)) * (h / 2.0);
		depth = d;
		return true;
	}

	/** Screen rectangle {x1, y1, x2, y2} around a box, or false if any corner is behind the camera. */
	public static boolean rect(AABB bb, double ox, double oy, double oz, int[] out) {
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (int i = 0; i < 8; i++) {
			double cx = ((i & 1) == 0 ? bb.minX : bb.maxX) + ox;
			double cy = ((i & 2) == 0 ? bb.minY : bb.maxY) + oy;
			double cz = ((i & 4) == 0 ? bb.minZ : bb.maxZ) + oz;
			if (!project(cx, cy, cz)) return false;
			minX = Math.min(minX, sx); maxX = Math.max(maxX, sx);
			minY = Math.min(minY, sy); maxY = Math.max(maxY, sy);
		}
		out[0] = (int) Math.floor(minX);
		out[1] = (int) Math.floor(minY);
		out[2] = (int) Math.ceil(maxX);
		out[3] = (int) Math.ceil(maxY);
		if (out[2] - out[0] < 4) { out[2] += 2; out[0] -= 2; }
		if (out[3] - out[1] < 4) { out[3] += 2; out[1] -= 2; }
		return out[2] - out[0] <= w * 4 && out[3] - out[1] <= h * 4;
	}
}
