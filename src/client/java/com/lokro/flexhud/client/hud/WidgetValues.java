package com.lokro.flexhud.client.hud;

import com.lokro.flexhud.client.config.WidgetType;
import com.lokro.flexhud.client.i18n.Lang;
import com.lokro.flexhud.client.state.TimerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Werte-Lieferanten für datengetriebene Widgets (Position, Ping, Biome …)
 * plus Tick-Tracker (CPS, Speed, Reach, TPS). Alles defensiv: ohne Welt oder
 * bei API-Abweichungen kommt null/"–" statt Crash.
 */
public final class WidgetValues {
	private WidgetValues() {}

	private static final Deque<Long> clicks = new ArrayDeque<>();
	private static boolean atkDown = false;
	private static boolean useDown = false;
	private static String lastReach = "–";

	private static double lastX = 0;
	private static double lastZ = 0;
	private static long lastMoveMs = 0;
	private static double speedEma = 0;
	private static boolean hasSpeed = false;

	private static long lastGameTime = -1;
	private static long lastTpsMs = 0;
	private static double tpsEma = 20.0;

	private static final long SESSION_START = System.currentTimeMillis();

	/** Jeden Client-Tick aufrufen (Tracker füttern). */
	public static void tick(Minecraft client) {
		long now = System.currentTimeMillis();
		while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) {
			clicks.pollFirst();
		}
		if (client == null || client.player == null || client.options == null) {
			atkDown = false;
			useDown = false;
			return;
		}
		boolean atk = client.options.keyAttack.isDown();
		boolean use = client.options.keyUse.isDown();
		if ((atk && !atkDown) || (use && !useDown)) {
			clicks.addLast(now);
		}
		if (atk && !atkDown && client.crosshairPickEntity != null) {
			try {
				Entity target = client.crosshairPickEntity;
				double dx = target.getX() - client.player.getX();
				double dy = target.getY() - client.player.getY();
				double dz = target.getZ() - client.player.getZ();
				lastReach = String.format(Locale.ROOT, "%.1f", Math.sqrt(dx * dx + dy * dy + dz * dz));
			} catch (Exception ignored) {
			}
		}
		atkDown = atk;
		useDown = use;

		try {
			double x = client.player.getX();
			double z = client.player.getZ();
			if (!hasSpeed) {
				lastX = x;
				lastZ = z;
				lastMoveMs = now;
				hasSpeed = true;
			} else if (now > lastMoveMs) {
				double dt = (now - lastMoveMs) / 1000.0;
				double dist = Math.sqrt((x - lastX) * (x - lastX) + (z - lastZ) * (z - lastZ));
				double inst = dist / dt;
				speedEma = speedEma == 0 ? inst : speedEma * 0.85 + inst * 0.15;
				lastX = x;
				lastZ = z;
				lastMoveMs = now;
			}
		} catch (Exception ignored) {
		}

