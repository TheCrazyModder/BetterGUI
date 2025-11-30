package com.bettergui.mixin.client;

import com.bettergui.screens.CustomMainMenuScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class ScreenSetMixin {
	@Inject(at = @At("HEAD"), method = "setScreen")
	private void init(CallbackInfo info) {
	}

    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
            )
    )

    private void injected(MinecraftClient client, Screen screen) {
        if (screen instanceof TitleScreen) {
            MinecraftClient.getInstance().setScreen(new CustomMainMenuScreen());
        } else {
            MinecraftClient.getInstance().setScreen(screen);
        }
    }
}