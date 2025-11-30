package com.bettergui.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class CustomWorldSelectScreen extends Screen {

    Screen parent;

    protected CustomWorldSelectScreen(Screen parent) {
        super(Text.of("World Select"));
        this.parent = parent;
    }

    @Override
    public void init() {

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {

    }
}
