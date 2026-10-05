package com.greenmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

/**
 * Kelp Client menu. Sidebar with categories, module cards, a settings page per module.
 * Input is polled via GLFW (mouse + keys); text comes from charTyped and scrolling from mouseScrolled.
 */
public class GreenGui extends Screen {
	private static final int WW = 470, WH = 290, SIDE = 112, TOP = 44, BOTTOM = 282, CX = 124, CW = 336, RH = 28;
	private static final int[] PALETTE = {
			0x39FF14, 0xFF3B30, 0xFF9500, 0xFFD60A, 0x30D158, 0x00E5FF,
			0x0A84FF, 0xBF5AF2, 0xFF2D95, 0xFFFFFF, 0xAAAAAA, 0x000000
	};

	private Category cat = Category.VISUALS;
	private Module open;
	private Macros.Macro openMacro;

	private float scroll, scrollTarget, contentH, openAnim;
	private float dt;
	private long lastNs = System.nanoTime();

	private boolean prevL, prevR;
	private final boolean[] prevKey = new boolean[349];
	private boolean cL, cR, cLDown;
	private float lx, ly;

	private Object listening;      // Module | Setting.Key | Macros.Macro
	private Object inputTarget;    // Setting.Text | Setting.Items | Macros.Macro | Macros.Step | "profile"
	private final StringBuilder input = new StringBuilder();

	private Setting.Num dragNum;
	private Setting.Color pickerOpen;
	private final float[] hsv = new float[3];
	private int dragStrip = -1;
	private boolean confirmReset;

	public GreenGui() {
		super(Component.literal("Kelp Client"));
		prevL = Keys.mouse(0);
		prevR = Keys.mouse(1);
		for (int k : Keys.VALID) prevKey[k] = Keys.down(k);
	}

	@Override public boolean isPauseScreen() { return false; }
	@Override public boolean shouldCloseOnEsc() { return false; }

