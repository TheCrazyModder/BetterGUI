package com.bettergui;

import net.minecraft.util.Identifier;

public class AssetLoader {

    public static class AssetImage {
        Identifier identifier;
        int width;
        int height;

        public AssetImage(Identifier identifier, int width, int height) {
            this.identifier = identifier;
            this.width = width;
            this.height = height;
        }

        public Identifier getIdentifier() {
            return this.identifier;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }
    }

    public static final AssetImage STATIC_BACKGROUND = new AssetImage(Identifier.of(BetterGUI.MOD_ID, "textures/gui/background.png"), 3840, 2160);

    public static void init() {}
}
