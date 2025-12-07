package com.bettergui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.*;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.text.Text;
import net.minecraft.util.path.PathUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CustomWorldSelectScreen extends Screen{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final GeneratorOptions DEBUG_GENERATOR_OPTIONS = new GeneratorOptions("test1".hashCode(), true, false);
    protected final Screen parent;
    @Nullable
    private ButtonWidget deleteButton;
    @Nullable
    private ButtonWidget selectButton;
    @Nullable
    private ButtonWidget editButton;
    @Nullable
    private ButtonWidget recreateButton;
    @Nullable
    private ButtonWidget createButton;
    @Nullable
    protected TextFieldWidget searchBox;
    @Nullable
    private WorldListWidget levelList;

    Consumer<WorldListWidget.WorldEntry> playAction = WorldListWidget.WorldEntry::play;

    int rightButtonSectionWidth = ScreenHelper.BUTTON_WIDTH + (ScreenHelper.BUTTON_PADDING * 2);

    AtomicInteger listOffset;

    public CustomWorldSelectScreen(Screen parent) {
        super(Text.translatable("selectWorld.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        this.listOffset = new AtomicInteger(-2);

        this.clearChildren();

        this.searchBox = new TextFieldWidget(
                this.textRenderer,
                ScreenHelper.getRightOffset(ScreenHelper.BUTTON_WIDTH+ScreenHelper.BUTTON_PADDING),
                ScreenHelper.BUTTON_PADDING,
                ScreenHelper.BUTTON_WIDTH,
                ScreenHelper.BUTTON_HEIGHT,
                Text.translatable("selectWorld.search")
        );

        this.searchBox.setChangedListener(search -> {
            if (this.levelList != null) {
                this.levelList.setSearch(search);
            }
        });

        this.levelList = new WorldListWidget.Builder(this.client, this)
                                .width(this.width - rightButtonSectionWidth)
                                .height(this.height)
                                .search(this.searchBox.getText())
                                .predecessor(this.levelList)
                                .selectionCallback(this::worldSelected)
                                .confirmationCallback(playAction)
                                .toWidget();

        this.addButtons();

        this.addDrawableChild(deleteButton);
        this.addDrawableChild(selectButton);
        this.addDrawableChild(editButton);
        this.addDrawableChild(recreateButton);
        this.addDrawableChild(searchBox);
        this.addDrawableChild(levelList);
        this.addDrawableChild(createButton);

        this.worldSelected(null);
    }

    private void addButtons() {
        WorldListWidget levelList = this.levelList;

        this.selectButton = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        LevelSummary.SELECT_WORLD_TEXT.getString(),
                        button -> levelList.getSelectedAsOptional().ifPresent(playAction),
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();

        this.editButton = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        "Edit",
                        button -> levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::edit),
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();

        this.deleteButton = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        "Delete",
                        button -> levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::deleteIfConfirmed),
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();

        this.recreateButton = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        "Recreate",
                        button -> levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::recreate),
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();

        this.createButton = ScreenHelper.build(
                        new ScreenHelper.ButtonInfo(
                                "Create New World",
                                button -> CreateWorldScreen.show(this.client, levelList::refresh),
                                new ScreenHelper.ScreenPos(
                                        ScreenHelper.getRightOffset(ScreenHelper.BUTTON_WIDTH+ScreenHelper.BUTTON_PADDING),
                                        this.height - (
                                                ScreenHelper.BUTTON_PADDING + ScreenHelper.BUTTON_HEIGHT
                                                )
                                )
                        )
                ).build();
    }

    private ButtonWidget createDebugRecreateButton() {
        return ButtonWidget.builder(
                        Text.literal("DEBUG recreate"),
                        button -> {
                            try {
                                String string = "DEBUG world";
                                if (this.levelList != null && !this.levelList.children().isEmpty()) {
                                    WorldListWidget.Entry entry = (WorldListWidget.Entry)this.levelList.children().getFirst();
                                    if (entry instanceof WorldListWidget.WorldEntry worldEntry && worldEntry.getLevelDisplayName().equals("DEBUG world")) {
                                        worldEntry.delete();
                                    }
                                }

                                LevelInfo levelInfo = new LevelInfo(
                                        "DEBUG world",
                                        GameMode.SPECTATOR,
                                        false,
                                        Difficulty.NORMAL,
                                        true,
                                        new GameRules(DataConfiguration.SAFE_MODE.enabledFeatures()),
                                        DataConfiguration.SAFE_MODE
                                );
                                String string2 = PathUtil.getNextUniqueName(this.client.getLevelStorage().getSavesDirectory(), "DEBUG world", "");
                                this.client.createIntegratedServerLoader().createAndStart(string2, levelInfo, DEBUG_GENERATOR_OPTIONS, WorldPresets::createDemoOptions, this);
                            } catch (IOException var5) {
                                LOGGER.error("Failed to recreate the debug world", (Throwable)var5);
                            }
                        }
                )
                .width(72)
                .build();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.init();
        //this.addButtons();
    }

    @Override
    protected void setInitialFocus() {
        if (this.searchBox != null) {
            this.setInitialFocus(this.searchBox);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    public void worldSelected(@Nullable LevelSummary levelSummary) {
        if (this.selectButton != null && this.editButton != null && this.recreateButton != null && this.deleteButton != null) {
            if (levelSummary == null) {
                this.selectButton.setMessage(LevelSummary.SELECT_WORLD_TEXT);
                this.selectButton.active = false;
                this.editButton.active = false;
                this.recreateButton.active = false;
                this.deleteButton.active = false;
            } else {
                this.selectButton.setMessage(levelSummary.getSelectWorldText());
                this.selectButton.active = levelSummary.isSelectable();
                this.editButton.active = levelSummary.isEditable();
                this.recreateButton.active = levelSummary.isRecreatable();
                this.deleteButton.active = levelSummary.isDeletable();
            }
        }
    }

    @Override
    public void removed() {
        if (this.levelList != null) {
            this.levelList.children().forEach(WorldListWidget.Entry::close);
        }
    }
}