	@Override
	public void onClose() {
		Config.save();
		super.onClose();
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (inputTarget != null) {
			int cp = event.codepoint();
			if (cp >= 32 && cp != 127 && input.length() < 120) input.appendCodePoint(cp);
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		scrollTarget -= (float) scrollY * 28f;
		clampScroll();
		return true;
	}

	private void clampScroll() {
		float max = Math.max(0, contentH - (BOTTOM - TOP));
		scrollTarget = Math.max(0, Math.min(max, scrollTarget));
	}

	private void resetScroll() {
		scroll = 0;
		scrollTarget = 0;
		pickerOpen = null;
	}

	// ---------------------------------------------------------------- colors
	private int acc() { return Ui.accent(); }
	private int rad() { return Modules.GUI_RADIUS.i(); }
	private int txt() { return Modules.GUI_TEXT.rgb; }
	private int dim() { return Ui.mix(Modules.GUI_TEXT.rgb, Modules.GUI_BG.rgb, 0.45); }
	private int panel() { return Ui.mix(Modules.GUI_BG.rgb, 0xFFFFFF, 0.03); }
	private int card() { return Ui.mix(Modules.GUI_BG.rgb, 0xFFFFFF, 0.08); }
	private int cardHover() { return Ui.mix(Modules.GUI_BG.rgb, 0xFFFFFF, 0.14); }

	// ---------------------------------------------------------------- helpers
	private boolean busy() { return listening != null || inputTarget != null; }
	private boolean vis(int y, int h) { return y + h > TOP - 2 && y + h <= BOTTOM + 2; }
	private boolean hov(int x, int y, int w, int h) { return lx >= x && lx < x + w && ly >= y && ly < y + h; }
	private boolean hovC(int x, int y, int w, int h) { return hov(x, y, w, h) && y >= TOP - 1 && y + h <= BOTTOM + 4; }
	private boolean click(int x, int y, int w, int h) { return cL && !busy() && hovC(x, y, w, h); }
	private boolean rclick(int x, int y, int w, int h) { return cR && !busy() && hovC(x, y, w, h); }

	private void text(GuiGraphicsExtractor g, String s, int x, int y, int rgb) { Ui.text(g, this.font, s, x, y, rgb); }
	private void small(GuiGraphicsExtractor g, String s, int x, int y, int rgb) { Ui.small(g, this.font, s, x, y, rgb, 0.75f); }
	private void textR(GuiGraphicsExtractor g, String s, int rightX, int y, int rgb) { text(g, s, rightX - this.font.width(s), y, rgb); }

	private void sw(GuiGraphicsExtractor g, int x, int y, float anim, boolean ro) {
		int track = Ui.mix(0x3A3F47, acc(), anim);
		if (ro) track = Ui.mix(track, Modules.GUI_BG.rgb, 0.55);
		Ui.rr(g, x, y, 28, 12, 6, Ui.opaque(track));
		int kx = x + 1 + Math.round(anim * 16);
		Ui.rr(g, kx, y + 1, 10, 10, 5, Ui.opaque(ro ? 0x7A7A7A : 0xFFFFFF));
	}

	private boolean btn(GuiGraphicsExtractor g, int x, int y, int w, int h, String label, boolean primary) {
		boolean hv = hovC(x, y, w, h);
		int bg = primary ? Ui.mix(Modules.GUI_BG.rgb, acc(), hv ? 0.6 : 0.4) : (hv ? cardHover() : card());
		Ui.rr(g, x, y, w, h, Math.min(rad(), h / 2), Ui.opaque(bg));
		text(g, label, x + (w - this.font.width(label)) / 2, y + (h - 8) / 2, txt());
		return click(x, y, w, h);
	}

	private void openModule(Module m) {
		open = m;
		resetScroll();
	}

	private float smooth(float cur, float target) {
		if (!Modules.GUI_ANIM.value) return target;
		return cur + (target - cur) * Math.min(1f, dt * 14f);
	}

	// ---------------------------------------------------------------- frame
	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		long now = System.nanoTime();
		dt = Math.min(0.05f, (now - lastNs) / 1e9f);
		lastNs = now;
		openAnim = Modules.GUI_ANIM.value ? openAnim + (1f - openAnim) * Math.min(1f, dt * 12f) : 1f;
		scroll = Modules.GUI_ANIM.value ? scroll + (scrollTarget - scroll) * Math.min(1f, dt * 14f) : scrollTarget;

		float sc = (float) Modules.GUI_SCALE.value;
		float ox = (this.width - WW * sc) / 2f;
		float oy = (this.height - WH * sc) / 2f + (1f - openAnim) * 14f;
		lx = (mouseX - ox) / sc;
		ly = (mouseY - oy) / sc;

		boolean l = Keys.mouse(0), r = Keys.mouse(1);
		cL = l && !prevL;
		cR = r && !prevR;
		cLDown = l;
		prevL = l;
		prevR = r;
		if (!l) { dragNum = null; dragStrip = -1; }

		int pressed = -1;
		for (int k : Keys.VALID) {
			boolean d = Keys.down(k);
			if (d && !prevKey[k] && pressed == -1) pressed = k;
			prevKey[k] = d;
		}
		if (pressed != -1) {
			if (inputTarget != null) {
				if (pressed == GLFW.GLFW_KEY_BACKSPACE && input.length() > 0) input.setLength(input.length() - 1);
				else if (pressed == GLFW.GLFW_KEY_ENTER || pressed == GLFW.GLFW_KEY_KP_ENTER) commitInput();
				else if (pressed == GLFW.GLFW_KEY_ESCAPE) { inputTarget = null; input.setLength(0); }
			} else if (listening != null) {
				int bound = pressed == GLFW.GLFW_KEY_ESCAPE ? -1 : pressed;
				if (listening instanceof Module m) m.key = bound;
				else if (listening instanceof Setting.Key ks) ks.key = bound;
				else if (listening instanceof Macros.Macro mm) mm.key = bound;
				listening = null;
			} else if (pressed == GLFW.GLFW_KEY_RIGHT_SHIFT) {
				onClose();
				return;
			} else if (pressed == GLFW.GLFW_KEY_ESCAPE) {
				if (open != null || openMacro != null) { open = null; openMacro = null; resetScroll(); }
				else { onClose(); return; }
			}
		}

		g.fill(0, 0, this.width, this.height, Ui.a(0x000000, 0.5));

		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(ox, oy);
		pose.scale(sc, sc);

		// window
		Ui.rr(g, -1, -1, WW + 2, WH + 2, rad() + 1, Ui.a(acc(), 0.35));
		Ui.rr(g, 0, 0, WW, WH, rad(), Ui.a(Modules.GUI_BG.rgb, Modules.GUI_OPACITY.value / 100.0));

		drawContent(g);
		clampScroll();

		// header + sidebar over the scrolled content
		g.fill(0, rad(), WW, TOP, Ui.opaque(panel()));
		Ui.rr(g, 0, 0, WW, TOP, rad(), Ui.opaque(panel()));
		g.fill(CX - 6, TOP, WW, TOP + 1, Ui.a(acc(), 0.4));
		drawHeader(g);
		drawSidebar(g);

		pose.popMatrix();
	}

