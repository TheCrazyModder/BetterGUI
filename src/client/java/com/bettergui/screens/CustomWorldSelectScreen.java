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

    @Nullable
    private ButtonWidget selectButton;

    ArrayList<Drawable> elements = new ArrayList<>();

    int rightButtonSectionWidth = ScreenHelper.BUTTON_WIDTH + (ScreenHelper.BUTTON_PADDING * 2);

    protected CustomWorldSelectScreen(Screen parent) {
        super(Text.of("World Select"));
        this.parent = parent;
    }

    @Override
    public void init() {
        elements.clear();

        Consumer<WorldListWidget.WorldEntry> consumer = WorldListWidget.WorldEntry::play;

        this.levelList = new WorldListWidget.Builder(this.client, this)
                .width(this.width - rightButtonSectionWidth)
                .height(this.height)
                .predecessor(this.levelList)
                .selectionCallback(this::worldSelected)
                .confirmationCallback(consumer)
                .toWidget();

        elements.add(this.levelList);

        addButtons(consumer, this.levelList);


        for (Drawable drawable : elements) {
            this.addDrawable(drawable);
        }

        this.refreshWidgetPositions();
        this.worldSelected(null);
    }

    private void addButtons(Consumer<WorldListWidget.WorldEntry> playAction, WorldListWidget levelList) {
        ScreenHelper.ButtonInfo[] mainList = new ScreenHelper.ButtonInfo[]{
                new ScreenHelper.ButtonInfo(
                        "Create New World",
                        (btn) -> CreateWorldScreen.show(this.client, levelList::refresh),
                        new ScreenHelper.ScreenPos(
                                ScreenHelper.getRightOffset(ScreenHelper.BUTTON_WIDTH+ScreenHelper.BUTTON_PADDING),
                                this.height - ScreenHelper.BUTTON_PADDING-ScreenHelper.BUTTON_HEIGHT
                        )
                )
        };

        this.selectButton = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        LevelSummary.SELECT_WORLD_TEXT.getString(),
                        (button) -> {
                            levelList.getSelectedAsOptional().ifPresent(playAction);
                        }
                )
        ).build();

        elements.addAll(ScreenHelper.buildList(mainList));
        elements.add(this.selectButton);

        ScreenHelper.ButtonInfo[] rightList = new ScreenHelper.ButtonInfo[]{
                new ScreenHelper.ButtonInfo(
                        "Play",
                        (btn) -> {levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::play);}
                ),
                new ScreenHelper.ButtonInfo(
                        "Edit",
                        (btn) -> {levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::edit);}
                ),
                new ScreenHelper.ButtonInfo(
                        "Delete",
                        (btn) -> {levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::deleteIfConfirmed);}
                )
        };

        elements.addAll(ScreenHelper.buildRightSideList(rightList));
    }

    private void worldSelected(@Nullable LevelSummary levelSummary) {
        if (levelSummary == null) {return;}

        this.selectButton.setMessage(levelSummary.getSelectWorldText());

        System.out.println(levelSummary.getName());
    }

    @Override
    protected void refreshWidgetPositions() {
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
