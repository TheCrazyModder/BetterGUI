package com.bettergui.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class CustomWorldSelectScreen extends Screen {

    Screen parent;

    private WorldListWidget levelList;

    protected CustomWorldSelectScreen(Screen parent) {
        super(Text.of("World Select"));
        this.parent = parent;
    }

    @Override
    public void init() {
        ArrayList<Drawable> elements = new ArrayList<>();

        int rightButtonSectionWidth = ScreenHelper.BUTTON_WIDTH + (ScreenHelper.BUTTON_PADDING * 2);

        Consumer<WorldListWidget.WorldEntry> consumer = WorldListWidget.WorldEntry::play;

        this.levelList = new WorldListWidget.Builder(this.client, this)
                .width(this.width - rightButtonSectionWidth)
                .height(this.height)
                .predecessor(this.levelList)
                .selectionCallback(this::worldSelected)
                .confirmationCallback(consumer)
                .toWidget();

        elements.add(this.levelList);

        ScreenHelper.ButtonInfo[] rightList = new ScreenHelper.ButtonInfo[]{
                new ScreenHelper.ButtonInfo(
                        "Play",
                        (btn) -> {}
                ),
                new ScreenHelper.ButtonInfo(
                        "Edit",
                        (btn) -> {}
                ),
                new ScreenHelper.ButtonInfo(
                        "Delete",
                        (btn) -> {}
                )
        };

        ScreenHelper.ButtonInfo[] mainList = new ScreenHelper.ButtonInfo[]{
                new ScreenHelper.ButtonInfo(
                        "Create New World",
                        (btn) -> {
                            CreateWorldScreen.show(this.client, levelList::refresh);
                        },
                        new ScreenHelper.ScreenPos(
                            ScreenHelper.getRightOffset(ScreenHelper.BUTTON_WIDTH+ScreenHelper.BUTTON_PADDING),
                                this.height - ScreenHelper.BUTTON_PADDING-ScreenHelper.BUTTON_HEIGHT
                        )
                )
        };

        elements.addAll(ScreenHelper.buildRightSideList(rightList));
        elements.addAll(ScreenHelper.buildList(mainList));

        for (Drawable drawable : elements) {
            this.addDrawable(drawable);
        }
    }

    private void worldSelected(@Nullable LevelSummary levelSummary) {
        if (levelSummary == null) {return;}

        System.out.println(levelSummary.getName());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