	private void drawHeader(GuiGraphicsExtractor g) {
		int kx = 14, ky = 14;
		g.fill(kx, ky, kx + 10, ky + 10, 0xFF2F4A24);
		g.fill(kx, ky, kx + 10, ky + 1, 0xFF5E8A45);
		g.fill(kx + 2, ky + 1, kx + 3, ky + 10, 0xFF4A7A36);
		g.fill(kx + 5, ky + 1, kx + 6, ky + 10, 0xFF4A7A36);
		g.fill(kx + 8, ky + 1, kx + 9, ky + 10, 0xFF3C6A2C);
		text(g, "Kelp Client", 30, 10, acc());
		small(g, "v" + GreenModClient.VERSION, 30, 24, dim());
		String played = String.format(Locale.ROOT, "Played with Kelp Client: %.1f h", Config.playSeconds / 3600.0);
		textR(g, played, WW - 14, 10, txt());
		String env = "Environment: " + ServerSafety.label() + (ServerSafety.allowsRestricted() ? "" : "  (restricted modules locked)");
		Ui.small(g, this.font, env, WW - 14 - this.font.width(env) * 0.75f, 24, ServerSafety.allowsRestricted() ? 0x7CFC9A : 0xE57373, 0.75f);
	}

	private void drawSidebar(GuiGraphicsExtractor g) {
		g.fill(0, TOP, SIDE, WH - rad(), Ui.opaque(panel()));
		Ui.rr(g, 0, WH - 2 * rad() - 4, SIDE, 2 * rad() + 4, rad(), Ui.opaque(panel()));
		int y = TOP + 8;
		for (Category c : Category.values()) {
			boolean sel = c == cat;
			boolean hv = hov(8, y, SIDE - 16, 22);
			if (sel) Ui.rr(g, 8, y, SIDE - 16, 22, Math.min(rad(), 8), Ui.a(acc(), 0.22));
			else if (hv) Ui.rr(g, 8, y, SIDE - 16, 22, Math.min(rad(), 8), Ui.a(0xFFFFFF, 0.06));
			if (sel) Ui.rr(g, 8, y + 4, 2, 14, 1, Ui.opaque(acc()));
			text(g, c.title, 20, y + 7, sel ? acc() : txt());
			if (cL && !busy() && hv && c != cat) {
				cat = c;
				open = null;
				openMacro = null;
				confirmReset = false;
				resetScroll();
			}
			y += 24;
		}
	}

	private void drawContent(GuiGraphicsExtractor g) {
		int y = TOP + 8 - Math.round(scroll);
		int end;
		if (openMacro != null) end = macroEditor(g, openMacro, y);
		else if (open != null) end = modulePage(g, open, y);
		else end = categoryList(g, y);
		contentH = end + Math.round(scroll) - TOP + 8;
	}

	// ---------------------------------------------------------------- module list
	private int categoryList(GuiGraphicsExtractor g, int y) {
		if (cat == Category.SETTINGS) y = profilesCard(g, y);
		List<Module> mods = Modules.byCategory(cat);
		for (Module m : mods) y = moduleRow(g, m, y);
		if (cat == Category.MACRO) y = macroList(g, y);
		if (mods.isEmpty()) {
			if (vis(y, 20)) text(g, "Nothing here yet - more modules arrive in the next update.", CX + 4, y + 4, dim());
			y += 24;
		}
		return y;
	}

	private int moduleRow(GuiGraphicsExtractor g, Module m, int y) {
		m.anim = smooth(m.anim, m.isEnabled() ? 1f : 0f);
		if (vis(y, RH)) {
			boolean locked = m.isLocked();
			boolean hv = hovC(CX, y, CW, RH);
			Ui.rr(g, CX, y, CW, RH, rad(), Ui.opaque(hv ? cardHover() : card()));
			if (m.isEnabled() && !m.noToggle && !locked) Ui.rr(g, CX, y + 6, 3, RH - 12, 1, Ui.opaque(acc()));
			text(g, m.name, CX + 10, y + 5, locked ? dim() : txt());
			String sub = locked ? ServerSafety.lockReason() : m.description;
			small(g, sub, CX + 10, y + 17, locked ? 0xE57373 : dim());

			boolean inSwitch = false;
			if (!m.noToggle) {
				int sx = CX + CW - 40;
				sw(g, sx, y + 8, m.anim, locked);
				inSwitch = hov(sx - 2, y + 4, 32, 20);
				int rx = sx - 8;
				if (locked) {
					Ui.lock(g, rx - 7, y + 9, 0xE57373);
					rx -= 14;
				}
				if (m.key >= 0) {
					String k = Keys.name(m.key);
					small(g, k, rx - (int) (this.font.width(k) * 0.75f), y + 10, 0x9FE8A8);
				}
			}
			if (click(CX, y, CW, RH)) {
				if (inSwitch && !m.noToggle) {
					if (locked) Notifications.push("This feature is disabled on this server.");
					else m.toggle();
				} else openModule(m);
			}
			if (rclick(CX, y, CW, RH)) openModule(m);
		}
		return y + RH + 4;
	}

