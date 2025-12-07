package com.bettergui.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class ScreenHelper {

    public static class ScreenPos {
        public int x;
        public int y;

        public ScreenPos() {}
        public ScreenPos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class ButtonInfo {
        public String text;
        public ButtonWidget.PressAction action;
        public ScreenPos pos;

        public ButtonInfo(String text, ButtonWidget.PressAction action, ScreenPos pos) {
            this.text = text;
            this.action = action;
            this.pos = pos;
        }

        public ButtonInfo(String text, ButtonWidget.PressAction action) {
            this.text = text;
            this.action = action;
            this.pos = new ScreenPos();
        }
    }

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_PADDING = 10;
    public static final int BUTTON_WIDTH = 150;

    public static int getWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }

    public static int getRightOffset(int offsetx) {
        return getWidth() - offsetx;
    }

    public static int getCenterOffset(int offsety) {
        return (getHeight() / 2) + offsety;
    }

    public static int getXlistOffset(int index) {
        int y = getCenterOffset((BUTTON_HEIGHT + BUTTON_PADDING)*index);
        //System.out.println("Index " + index + " Y " + y + " Height " + getHeight() + " ButtonH " + (BUTTON_HEIGHT+BUTTON_PADDING));
        return y;
    }

    public static ScreenPos getScreenRightListPos(int index) {
        return new ScreenPos(
                getRightOffset(
                        BUTTON_WIDTH+BUTTON_PADDING
                ),
                getXlistOffset(index)
        );
    }

    public static ButtonWidget.Builder build(ButtonInfo info) {

        return ButtonWidget.builder(
                        Text.of(info.text),
                        info.action
                )
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .position(info.pos.x, info.pos.y);
    }

    public static ArrayList<ButtonWidget> buildList(ButtonInfo[] infoList) {

        ArrayList<ButtonWidget> widgets = new ArrayList<>();

        for (ButtonInfo info : infoList) {
            widgets.add(build(info).build());
        }

        return widgets;
    }

    public static ArrayList<ButtonWidget> buildRightSideList(ButtonInfo[] infoList) {
        int i = 0;
        for (ButtonInfo info : infoList) {
            info.pos = getScreenRightListPos(i);
            i++;
        }

        return buildList(infoList);
    }
}
