package com.lokro.flexhud.client.cmd;

import com.lokro.flexhud.client.bridge.StepcountBridge;
import com.lokro.flexhud.client.gui.FlexhudScreen;
import com.lokro.flexhud.client.gui.MarketplaceScreen;
import com.lokro.flexhud.client.gui.WidgetEditorScreen;
import com.lokro.flexhud.client.state.TimerState;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

/**
 * Client-Commands: {@code /flexhud} (Menü/Editor/Marketplace/Steuerung)
 * und {@code /timer} (Kurzform für den Timer).
 */
public final class FlexhudCommands {
	private FlexhudCommands() {}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register(FlexhudCommands::registerCommands);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> d, CommandBuildContext ctx) {
		d.register(ClientCommands.literal("flexhud")
			.executes(c -> {
				open(new FlexhudScreen(null));
				return 1;
			})
			.then(ClientCommands.literal("editor").executes(c -> {
				open(new WidgetEditorScreen(null));
				return 1;
			}))
			.then(ClientCommands.literal("market").executes(c -> {
				open(new MarketplaceScreen(null));
				return 1;
			}))
			.then(ClientCommands.literal("timer")
				.executes(c -> {
					feedback(c.getSource(), "Timer: " + TimerState.format(TimerState.getElapsedMs(), true)
						+ (TimerState.isRunning() ? " (läuft)" : " (pausiert)"));
					return 1;
				})
				.then(ClientCommands.literal("start").executes(c -> {
					TimerState.start();
					feedback(c.getSource(), "Timer gestartet.");
					return 1;
				}))
				.then(ClientCommands.literal("stop").executes(c -> {
					TimerState.pause();
					feedback(c.getSource(), "Timer pausiert bei " + TimerState.format(TimerState.getElapsedMs(), true) + ".");
					return 1;
				}))
				.then(ClientCommands.literal("reset").executes(c -> {
					TimerState.reset();
					feedback(c.getSource(), "Timer zurückgesetzt.");
					return 1;
				}))
				.then(ClientCommands.literal("show").executes(c -> {
					boolean v = TimerState.toggleShow();
					feedback(c.getSource(), v ? "Timer-Anzeige EIN." : "Timer-Anzeige AUS.");
					return 1;
				})))
			.then(ClientCommands.literal("stepcount")
				.then(ClientCommands.literal("start").executes(c -> {
					feedback(c.getSource(), "[FlexHUD→StepCount] " + StepcountBridge.command("start"));
					return 1;
				}))
				.then(ClientCommands.literal("stop").executes(c -> {
					feedback(c.getSource(), "[FlexHUD→StepCount] " + StepcountBridge.command("stop"));
					return 1;
				}))
				.then(ClientCommands.literal("reset").executes(c -> {
					feedback(c.getSource(), "[FlexHUD→StepCount] " + StepcountBridge.command("reset"));
					return 1;
				}))
				.then(ClientCommands.literal("show").executes(c -> {
					feedback(c.getSource(), "[FlexHUD→StepCount] " + StepcountBridge.command("show"));
					feedback(c.getSource(), "Tipp: Nutze das FlexHUD-STEPCOUNT-Widget für frei positionierbare Anzeige.");
					return 1;
				})))
		);

		d.register(ClientCommands.literal("timer")
			.executes(c -> {
				TimerState.toggle();
				feedback(c.getSource(), TimerState.isRunning() ? "Timer gestartet."
					: "Timer pausiert bei " + TimerState.format(TimerState.getElapsedMs(), true) + ".");
				return 1;
			})
			.then(ClientCommands.literal("start").executes(c -> {
				TimerState.start();
				feedback(c.getSource(), "Timer gestartet.");
				return 1;
			}))
			.then(ClientCommands.literal("stop").executes(c -> {
				TimerState.pause();
				feedback(c.getSource(), "Timer pausiert.");
				return 1;
			}))
			.then(ClientCommands.literal("reset").executes(c -> {
				TimerState.reset();
				feedback(c.getSource(), "Timer zurückgesetzt.");
				return 1;
			})));
	}

	private static void open(net.minecraft.client.gui.screens.Screen screen) {
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			// Client-Commands laufen bereits auf dem Client-Thread (26.3: kein execute() mehr nötig).
			client.gui.setScreen(screen);
		}
	}

	private static void feedback(FabricClientCommandSource src, String msg) {
		src.sendFeedback(Component.literal("§b[FlexHUD] §f" + msg));
	}
}