	// ---------------------------------------------------------------- module page
	private int modulePage(GuiGraphicsExtractor g, Module m, int y) {
		boolean ro = m.isLocked();
		m.anim = smooth(m.anim, m.isEnabled() ? 1f : 0f);

		if (vis(y, 18)) {
			if (btn(g, CX, y, 54, 18, "< Back", false)) { open = null; resetScroll(); return y; }
			text(g, m.name, CX + 66, y + 5, acc());
		}
		y += 24;
		if (vis(y, 12)) small(g, m.description, CX + 2, y, dim());
		y += 14;

		if (ro) {
			if (vis(y, 20)) {
				Ui.rr(g, CX, y, CW, 20, rad(), Ui.a(0xE57373, 0.18));
				Ui.lock(g, CX + 8, y + 5, 0xE57373);
				text(g, ServerSafety.lockReason() + " - settings are read-only", CX + 22, y + 6, 0xE57373);
			}
			y += 26;
		}

		if (!m.noToggle) {
			if (vis(y, 26)) {
				Ui.rr(g, CX, y, CW, 26, rad(), Ui.opaque(card()));
				text(g, "Enabled", CX + 10, y + 9, txt());
				sw(g, CX + CW - 40, y + 7, m.anim, ro);
				if (click(CX, y, CW, 26)) {
					if (ro) Notifications.push("This feature is disabled on this server.");
					else m.toggle();
				}
			}
			y += 30;

			if (vis(y, 26)) {
				Ui.rr(g, CX, y, CW, 26, rad(), Ui.opaque(card()));
				text(g, "Toggle key", CX + 10, y + 9, txt());
				String label = listening == m ? "press a key..." : Keys.name(m.key);
				int bw = Math.max(54, this.font.width(label) + 14);
				boolean bhv = hovC(CX + CW - bw - 10, y + 4, bw, 18);
				Ui.rr(g, CX + CW - bw - 10, y + 4, bw, 18, Math.min(rad(), 9), Ui.opaque(listening == m ? Ui.mix(card(), acc(), 0.4) : (bhv ? cardHover() : Ui.mix(card(), 0, 0.2))));
				text(g, label, CX + CW - bw - 10 + (bw - this.font.width(label)) / 2, y + 9, txt());
				if (click(CX + CW - bw - 10, y + 4, bw, 18) && !ro) listening = m;
			}
			y += 30;
		}

		for (Setting s : m.settings) y = settingRow(g, m, s, y, ro);

		if (vis(y, 22)) {
			if (btn(g, CX, y, 120, 22, "Reset " + (m.name.length() > 14 ? "module" : m.name), false) && !ro) {
				m.reset();
				pickerOpen = null;
				Notifications.push(m.name + " reset to defaults.");
			}
		}
		return y + 30;
	}