		try {
			if (client.level == null) {
				lastGameTime = -1;
			} else {
				long gt = client.level.getGameTime();
				if (lastGameTime < 0) {
					lastGameTime = gt;
					lastTpsMs = now;
				} else if (now - lastTpsMs >= 500) {
					double inst = (gt - lastGameTime) * 1000.0 / (now - lastTpsMs);
					if (inst >= 0 && inst <= 40) {
						tpsEma = tpsEma == 0 ? inst : tpsEma * 0.9 + inst * 0.1;
					}
					lastGameTime = gt;
					lastTpsMs = now;
				}
			}
		} catch (Exception ignored) {
		}
	}

	/** Tracker bei Weltwechsel zurücksetzen (keine Altlasten). */
	public static void onWorldChange() {
		hasSpeed = false;
		speedEma = 0;
		lastGameTime = -1;
	}

	/** Live-Wert fürs HUD (null = derzeit nicht verfügbar). */
	public static String valueFor(WidgetType type, Minecraft client) {
		if (client == null || client.player == null || client.level == null) {
			return null;
		}
		try {
			LocalPlayer p = client.player;
			return switch (type) {
				case ARMOR -> armor(p);
				case CPS -> String.valueOf(clicks.size());
				case COORDS -> {
					BlockPos bp = p.blockPosition();
					yield bp.getX() + " " + bp.getY() + " " + bp.getZ();
				}
				case BIOME -> client.level.getBiome(p.blockPosition()).unwrapKey()
					.map(k -> k.identifier().getPath()).orElse("?");
				case NETHER -> nether(client);
				case COMPASS -> compass(p.getYRot());
				case PITCH -> String.format(Locale.ROOT, "%.0f°", -p.getXRot());
				case DAY -> String.valueOf(client.level.getGameTime() / 24000L);
				case PING -> ping(client);
				case SERVER -> client.getCurrentServer() != null
					? client.getCurrentServer().ip : Lang.t("server.sp");
				case MEMORY -> memory();
				case SPEED -> String.format(Locale.ROOT, "%.1f", speedEma);
				case PLAYTIME -> TimerState.format(System.currentTimeMillis() - SESSION_START, true);
				case WORLD_TIME -> worldTime(client.level.getGameTime());
				case HELD -> held(p);
				case LIGHT -> String.valueOf(
					client.level.getLightEngine().getRawBrightness(p.blockPosition(), 0));
				case DISTANCE -> distance(client);
				case POTIONS -> potions(p);
				case WEATHER -> client.level.isThundering() ? Lang.t("weather.storm")
					: client.level.isRaining() ? Lang.t("weather.rain") : Lang.t("weather.clear");
				case FULL_INV -> p.getInventory().getFreeSlot() == -1
					? Lang.t("full.full") : "OK";
				case KEYS -> keys(client);
				case SPRINT -> p.isSprinting() ? Lang.t("sprint.sprint")
					: (client.options.keyShift.isDown() ? Lang.t("sprint.sneak") : "–");
				case ENTITY_COUNT -> String.valueOf(nearby(client, 32, false));
				case REACH -> lastReach;
				case TPS -> String.format(Locale.ROOT, "%.1f", Math.min(99, Math.max(0, tpsEma)));
				case TNT -> tnt(client);
				default -> null;
			};
		} catch (Exception e) {
			return "–";
		}
	}

	/** Beispiel-Wert für Editor-Vorschau (ohne Welt). */
	public static String previewFor(WidgetType type) {
		return switch (type) {
			case ARMOR -> "82%";
			case CPS -> "7";
			case COORDS -> "128 64 -45";
			case BIOME -> "plains";
			case NETHER -> "16 -6";
			case COMPASS -> "NE";
			case PITCH -> "-12°";
			case DAY -> "42";
			case PING -> "23";
			case SERVER -> "play.example.net";
			case MEMORY -> "512/2048MB 25%";
			case SPEED -> "5.6";
			case PLAYTIME -> "12:34.5";
			case WORLD_TIME -> "13:37";
			case HELD -> "Diamond Sword";
			case LIGHT -> "12";
			case DISTANCE -> "4.5";
			case POTIONS -> "Speed 01:23";
			case WEATHER -> Lang.t("weather.clear");
			case FULL_INV -> "OK";
			case KEYS -> "[W] [Space]";
			case SPRINT -> Lang.t("sprint.sprint");
			case ENTITY_COUNT -> "17";
			case REACH -> "3.0";
			case TPS -> "20.0";
			case TNT -> "2.4s";
			default -> "?";
		};
	}

	private static String armor(LocalPlayer p) {
		int min = 101;
		boolean worn = false;
		for (EquipmentSlot slot : new EquipmentSlot[]{
				EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			try {
				ItemStack stack = p.getItemBySlot(slot);
				if (!stack.isEmpty() && stack.getMaxDamage() > 0) {
					worn = true;
					int pct = (stack.getMaxDamage() - stack.getDamageValue()) * 100 / stack.getMaxDamage();
					min = Math.min(min, pct);
				}
			} catch (Exception ignored) {
			}
		}
		return worn ? Math.max(0, min) + "%" : "–";
	}

	private static String nether(Minecraft client) {
		String dim = "";
		try {
			dim = client.level.dimension().identifier().getPath();
		} catch (Exception ignored) {
		}
		BlockPos bp = client.player.blockPosition();
		if (dim.contains("nether")) {
			return (bp.getX() * 8) + " " + (bp.getZ() * 8);
		}
		if (dim.contains("end")) {
			return "–";
		}
		return (bp.getX() / 8) + " " + (bp.getZ() / 8);
	}

	private static String compass(float yaw) {
		String[] dirs = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
		float norm = ((yaw % 360) + 360) % 360;
		return dirs[(int) (Math.floor(norm / 45 + 0.5)) % 8];
	}

	private static String ping(Minecraft client) {
		try {
			var conn = client.getConnection();
			if (conn == null) {
				return "–";
			}
			var info = conn.getPlayerInfo(client.player.getUUID());
			if (info == null) {
				return "–";
			}
			return String.valueOf(info.getLatency());
		} catch (Exception e) {
			return "–";
		}
	}

	private static String memory() {
		Runtime rt = Runtime.getRuntime();
		long max = rt.maxMemory() / 1048576L;
		long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
		long pct = max > 0 ? used * 100 / max : 0;
		return used + "/" + max + "MB " + pct + "%";
	}

	private static String worldTime(long gameTime) {
		long t = gameTime % 24000L;
		long h = (t / 1000 + 6) % 24;
		long m = (t % 1000) * 60 / 1000;
		return String.format(Locale.ROOT, "%02d:%02d", h, m);
	}

	private static String held(LocalPlayer p) {
		ItemStack stack = p.getMainHandItem();
		if (stack.isEmpty()) {
			return "–";
		}
		String name;
		try {
			name = stack.getHoverName().getString();
		} catch (Exception e) {
			name = "?";
		}
		return stack.getCount() > 1 ? name + " ×" + stack.getCount() : name;
	}

	private static String distance(Minecraft client) {
		try {
			if (client.hitResult == null) {
				return "–";
			}
			var loc = client.hitResult.getLocation();
			var pp = client.player.position();
			double dx = loc.x - pp.x;
			double dy = loc.y - pp.y;
			double dz = loc.z - pp.z;
			double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (client.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
				return "–";
			}
			return String.format(Locale.ROOT, "%.1f", d);
		} catch (Exception e) {
			return "–";
		}
	}

	private static String potions(LocalPlayer p) {
		List<String> parts = new ArrayList<>();
		for (MobEffectInstance inst : p.getActiveEffects()) {
			try {
				String name = net.minecraft.network.chat.Component.translatable(
					inst.getDescriptionId()).getString();
				if (inst.getAmplifier() > 0) {
					name += " " + roman(inst.getAmplifier() + 1);
				}
				if (!inst.isInfiniteDuration()) {
					int sec = inst.getDuration() / 20;
					name += String.format(Locale.ROOT, " %d:%02d", sec / 60, sec % 60);
				}
				parts.add(name);
			} catch (Exception ignored) {
			}
		}
		return parts.isEmpty() ? Lang.t("potions.none") : String.join(", ", parts);
	}

	private static String roman(int n) {
		return switch (n) {
			case 1 -> "I";
			case 2 -> "II";
			case 3 -> "III";
			case 4 -> "IV";
			case 5 -> "V";
			default -> String.valueOf(n);
		};
	}

	private static String keys(Minecraft client) {
		List<String> down = new ArrayList<>();
		var o = client.options;
		if (o.keyUp.isDown()) {
			down.add("[W]");
		}
		if (o.keyLeft.isDown()) {
			down.add("[A]");
		}
		if (o.keyDown.isDown()) {
			down.add("[S]");
		}
		if (o.keyRight.isDown()) {
			down.add("[D]");
		}
		if (o.keyJump.isDown()) {
			down.add("[Space]");
		}
		if (o.keyShift.isDown()) {
			down.add("[Shift]");
		}
		if (o.keyAttack.isDown()) {
			down.add("[LMB]");
		}
		if (o.keyUse.isDown()) {
			down.add("[RMB]");
		}
		return down.isEmpty() ? "–" : String.join(" ", down);
	}

	private static int nearby(Minecraft client, int radius, boolean tntOnly) {
		try {
			var p = client.player.position();
			AABB box = new AABB(p.x - radius, p.y - radius, p.z - radius,
				p.x + radius, p.y + radius, p.z + radius);
			var list = client.level.getEntities((Entity) null, box, e -> true);
			if (!tntOnly) {
				int n = 0;
				for (Entity e : list) {
					if (e != client.player && e.isAlive()) {
						n++;
					}
				}
				return n;
			}
			return list.size();
		} catch (Exception e) {
			return 0;
		}
	}

	private static String tnt(Minecraft client) {
		try {
			var p = client.player.position();
			int radius = 32;
			AABB box = new AABB(p.x - radius, p.y - radius, p.z - radius,
				p.x + radius, p.y + radius, p.z + radius);
			var list = client.level.getEntities((Entity) null, box, e -> e instanceof PrimedTnt);
			int best = Integer.MAX_VALUE;
			for (Entity e : list) {
				best = Math.min(best, ((PrimedTnt) e).getFuse());
			}
			if (best == Integer.MAX_VALUE) {
				return "–";
			}
			return String.format(Locale.ROOT, "%.1fs", best / 20.0);
		} catch (Exception e) {
			return "–";
		}
	}
}
