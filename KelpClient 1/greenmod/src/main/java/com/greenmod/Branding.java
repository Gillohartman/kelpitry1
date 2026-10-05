package com.greenmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Kelp Client label on the Minecraft main menu. */
public final class Branding {
	private Branding() {}

	public static void draw(GuiGraphicsExtractor g) {
		Minecraft mc = Minecraft.getInstance();
		String title = "Kelp Client";
		String sub = "v" + GreenModClient.VERSION;
		int tw = mc.font.width(title);
		int w = 8 + 10 + 6 + tw + 8, h = 24;
		int x = 8, y = 8;

		Ui.rr(g, x, y, w, h, 7, Ui.a(0x111418, 0.82));
		Ui.rr(g, x + 5, y + 5, 14, 14, 4, Ui.a(Ui.accent(), 0.25));
		// kelp block icon
		int kx = x + 8, ky = y + 8;
		g.fill(kx, ky, kx + 8, ky + 8, 0xFF2F4A24);
		g.fill(kx, ky, kx + 8, ky + 1, 0xFF5E8A45);
		g.fill(kx + 1, ky + 1, kx + 2, ky + 8, 0xFF4A7A36);
		g.fill(kx + 4, ky + 1, kx + 5, ky + 8, 0xFF4A7A36);
		g.fill(kx + 6, ky + 1, kx + 7, ky + 8, 0xFF3C6A2C);

		g.text(mc.font, title, x + 26, y + 4, Ui.opaque(Ui.accent()));
		Ui.small(g, mc.font, sub, x + 26, y + 14, 0xAAAAAA, 0.75f);
	}
}