	private int settingRow(GuiGraphicsExtractor g, Module m, Setting s, int y, boolean ro) {
		boolean can = !ro;
		int rx = CX + CW - 28; // right edge of controls (reset button sits to the right)

		if (s instanceof Setting.Items it) return itemsRow(g, it, y, ro);

		int h = s instanceof Setting.Num ? 36 : 26;
		if (!vis(y, h)) {
			int extra = (s == pickerOpen && s instanceof Setting.Color) ? 76 : 0;
			return y + h + 4 + extra;
		}

		Ui.rr(g, CX, y, CW, h, rad(), Ui.opaque(card()));
		text(g, s.name, CX + 10, y + 9 - (s instanceof Setting.Num ? 3 : 0), ro ? dim() : txt());

		// reset button
		int bx = CX + CW - 20, by = y + (h - 14) / 2;
		boolean rh = hovC(bx, by, 14, 14);
		Ui.rr(g, bx, by, 14, 14, Math.min(rad(), 7), Ui.opaque(rh ? cardHover() : Ui.mix(card(), 0, 0.2)));
		small(g, "R", bx + 5, by + 4, dim());
		if (click(bx, by, 14, 14) && can) s.reset();

		if (s instanceof Setting.Bool b) {
			float a = b.value ? 1f : 0f;
			sw(g, rx - 28, y + 7, a, ro);
			if (click(CX, y, CW - 26, h) && can) b.value = !b.value;
		} else if (s instanceof Setting.Num n) {
			String val = n.step >= 1 ? String.valueOf(n.i()) : String.format(Locale.ROOT, "%.2f", n.value);
			textR(g, val, rx, y + 6, txt());
			int tx = CX + 10, tw = CW - 46, ty = y + 22;
			double frac = (n.value - n.min) / (n.max - n.min);
			Ui.rr(g, tx, ty, tw, 6, 3, Ui.opaque(Ui.mix(card(), 0, 0.35)));
			int fw = Math.max(6, (int) (tw * frac));
			Ui.rr(g, tx, ty, fw, 6, 3, Ui.opaque(ro ? dim() : acc()));
			Ui.rr(g, tx + fw - 4, ty - 2, 10, 10, 5, Ui.opaque(ro ? 0x7A7A7A : 0xFFFFFF));
			if (can && cL && !busy() && hovC(tx - 4, ty - 6, tw + 8, 18)) dragNum = n;
			if (can && dragNum == n && cLDown) {
				double f = Math.max(0.0, Math.min(1.0, (lx - tx) / (double) tw));
				double v = n.min + f * (n.max - n.min);
				v = Math.round(v / n.step) * n.step;
				n.value = Math.max(n.min, Math.min(n.max, v));
			}
		} else if (s instanceof Setting.Choice c) {
			String label = "<  " + c.get() + "  >";
			int bw = this.font.width(label) + 14;
			int px = rx - bw;
			boolean hv = hovC(px, y + 4, bw, 18);
			Ui.rr(g, px, y + 4, bw, 18, Math.min(rad(), 9), Ui.opaque(hv ? cardHover() : Ui.mix(card(), 0, 0.2)));
			text(g, label, px + 7, y + 9, txt());
			if (click(px, y + 4, bw, 18) && can) c.next(1);
			if (rclick(px, y + 4, bw, 18) && can) c.next(-1);
		} else if (s instanceof Setting.Key k) {
			String label = listening == k ? "press a key..." : Keys.name(k.key);
			int bw = Math.max(54, this.font.width(label) + 14);
			int px = rx - bw;
			boolean hv = hovC(px, y + 4, bw, 18);
			Ui.rr(g, px, y + 4, bw, 18, Math.min(rad(), 9), Ui.opaque(listening == k ? Ui.mix(card(), acc(), 0.4) : (hv ? cardHover() : Ui.mix(card(), 0, 0.2))));
			text(g, label, px + (bw - this.font.width(label)) / 2, y + 9, txt());
			if (click(px, y + 4, bw, 18) && can) listening = k;
		} else if (s instanceof Setting.Text t) {
			boolean editing = inputTarget == t;
			String label = editing ? input + (System.currentTimeMillis() / 400 % 2 == 0 ? "|" : "") : (t.value.isEmpty() ? "(click to edit)" : t.value);
			if (label.length() > 28) label = "..." + label.substring(label.length() - 25);
			int bw = Math.max(110, this.font.width(label) + 14);
			int px = rx - bw;
			boolean hv = hovC(px, y + 4, bw, 18);
			Ui.rr(g, px, y + 4, bw, 18, Math.min(rad(), 9), Ui.opaque(editing ? Ui.mix(card(), acc(), 0.3) : (hv ? cardHover() : Ui.mix(card(), 0, 0.2))));
			text(g, label, px + 7, y + 9, t.value.isEmpty() && !editing ? dim() : txt());
			if (click(px, y + 4, bw, 18) && can) {
				inputTarget = t;
				input.setLength(0);
				input.append(t.value);
			}
		} else if (s instanceof Setting.Color c) {
			int px = rx - 40;
			boolean hv = hovC(px, y + 4, 40, 18);
			Ui.rr(g, px - 1, y + 3, 42, 20, Math.min(rad(), 9), Ui.opaque(hv ? 0xFFFFFF : Ui.mix(card(), 0xFFFFFF, 0.3)));
			Ui.rr(g, px, y + 4, 40, 18, Math.min(rad(), 9), Ui.opaque(c.rgb));
			if (click(px, y + 4, 40, 18) && can) {
				if (pickerOpen == c) pickerOpen = null;
				else {
					pickerOpen = c;
					float[] v = Ui.toHsv(c.rgb);
					hsv[0] = v[0]; hsv[1] = v[1]; hsv[2] = v[2];
				}
			}
			if (pickerOpen == c) {
				colorPicker(g, c, y + h + 4, can);
				return y + h + 4 + 76 + 4;
			}
		}
		return y + h + 4;
	}

	private void colorPicker(GuiGraphicsExtractor g, Setting.Color c, int y, boolean can) {
		Ui.rr(g, CX, y, CW, 76, rad(), Ui.opaque(Ui.mix(card(), 0, 0.15)));
		for (int i = 0; i < PALETTE.length; i++) {
			int sx = CX + 10 + i * 26;
			Ui.rr(g, sx, y + 6, 22, 14, 4, Ui.opaque(PALETTE[i]));
			if (click(sx, y + 6, 22, 14) && can) {
				c.rgb = PALETTE[i];
				float[] v = Ui.toHsv(c.rgb);
				hsv[0] = v[0]; hsv[1] = v[1]; hsv[2] = v[2];
			}
		}
		int sx0 = CX + 10, sw = CW - 20;
		for (int strip = 0; strip < 3; strip++) {
			int sy = y + 26 + strip * 16;
			for (int i = 0; i < sw; i += 2) {
				float f = i / (float) sw;
				int col = strip == 0 ? Ui.hsv(f, 1f, 1f) : strip == 1 ? Ui.hsv(hsv[0], f, hsv[2]) : Ui.hsv(hsv[0], hsv[1], f);
				g.fill(sx0 + i, sy, sx0 + i + 2, sy + 10, Ui.opaque(col));
			}
			float cur = hsv[strip];
			int mx = sx0 + (int) (cur * sw);
			g.fill(mx - 1, sy - 2, mx + 1, sy + 12, 0xFFFFFFFF);
			if (can && cL && !busy() && hovC(sx0 - 3, sy - 3, sw + 6, 16)) dragStrip = strip;
			if (can && dragStrip == strip && cLDown) {
				hsv[strip] = Math.max(0f, Math.min(1f, (lx - sx0) / (float) sw));
				if (strip == 0) hsv[0] = Math.min(0.999f, hsv[0]);
				c.rgb = Ui.hsv(hsv[0], hsv[1], hsv[2]);
			}
		}
	}

