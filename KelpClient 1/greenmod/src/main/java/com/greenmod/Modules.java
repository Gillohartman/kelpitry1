package com.greenmod;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Every module with its default settings. */
public final class Modules {
	public static final List<Module> ALL = new ArrayList<>();

	private Modules() {}

	private static Module reg(Module m) {
		ALL.add(m);
		return m;
	}

	public static List<Module> byCategory(Category c) {
		List<Module> out = new ArrayList<>();
		for (Module m : ALL) if (m.category == c) out.add(m);
		return out;
	}

	public static void resetAll() {
		for (Module m : ALL) m.reset();
		Macros.LIST.clear();
	}


	// =====================================================================
	// VISUALS
	// =====================================================================

	// ---- Nametags ----
	public static final Module PLAYER_TAGS = reg(new Module("player_tags", "Player Nametags", "Small custom nametags for players", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Color PT_COLOR = PLAYER_TAGS.color("Text color", 0x39FF14);
	public static final Setting.Num PT_SCALE = PLAYER_TAGS.num("Scale", 0.3, 2.0, 0.7, 0.05);
	public static final Setting.Num PT_RANGE = PLAYER_TAGS.num("Range", 8, 128, 64, 1);
	public static final Setting.Bool PT_HEALTH = PLAYER_TAGS.bool("Show health", true);
	public static final Setting.Bool PT_DIST = PLAYER_TAGS.bool("Show distance", false);
	public static final Setting.Bool PT_ARMOR = PLAYER_TAGS.bool("Show armor", false);
	public static final Setting.Bool PT_HELD = PLAYER_TAGS.bool("Show held item", false);
	public static final Setting.Bool PT_BG = PLAYER_TAGS.bool("Background", true);
	public static final Setting.Num PT_BG_OP = PLAYER_TAGS.num("Background opacity %", 0, 100, 45, 5);
	public static final Setting.Bool PT_SELF = PLAYER_TAGS.bool("Own nametag in F5", true);
	public static final Setting.Bool PT_KELP = PLAYER_TAGS.bool("Kelp badge for Kelp Client users", true);
	public static final Setting.Bool PT_ANNOUNCE = PLAYER_TAGS.bool("Mark me as a user (after rejoin)", true);

	public static final Module MOB_TAGS = reg(new Module("mob_tags", "Mob Nametags", "Small nametags with name and optional health", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Color MT_COLOR = MOB_TAGS.color("Text color", 0xFFFFFF);
	public static final Setting.Num MT_SCALE = MOB_TAGS.num("Scale", 0.3, 2.0, 0.6, 0.05);
	public static final Setting.Num MT_RANGE = MOB_TAGS.num("Range", 4, 64, 20, 1);
	public static final Setting.Num MT_MAX = MOB_TAGS.num("Max tags", 1, 64, 16, 1);
	public static final Setting.Bool MT_HEALTH = MOB_TAGS.bool("Show health", true);
	public static final Setting.Bool MT_BG = MOB_TAGS.bool("Background", true);
	public static final Setting.Num MT_BG_OP = MOB_TAGS.num("Background opacity %", 0, 100, 35, 5);

	public static final Module ITEM_TAGS = reg(new Module("item_tags", "Item Nametags", "Names for dropped items", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Color IT_COLOR = ITEM_TAGS.color("Text color", 0xFFE08A);
	public static final Setting.Num IT_SCALE = ITEM_TAGS.num("Scale", 0.3, 2.0, 0.55, 0.05);
	public static final Setting.Num IT_RANGE = ITEM_TAGS.num("Range", 4, 64, 14, 1);
	public static final Setting.Num IT_MAX = ITEM_TAGS.num("Max tags", 1, 64, 16, 1);
	public static final Setting.Bool IT_COUNT = ITEM_TAGS.bool("Show item count", true);
	public static final Setting.Bool IT_BG = ITEM_TAGS.bool("Background", true);
	public static final Setting.Num IT_BG_OP = ITEM_TAGS.num("Background opacity %", 0, 100, 35, 5);

	// ---- Hitboxes ----
	public static final class Hit {
		public final Module module;
		public final Setting.Color color;
		public final Setting.Num thickness, opacity, range;
		public final Setting.Bool visibleOnly;

		Hit(String id, String name, String desc, boolean on, int rgb) {
			module = reg(new Module(id, name, desc, Category.VISUALS, Safety.PUBLIC_SAFE, on));
			color = module.color("Color", rgb);
			thickness = module.num("Line width", 1, 4, 1, 1);
			opacity = module.num("Opacity %", 10, 100, 100, 5);
			range = module.num("Range", 8, 96, 64, 1);
			visibleOnly = module.bool("Only when visible", true);
		}
	}

	public static final Hit H_PLAYERS = new Hit("hit_players", "Hitbox: Players", "2D hitbox box around players", true, 0x39FF14);
	public static final Hit H_HOSTILE = new Hit("hit_hostile", "Hitbox: Hostile Mobs", "2D hitbox box around monsters", false, 0xFF3B30);
	public static final Hit H_PASSIVE = new Hit("hit_passive", "Hitbox: Passive Mobs", "2D hitbox box around animals and NPCs", false, 0x4FC3FF);
	public static final Hit H_ITEMS = new Hit("hit_items", "Hitbox: Items", "2D hitbox box around dropped items", false, 0xFFB020);

	// ---- Misc visuals ----
	public static final Module LOW_FIRE = reg(new Module("low_fire", "Low Fire", "Replaces the big fire overlay with a small one", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Num FIRE_HEIGHT = LOW_FIRE.num("Overlay height %", 0, 100, 18, 1);
	public static final Setting.Num FIRE_OPACITY = LOW_FIRE.num("Opacity %", 0, 100, 65, 5);

	public static final Module SMALL_HANDS = reg(new Module("small_hands", "Small Hands", "Smaller hands and held items", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Num HAND_SCALE = SMALL_HANDS.num("Scale", 0.3, 1.0, 0.6, 0.05);

	public static final Module CROSSHAIR = reg(new Module("crosshair", "Custom Crosshair", "Clean crosshair replacing the vanilla one", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Color CROSS_COLOR = CROSSHAIR.color("Color", 0x39FF14);
	public static final Setting.Num CROSS_LEN = CROSSHAIR.num("Length", 1, 12, 3, 1);
	public static final Setting.Num CROSS_GAP = CROSSHAIR.num("Gap", 0, 8, 2, 1);
	public static final Setting.Num CROSS_THICK = CROSSHAIR.num("Thickness", 1, 3, 1, 1);
	public static final Setting.Bool CROSS_DOT = CROSSHAIR.bool("Center dot", false);

	public static final Module FULLBRIGHT = reg(new Module("fullbright", "Fullbright", "Maximum brightness everywhere", Category.VISUALS, Safety.PUBLIC_SAFE, false));

	public static final Module NO_FOV = reg(new Module("no_fov", "No FOV Effects", "Turns off sprint, speed, underwater and distortion FOV effects", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Bool NF_FOV = NO_FOV.bool("Disable FOV effects", true);
	public static final Setting.Bool NF_DISTORT = NO_FOV.bool("Disable distortion effects", true);

	public static final Module ZOOM = reg(new Module("zoom", "Zoom", "Hold the key to zoom, scroll to adjust", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Key ZOOM_KEY = ZOOM.keybind("Zoom key (hold)", GLFW.GLFW_KEY_C);
	public static final Setting.Num ZOOM_LEVEL = ZOOM.num("Zoom strength", 1.1, 8.0, 1.5, 0.05);
	public static final Setting.Num ZOOM_MIN = ZOOM.num("Minimum zoom", 1.0, 4.0, 1.1, 0.05);
	public static final Setting.Num ZOOM_MAX = ZOOM.num("Maximum zoom", 1.5, 10.0, 4.0, 0.1);
	public static final Setting.Num ZOOM_STEP = ZOOM.num("Scroll step", 0.05, 1.0, 0.15, 0.05);
	public static final Setting.Num ZOOM_SMOOTH = ZOOM.num("Smoothing speed", 2, 30, 12, 1);
	public static final Setting.Bool ZOOM_REMEMBER = ZOOM.bool("Remember scrolled zoom", false);

	public static final Module TOTEM_GLOW = reg(new Module("totem_glow", "Totem Glow", "Totems glow in your hotbar and inventory", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Color TG_COLOR = TOTEM_GLOW.color("Glow color", 0x39FF14);
	public static final Setting.Bool TG_PULSE = TOTEM_GLOW.bool("Pulse", true);
	public static final Setting.Bool TG_HOTBAR = TOTEM_GLOW.bool("Glow in hotbar", true);
	public static final Setting.Bool TG_INV = TOTEM_GLOW.bool("Glow in inventory", true);

	public static final Module TOTEM_POP = reg(new Module("totem_pop", "Small Totem Pop", "Shrinks the totem pop animation", Category.VISUALS, Safety.PUBLIC_SAFE, true));
	public static final Setting.Num TP_SCALE = TOTEM_POP.num("Scale", 0.1, 1.0, 0.45, 0.05);
	public static final Setting.Bool TP_HIDE = TOTEM_POP.bool("Hide completely", false);

	public static final Module ESP = reg(new Module("entity_esp", "Entity ESP", "Highlights entities through walls (restricted)", Category.VISUALS, Safety.RESTRICTED, false));
	public static final Setting.Bool ESP_PLAYERS = ESP.bool("Players", true);
	public static final Setting.Bool ESP_HOSTILE = ESP.bool("Hostile mobs", true);
	public static final Setting.Bool ESP_PASSIVE = ESP.bool("Passive mobs", false);
	public static final Setting.Color ESP_C_PLAYERS = ESP.color("Player color", 0x39FF14);
	public static final Setting.Color ESP_C_HOSTILE = ESP.color("Hostile color", 0xFF3B30);
	public static final Setting.Color ESP_C_PASSIVE = ESP.color("Passive color", 0x4FC3FF);
	public static final Setting.Num ESP_LINE = ESP.num("Outline width", 1, 4, 1, 1);
	public static final Setting.Num ESP_RANGE = ESP.num("Range", 8, 128, 64, 1);
	public static final Setting.Bool ESP_TRACERS = ESP.bool("Tracers", true);

	// =====================================================================
	// PLAYER
	// =====================================================================
	public static final Module NO_BOB = reg(new Module("no_bob", "No View Bobbing", "Keeps view bobbing off", Category.PLAYER, Safety.PUBLIC_SAFE, true));

	public static final Module SLOW_SWING = reg(new Module("slow_swing", "Slow Swing", "Swings your hand slower (visual only)", Category.PLAYER, Safety.PUBLIC_SAFE, false));
	public static final Setting.Num SWING_MULT = SLOW_SWING.num("Slowdown (x)", 1.0, 8.0, 2.0, 0.25);

	public static final Module SPRINT = reg(new Module("auto_sprint", "Auto Sprint", "Sprints while you walk forward", Category.PLAYER, Safety.PUBLIC_SAFE, false));

	// =====================================================================
	// COMBAT / MOVEMENT (restricted)
	// =====================================================================
	public static final Module AUTO_TOTEM = reg(new Module("auto_totem", "Auto Totem", "Moves a totem into your offhand (restricted)", Category.COMBAT, Safety.RESTRICTED, false));
	public static final Setting.Num AT_HEALTH = AUTO_TOTEM.num("Health threshold (0 = always)", 0, 20, 0, 1);
	public static final Setting.Num AT_SLOT = AUTO_TOTEM.num("Preferred slot (0 = any)", 0, 36, 0, 1);
	public static final Setting.Num AT_DELAY = AUTO_TOTEM.num("Delay (ticks)", 0, 20, 2, 1);

	public static final Module FLIGHT = reg(new Module("flight", "Flight", "Fly freely (restricted)", Category.MOVEMENT, Safety.RESTRICTED, false));
	public static final Setting.Num FLIGHT_SPEED = FLIGHT.num("Horizontal speed", 0.1, 3.0, 0.6, 0.05);
	public static final Setting.Num FLIGHT_VSPEED = FLIGHT.num("Vertical speed", 0.1, 3.0, 0.5, 0.05);

	public static final Module ELYTRA_BOOST = reg(new Module("elytra_boost", "Elytra Boost", "Extra thrust while gliding (restricted)", Category.MOVEMENT, Safety.RESTRICTED, false));
	public static final Setting.Key EB_KEY = ELYTRA_BOOST.keybind("Boost key (hold)", GLFW.GLFW_KEY_LEFT_ALT);
	public static final Setting.Num EB_POWER = ELYTRA_BOOST.num("Thrust", 0.01, 0.2, 0.04, 0.01);
	public static final Setting.Num EB_MAX = ELYTRA_BOOST.num("Max speed", 0.5, 4.0, 2.0, 0.1);

	// =====================================================================
	// HUD
	// =====================================================================
	public static final Module ARMOR = reg(new Module("armor_hud", "Armor HUD", "Armor and durability next to the hotbar", Category.HUD, Safety.PUBLIC_SAFE, true));
	public static final HudPos ARMOR_POS = new HudPos(ARMOR, 4, 108, -2, 1.0);
	public static final Setting.Choice ARMOR_ORIENT = ARMOR.choice("Orientation", 0, "Horizontal", "Vertical");
	public static final Setting.Num ARMOR_SPACING = ARMOR.num("Spacing", 0, 16, 2, 1);
	public static final Setting.Choice ARMOR_DUR = ARMOR.choice("Durability", 1, "None", "Bar", "Number", "Percent");
	public static final Setting.Bool ARMOR_BG = ARMOR.bool("Background", false);
	public static final Setting.Num ARMOR_BG_OP = ARMOR.num("Background opacity %", 0, 100, 40, 5);

	public static final Module TOTEM_COUNT = reg(new Module("totem_counter", "Totem Counter", "Shows how many totems you carry", Category.HUD, Safety.PUBLIC_SAFE, true));
	public static final HudPos TOTEM_POS = new HudPos(TOTEM_COUNT, 4, -108, -2, 1.0);
	public static final Setting.Color TOTEM_TEXT = TOTEM_COUNT.color("Text color", 0xFFFFFF);
	public static final Setting.Bool TOTEM_BG = TOTEM_COUNT.bool("Background", true);
	public static final Setting.Bool TOTEM_HIDE0 = TOTEM_COUNT.bool("Hide when zero", false);

	public static final Module INFO = reg(new Module("info_hud", "Info Display", "Coordinates, direction and FPS", Category.HUD, Safety.PUBLIC_SAFE, false));
	public static final HudPos INFO_POS = new HudPos(INFO, 0, 4, 4, 1.0);
	public static final Setting.Color INFO_COLOR = INFO.color("Text color", 0xFFFFFF);
	public static final Setting.Bool INFO_COORDS = INFO.bool("Coordinates", true);
	public static final Setting.Bool INFO_DIR = INFO.bool("Direction", true);
	public static final Setting.Bool INFO_FPS = INFO.bool("FPS", true);

	public static final Module ONLINE = reg(new Module("online_hub", "Player Online Hub", "Shows watched players who are on your server", Category.HUD, Safety.PUBLIC_SAFE, false));
	public static final HudPos ONLINE_POS = new HudPos(ONLINE, 2, -4, 4, 0.9);
	public static final Setting.Color ONLINE_COLOR = ONLINE.color("Name color", 0x39FF14);
	public static final Setting.Color ONLINE_TITLE = ONLINE.color("Title color", 0xAAAAAA);
	public static final Setting.Bool ONLINE_EMPTY = ONLINE.bool("Show when nobody is online", false);
	public static final Setting.Items ONLINE_LIST = ONLINE.items("Watch list", true);

	public static final Module HUD_COLORS = reg(new Module("hud_colors", "HUD Colors", "Custom colors for hearts, hunger, XP and inventory", Category.HUD, Safety.PUBLIC_SAFE, false));
	public static final Setting.Color HC_HEART = HUD_COLORS.color("Heart color", 0xE53935);
	public static final Setting.Color HC_HEART_BG = HUD_COLORS.color("Heart background", 0x3A1414);
	public static final Setting.Color HC_ABSORB = HUD_COLORS.color("Absorption color", 0xFFD60A);
	public static final Setting.Color HC_FOOD = HUD_COLORS.color("Hunger color", 0xD2955A);
	public static final Setting.Color HC_FOOD_BG = HUD_COLORS.color("Hunger background", 0x2B2014);
	public static final Setting.Bool HC_XP = HUD_COLORS.bool("Replace XP bar", true);
	public static final Setting.Color HC_XP_COLOR = HUD_COLORS.color("XP bar color", 0x7CFC00);
	public static final Setting.Color HC_XP_BG = HUD_COLORS.color("XP bar background", 0x1E1E1E);
	public static final Setting.Color HC_LEVEL = HUD_COLORS.color("Level number color", 0x80FF20);
	public static final Setting.Color HC_INV = HUD_COLORS.color("Inventory tint color", 0x39FF14);
	public static final Setting.Num HC_INV_OP = HUD_COLORS.num("Inventory tint opacity %", 0, 60, 0, 1);

	// =====================================================================
	// UTILITY
	// =====================================================================
	public static final Module SORT = reg(new Module("inv_sort", "Sort Inventory", "Sorts and merges your inventory", Category.UTILITY, Safety.PUBLIC_SAFE, true));
	public static final Setting.Key SORT_KEY = SORT.keybind("Sort key", GLFW.GLFW_KEY_R);
	public static final Setting.Bool SORT_CTRL = SORT.bool("Hold Ctrl with key", true);
	public static final Setting.Bool SORT_BUTTON = SORT.bool("Show Sort button", true);
	public static final Setting.Choice SORT_MODE = SORT.choice("Sort by", 0, "Item type", "Alphabetical", "Stack size", "Custom priority");
	public static final Setting.Bool SORT_MERGE = SORT.bool("Merge stacks", true);
	public static final Setting.Bool SORT_CHEST = SORT.bool("Sort chests too", true);
	public static final Setting.Items SORT_PRIORITY = SORT.items("Custom priority (words in item id)", false, "sword", "pickaxe", "axe", "bow", "food", "golden_apple", "totem");

	public static final Module REFILL = reg(new Module("hotbar_refill", "Refill Hotbar", "Refills empty hotbar slots from the inventory", Category.UTILITY, Safety.PUBLIC_SAFE, true));
	public static final Setting.Bool[] REFILL_SLOTS = new Setting.Bool[9];
	static {
		for (int i = 0; i < 9; i++) REFILL_SLOTS[i] = REFILL.bool("Refill slot " + (i + 1), true);
	}

	public static final Module SHULKER = reg(new Module("shulker_preview", "Shulker Preview", "Hover a shulker box to see what is inside", Category.UTILITY, Safety.PUBLIC_SAFE, true));
	public static final Setting.Choice SH_ANCHOR = SHULKER.choice("Position", 0, "Follow mouse", "Top Left", "Top Right", "Bottom Left", "Bottom Right");
	public static final Setting.Num SH_OX = SHULKER.num("Offset X", -300, 300, 0, 1);
	public static final Setting.Num SH_OY = SHULKER.num("Offset Y", -300, 300, 0, 1);
	public static final Setting.Num SH_SCALE = SHULKER.num("Scale", 0.5, 2.0, 1.0, 0.05);
	public static final Setting.Bool SH_COUNTS = SHULKER.bool("Show counts", true);

	public static final Module DISCORD = reg(new Module("discord_rpc", "Discord Rich Presence", "Shows Kelp Client on your Discord profile", Category.UTILITY, Safety.PUBLIC_SAFE, false));
	public static final Setting.Text DC_APP_ID = DISCORD.text("Application ID", "");
	public static final Setting.Bool DC_VERSION = DISCORD.bool("Show Minecraft version", true);
	public static final Setting.Bool DC_WORLD = DISCORD.bool("Show Singleplayer / Multiplayer", true);
	public static final Setting.Bool DC_SERVER = DISCORD.bool("Show server address (private!)", false);

	// =====================================================================
	// MACROS
	// =====================================================================
	public static final Module MACROS = reg(new Module("macros", "Macros", "Keys that send chat messages and commands", Category.MACRO, Safety.PUBLIC_SAFE, true));

	// =====================================================================
	// SETTINGS
	// =====================================================================
	public static final Module GUI = reg(new Module("gui", "Appearance", "Colors, size and shape of the Kelp Client menu", Category.SETTINGS, Safety.PUBLIC_SAFE, true).alwaysOn());
	public static final Setting.Color GUI_ACCENT = GUI.color("Accent color", 0x39FF14);
	public static final Setting.Color GUI_BG = GUI.color("Background color", 0x111418);
	public static final Setting.Color GUI_TEXT = GUI.color("Text color", 0xE8EAED);
	public static final Setting.Num GUI_OPACITY = GUI.num("Background opacity %", 40, 100, 94, 1);
	public static final Setting.Num GUI_RADIUS = GUI.num("Corner radius", 0, 10, 6, 1);
	public static final Setting.Num GUI_SCALE = GUI.num("Menu scale", 0.6, 1.5, 1.0, 0.05);
	public static final Setting.Bool GUI_ANIM = GUI.bool("Animations", true);

	public static final Module SAFETY = reg(new Module("server_safety", "Server Safety", "Controls where restricted modules may run", Category.SETTINGS, Safety.PUBLIC_SAFE, true).alwaysOn());
	public static final Setting.Items ALLOWED_SERVERS = SAFETY.items("Allowed servers (host or host:port)", true);
	public static final Setting.Bool ALLOW_LAN = SAFETY.bool("Allow restricted modules on LAN", true);
	public static final Setting.Bool RESTORE_AFTER_LOCK = SAFETY.bool("Restore modules after a locked server", false);
}
