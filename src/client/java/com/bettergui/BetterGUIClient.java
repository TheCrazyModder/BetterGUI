package com.bettergui;

import com.bettergui.screens.CustomMainMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.TitleScreen;


public class BetterGUIClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        AssetLoader.init();
        ClientTickEvents.END_CLIENT_TICK.register((t) -> {
            if (t.currentScreen instanceof TitleScreen) {
                t.setScreen(new CustomMainMenuScreen());
            }
        });
	}
}