	private int itemsRow(GuiGraphicsExtractor g, Setting.Items it, int y, boolean ro) {
		boolean can = !ro;
		int total = 26 + it.values.size() * 22 + 26;
		if (vis(y, 24)) {
			Ui.rr(g, CX, y, CW, 24, rad(), Ui.opaque(card()));
			text(g, it.name + " (" + it.values.size() + ")", CX + 10, y + 8, ro ? dim() : txt());
			int bx = CX + CW - 20;
			boolean rh = hovC(bx, y + 5, 14, 14);
			Ui.rr(g, bx, y + 5, 14, 14, Math.min(rad(), 7), Ui.opaque(rh ? cardHover() : Ui.mix(card(), 0, 0.2)));
			small(g, "R", bx + 5, y + 9, dim());
			if (click(bx, y + 5, 14, 14) && can) it.reset();
		}
		y += 26;

		for (int i = 0; i < it.values.size(); i++) {
			String e = it.values.get(i);
			if (vis(y, 20)) {
				boolean on = it.isOn(e);
				Ui.rr(g, CX + 8, y, CW - 16, 20, Math.min(rad(), 8), Ui.opaque(Ui.mix(card(), 0, 0.1)));
				text(g, Setting.Items.clean(e), CX + 16, y + 6, on ? txt() : dim());
				int xx = CX + CW - 34;
				boolean xh = hovC(xx, y + 3, 14, 14);
				Ui.rr(g, xx, y + 3, 14, 14, 7, Ui.opaque(xh ? 0xB04A4A : Ui.mix(card(), 0, 0.25)));
				small(g, "x", xx + 5, y + 7, 0xFFFFFF);
				if (it.toggleable) {
					float a = on ? 1f : 0f;
					sw(g, xx - 36, y + 4, a, ro);
					if (click(xx - 38, y + 2, 32, 16) && can) {
						it.values.set(i, on ? "-" + e : Setting.Items.clean(e));
						return y + 22 + 26 + (it.values.size() - i - 1) * 22 - 0;
					}
				}
				if (click(xx, y + 3, 14, 14) && can) {
					it.values.remove(i);
					return y + 22 + 26 + (it.values.size() - i) * 22 - 0;
				}
			}
			y += 22;
		}

		if (vis(y, 22)) {
			boolean editing = inputTarget == it;
			boolean hv = hovC(CX + 8, y, CW - 16, 22);
			Ui.rr(g, CX + 8, y, CW - 16, 22, Math.min(rad(), 8), Ui.opaque(editing ? Ui.mix(card(), acc(), 0.3) : (hv ? cardHover() : Ui.mix(card(), 0, 0.2))));
			String label = editing ? input + (System.currentTimeMillis() / 400 % 2 == 0 ? "|" : "") : "+ Add entry (click, type, press Enter)";
			text(g, label, CX + 16, y + 7, editing ? txt() : dim());
			if (click(CX + 8, y, CW - 16, 22) && can) {
				inputTarget = it;
				input.setLength(0);
			}
		}
		return y + 26 + 4;
	}

