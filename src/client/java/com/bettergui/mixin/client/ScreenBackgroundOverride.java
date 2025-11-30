package com.bettergui.mixin.client;

import com.bettergui.AssetLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public class ScreenBackgroundOverride {
    @Shadow
    @Nullable
    protected MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", cancellable = true)
    private void renderBackgroundOverride(DrawContext context, int mouseX, int mouseY, float tickDelta, CallbackInfo info) {

        Screen screen = (Screen) (Object) this;

        if (client.world != null) {
            return;
        }

        AssetLoader.AssetImage background = AssetLoader.STATIC_BACKGROUND;

        int screenW = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenH = MinecraftClient.getInstance().getWindow().getScaledHeight();

        int imgWidth = background.getWidth();
        int imgHeight = background.getHeight();

        float imgAspect = (float) imgWidth / imgHeight;
        float screenAspect = (float) screenW / screenH;

        int drawW, drawH;
        if (screenAspect > imgAspect) {
            // screen is wider than image — scale based on width
            drawW = screenW;
            drawH = (int) (screenW / imgAspect);
        } else {
            // screen is taller (or narrower) than image — scale based on height
            drawH = screenH;
            drawW = (int) (screenH * imgAspect);
        }

        // compute offsets so image is centered (cropping edges if needed)
        int x = (screenW - drawW) / 2;
        int y = (screenH - drawH) / 2;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, background.getIdentifier(), x, y, 1.0f, 1.0f, drawW, drawH, drawW, drawH);

        info.cancel();
    }
}
