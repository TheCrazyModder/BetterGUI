package com.bettergui.screens;

import com.bettergui.AssetLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class CustomMainMenuScreen extends Screen {

    private final LogoDrawer logoDrawer;

    public CustomMainMenuScreen() {
        super(Text.of("CustomMainMenuScreen"));
        this.logoDrawer = new LogoDrawer(true);
    }

    @Override
    protected void init() {

        int rightOffset = ScreenHelper.getRightOffset(ScreenHelper.BUTTON_WIDTH+ScreenHelper.BUTTON_PADDING);

        ScreenHelper.ButtonInfo[] info = new ScreenHelper.ButtonInfo[]{
                new ScreenHelper.ButtonInfo(
                        "Singleplayer",
                        (btn) -> {
                            this.client.setScreen(new TempTestWoldScreen(this));
                        }
                ),
                new ScreenHelper.ButtonInfo(
                        "Multiplayer",
                        (btn) -> {
                            this.client.setScreen(new MultiplayerScreen(this));
                        }
                ),
                new ScreenHelper.ButtonInfo(
                        "Settings",
                        (btn) -> {
                            this.client.setScreen(new OptionsScreen(this, MinecraftClient.getInstance().options));
                        }
                ),
                new ScreenHelper.ButtonInfo(
                        "Quit",
                        (btn) -> {
                            this.client.scheduleStop();
                        }
                )
        };

        ArrayList<ButtonWidget> buttons = ScreenHelper.buildRightSideList(info);

        for (ButtonWidget button : buttons) {
            this.addDrawableChild(button);
        }

    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        this.logoDrawer.draw(context, this.width, 1.0f);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