	// ---------------------------------------------------------------- profiles (Settings)
	private int profilesCard(GuiGraphicsExtractor g, int y) {
		if (vis(y, 26)) {
			Ui.rr(g, CX, y, CW, 26, rad(), Ui.opaque(card()));
			text(g, "Profile: " + Config.profile, CX + 10, y + 9, acc());
			String played = String.format(Locale.ROOT, "%.1f h played", Config.playSeconds / 3600.0);
			textR(g, played, CX + CW - 10, y + 9, txt());
		}
		y += 30;

		if (vis(y, 22)) {
			if (btn(g, CX, y, 90, 22, "Save", true)) { Config.save(); Notifications.push("Profile saved."); }
			if (btn(g, CX + 96, y, 130, 22, confirmReset ? "Click again to confirm" : "Reset everything", false)) {
				if (confirmReset) {
					Modules.resetAll();
					Notifications.push("Everything was reset to defaults.");
					confirmReset = false;
				} else confirmReset = true;
			}
		}
		y += 28;

		if (vis(y, 22)) {
			boolean editing = "profile".equals(inputTarget);
			boolean hv = hovC(CX, y, CW, 22);
			Ui.rr(g, CX, y, CW, 22, rad(), Ui.opaque(editing ? Ui.mix(card(), acc(), 0.3) : (hv ? cardHover() : card())));
			String label = editing ? input + (System.currentTimeMillis() / 400 % 2 == 0 ? "|" : "") : "+ New profile (click, type a name, press Enter)";
			text(g, label, CX + 10, y + 7, editing ? txt() : dim());
			if (click(CX, y, CW, 22)) { inputTarget = "profile"; input.setLength(0); }
		}
		y += 28;

		for (String p : Config.profiles()) {
			if (vis(y, 24)) {
				boolean cur = p.equals(Config.profile);
				Ui.rr(g, CX, y, CW, 24, rad(), Ui.opaque(card()));
				if (cur) Ui.rr(g, CX, y + 5, 3, 14, 1, Ui.opaque(acc()));
				text(g, p, CX + 10, y + 8, cur ? acc() : txt());
				if (!cur) {
					if (btn(g, CX + CW - 126, y + 3, 56, 18, "Load", false)) {
						Config.switchProfile(p);
						Notifications.push("Loaded profile " + p + ".");
						return y;
					}
					if (btn(g, CX + CW - 64, y + 3, 56, 18, "Delete", false)) {
						Config.deleteProfile(p);
						return y;
					}
				} else small(g, "active", CX + CW - 50, y + 9, dim());
			}
			y += 28;
		}
		return y + 6;
	}

	// ---------------------------------------------------------------- macros
	private int macroList(GuiGraphicsExtractor g, int y) {
		if (vis(y, 22)) {
			text(g, "Your macros", CX + 4, y + 7, txt());
			if (btn(g, CX + CW - 90, y, 90, 22, "+ New macro", true)) {
				Macros.Macro m = new Macros.Macro();
				m.steps.add(new Macros.Step("", 250));
				Macros.LIST.add(m);
				openMacro = m;
				resetScroll();
				return y;
			}
		}
		y += 28;

		for (int i = 0; i < Macros.LIST.size(); i++) {
			Macros.Macro m = Macros.LIST.get(i);
			if (vis(y, RH)) {
				boolean hv = hovC(CX, y, CW, RH);
				Ui.rr(g, CX, y, CW, RH, rad(), Ui.opaque(hv ? cardHover() : card()));
				text(g, m.name, CX + 10, y + 5, m.enabled ? txt() : dim());
				small(g, m.steps.size() + " step(s)   key: " + Keys.name(m.key), CX + 10, y + 17, dim());
				float a = m.enabled ? 1f : 0f;
				int sx = CX + CW - 40;
				sw(g, sx, y + 8, a, false);
				int xx = sx - 24;
				boolean xh = hovC(xx, y + 7, 14, 14);
				Ui.rr(g, xx, y + 7, 14, 14, 7, Ui.opaque(xh ? 0xB04A4A : Ui.mix(card(), 0, 0.25)));
				small(g, "x", xx + 5, y + 11, 0xFFFFFF);
				if (click(xx, y + 7, 14, 14)) { Macros.LIST.remove(i); return y; }
				if (click(sx - 2, y + 4, 32, 20)) m.enabled = !m.enabled;
				else if (click(CX, y, CW, RH)) { openMacro = m; resetScroll(); return y; }
			}
			y += RH + 4;
		}
		if (Macros.LIST.isEmpty()) {
			if (vis(y, 20)) text(g, "No macros yet. Create one with the button above.", CX + 4, y + 4, dim());
			y += 24;
		}
		return y;
	}

