package com.greenmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/** Read-only preview of a hovered shulker box. Never opens or modifies the container. */
public final class ShulkerPreview {
	private ShulkerPreview() {}

	public static void render(GuiGraphicsExtractor g, int mouseX, int mouseY, Slot hovered) {
		if (!Modules.SHULKER.isActive() || hovered == null) return;
		ItemStack st = hovered.getItem();
		if (st.isEmpty()) return;
		if (!(st.getItem() instanceof BlockItem bi) || !(bi.getBlock() instanceof ShulkerBoxBlock sb)) return;

		Minecraft mc = Minecraft.getInstance();
		NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
		ItemContainerContents contents = st.get(DataComponents.CONTAINER);
		if (contents != null) contents.copyInto(items);

		DyeColor dye = sb.getColor();
		int tint = dye == null ? 0x946794 : dye.getMapColor().col;

		int w = 9 * 18 + 10, h = 3 * 18 + 24;
		float s = (float) Modules.SH_SCALE.value;
		int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
		float pw = w * s, ph = h * s;
		float x, y;
		switch (Modules.SH_ANCHOR.index) {
			case 1: x = 6; y = 6; break;
			case 2: x = sw - pw - 6; y = 6; break;
			case 3: x = 6; y = sh - ph - 6; break;
			case 4: x = sw - pw - 6; y = sh - ph - 6; break;
			default: x = mouseX + 14; y = mouseY - ph - 6; break;
		}
		x += (float) Modules.SH_OX.value;
		y += (float) Modules.SH_OY.value;
		x = Math.max(2, Math.min(sw - pw - 2, x));
		y = Math.max(2, Math.min(sh - ph - 2, y));

		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(s, s);

		Ui.rr(g, 0, 0, w, h, 6, Ui.a(Ui.mix(0x101214, tint, 0.18), 0.95));
		Ui.rr(g, 0, 0, w, 3, 2, Ui.opaque(tint));
		g.text(mc.font, st.getHoverName().getString(), 6, 7, 0xFFFFFFFF);

		for (int i = 0; i < 27; i++) {
			int ix = 5 + (i % 9) * 18;
			int iy = 20 + (i / 9) * 18;
			Ui.rr(g, ix, iy, 17, 17, 2, Ui.a(0x000000, 0.35));
			ItemStack it = items.get(i);
			if (it.isEmpty()) continue;
			g.item(it, ix + 1, iy + 1);
			if (Modules.SH_COUNTS.value) g.itemDecorations(mc.font, it, ix + 1, iy + 1);
		}
		pose.popMatrix();
	}
}