	private int macroEditor(GuiGraphicsExtractor g, Macros.Macro m, int y) {
		if (vis(y, 18)) {
			if (btn(g, CX, y, 54, 18, "< Back", false)) { openMacro = null; resetScroll(); return y; }
			text(g, "Edit macro", CX + 66, y + 5, acc());
		}
		y += 26;

		// name
		if (vis(y, 26)) {
			Ui.rr(g, CX, y, CW, 26, rad(), Ui.opaque(card()));
			text(g, "Name", CX + 10, y + 9, txt());
			boolean editing = inputTarget == m;
			String label = editing ? input + (System.currentTimeMillis() / 400 % 2 == 0 ? "|" : "") : m.name;
			int bw = Math.max(110, this.font.width(label) + 14);
			int px = CX + CW - bw - 10;
			Ui.rr(g, px, y + 4, bw, 18, Math.min(rad(), 9), Ui.opaque(editing ? Ui.mix(card(), acc(), 0.3) : Ui.mix(card(), 0, 0.2)));
			text(g, label, px + 7, y + 9, txt());
			if (click(px, y + 4, bw, 18)) { inputTarget = m; input.setLength(0); input.append(m.name); }
		}
		y += 30;

		// key
		if (vis(y, 26)) {
			Ui.rr(g, CX, y, CW, 26, rad(), Ui.opaque(card()));
			text(g, "Key", CX + 10, y + 9, txt());
			String label = listening == m ? "press a key..." : Keys.name(m.key);
			int bw = Math.max(54, this.font.width(label) + 14);
			int px = CX + CW - bw - 10;
			Ui.rr(g, px, y + 4, bw, 18, Math.min(rad(), 9), Ui.opaque(listening == m ? Ui.mix(card(), acc(), 0.4) : Ui.mix(card(), 0, 0.2)));
			text(g, label, px + (bw - this.font.width(label)) / 2, y + 9, txt());
			if (click(px, y + 4, bw, 18)) listening = m;
		}
		y += 30;

		// enabled
		if (vis(y, 26)) {
			Ui.rr(g, CX, y, CW, 26, rad(), Ui.opaque(card()));
			text(g, "Enabled", CX + 10, y + 9, txt());
			sw(g, CX + CW - 40, y + 7, m.enabled ? 1f : 0f, false);
			if (click(CX, y, CW, 26)) m.enabled = !m.enabled;
		}
		y += 30;

		if (vis(y, 14)) small(g, "Steps run in order. Start a step with / for a command. Delay = wait before the step (ms).", CX + 2, y + 2, dim());
		y += 16;

		for (int i = 0; i < m.steps.size(); i++) {
			Macros.Step st = m.steps.get(i);
			if (vis(y, 26)) {
				Ui.rr(g, CX, y, CW, 26, rad(), Ui.opaque(card()));
				small(g, String.valueOf(i + 1), CX + 8, y + 10, dim());
				boolean editing = inputTarget == st;
				String label = editing ? input + (System.currentTimeMillis() / 400 % 2 == 0 ? "|" : "") : (st.text.isEmpty() ? "(click to type)" : st.text);
				int fw = CW - 150;
				if (this.font.width(label) > fw - 12) {
					while (label.length() > 4 && this.font.width("..." + label) > fw - 12) label = label.substring(1);
					label = "..." + label;
				}
				Ui.rr(g, CX + 22, y + 4, fw, 18, Math.min(rad(), 9), Ui.opaque(editing ? Ui.mix(card(), acc(), 0.3) : Ui.mix(card(), 0, 0.2)));
				text(g, label, CX + 28, y + 9, st.text.isEmpty() && !editing ? dim() : txt());
				if (click(CX + 22, y + 4, fw, 18)) { inputTarget = st; input.setLength(0); input.append(st.text); }

				int dx = CX + 22 + fw + 6;
				int step = Keys.shift() ? 250 : 50;
				if (btn(g, dx, y + 4, 16, 18, "-", false)) st.delayMs = Math.max(0, st.delayMs - step);
				String d = st.delayMs + "ms";
				text(g, d, dx + 20, y + 9, txt());
				if (btn(g, dx + 20 + this.font.width("0000ms") + 2, y + 4, 16, 18, "+", false)) st.delayMs = Math.min(10000, st.delayMs + step);
				int xx = CX + CW - 22;
				boolean xh = hovC(xx, y + 6, 14, 14);
				Ui.rr(g, xx, y + 6, 14, 14, 7, Ui.opaque(xh ? 0xB04A4A : Ui.mix(card(), 0, 0.25)));
				small(g, "x", xx + 5, y + 10, 0xFFFFFF);
				if (click(xx, y + 6, 14, 14)) { m.steps.remove(i); return y; }
			}
			y += 30;
		}

		if (vis(y, 22)) {
			if (btn(g, CX, y, 100, 22, "+ Add step", true)) m.steps.add(new Macros.Step("", 250));
			if (btn(g, CX + 108, y, 110, 22, "Delete macro", false)) {
				Macros.LIST.remove(m);
				openMacro = null;
				resetScroll();
				return y;
			}
		}
		return y + 30;
	}

	// ---------------------------------------------------------------- text input
	private void commitInput() {
		String v = input.toString().trim();
		if (inputTarget instanceof Setting.Text t) {
			t.value = v;
		} else if (inputTarget instanceof Setting.Items it) {
			boolean dup = false;
			for (String e : it.values) if (Setting.Items.clean(e).equalsIgnoreCase(v)) dup = true;
			if (!v.isEmpty() && !dup) it.values.add(v);
		} else if (inputTarget instanceof Macros.Macro m) {
			if (!v.isEmpty()) m.name = v;
		} else if (inputTarget instanceof Macros.Step s) {
			s.text = v;
		} else if ("profile".equals(inputTarget)) {
			if (!v.isEmpty()) Config.createProfile(v);
		}
		inputTarget = null;
		input.setLength(0);
	}
}
