package com.bettergui.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.*;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMultiplayerScreen extends MultiplayerScreen {

    private static final Logger LOGGER = LogUtils.getLogger();
    protected MultiplayerServerListWidget serverListWidget;
    private ServerList serverList;
    private final MultiplayerServerListPinger serverListPinger = new MultiplayerServerListPinger();

    private ButtonWidget buttonAdd;
    private ButtonWidget buttonEdit;
    private ButtonWidget buttonJoin;
    private ButtonWidget buttonDelete;
    private ButtonWidget directConnect;
    private ButtonWidget refreshButton;

    private ServerInfo selectedEntry;
    private LanServerQueryManager.LanServerEntryList lanServers;
    private LanServerQueryManager.LanServerDetector lanServerDetector;

    private Screen parent;

    private AtomicInteger listOffset;

    public CustomMultiplayerScreen(Screen parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected void init() {

        listOffset = new AtomicInteger(-2);

        this.clearChildren();

        this.serverList = new ServerList(this.client);
        this.serverList.loadFile();
        this.lanServers = new LanServerQueryManager.LanServerEntryList();

        try {
            this.lanServerDetector = new LanServerQueryManager.LanServerDetector(this.lanServers);
            this.lanServerDetector.start();
        } catch (Exception var4) {
            LOGGER.warn("Unable to start LAN server detection: {}", var4.getMessage());
        }

        this.addButtons();

        ButtonWidget[] elements = new ButtonWidget[]{
                buttonAdd,
                directConnect,
                refreshButton,
                buttonJoin,
                buttonEdit,
                buttonDelete
        };

        for (ButtonWidget widget : elements) {
            this.addDrawableChild(widget);
        }

        this.serverListWidget = new MultiplayerServerListWidget(this, this.client, this.width - ScreenHelper.getFullButtonWidth()-ScreenHelper.BUTTON_PADDING, this.height, 0, 36);
        this.serverListWidget.setServers(this.serverList);

        this.addDrawableChild(serverListWidget);


        this.refreshWidgetPositions();
        this.updateButtonActivationStates();
    }


    protected void addButtons() {


        this.buttonJoin = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        "Join",
                        button -> {
                            MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
                            if (entry != null) {
                                entry.connect();
                            }},
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();


        this.buttonEdit = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        "Edit",
                        (button) -> {
                            MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
                            if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
                                ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry) entry).getServer();
                                this.selectedEntry = new ServerInfo(serverInfo.name, serverInfo.address, ServerInfo.ServerType.OTHER);
                                this.selectedEntry.copyWithSettingsFrom(serverInfo);
                                this.client.setScreen(new AddServerScreen(this, Text.of("Edit Server Info"), this::editEntry, this.selectedEntry));
                            }
                        },
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();

        this.buttonDelete = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        "Delete",
                        button -> {
                            MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
                            if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
                                String string = ((MultiplayerServerListWidget.ServerEntry)entry).getServer().name;
                                if (string != null) {
                                    Text text = Text.translatable("selectServer.deleteQuestion");
                                    Text text2 = Text.translatable("selectServer.deleteWarning", new Object[]{string});
                                    Text text3 = Text.translatable("selectServer.deleteButton");
                                    Text text4 = ScreenTexts.CANCEL;
                                    this.client.setScreen(new ConfirmScreen(this::removeEntry, text, text2, text3, text4));
                                }
                            }
                        },
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();

        // little space
        listOffset.getAndIncrement();

        this.buttonAdd = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        "Add",
                        button -> {
                            this.selectedEntry = new ServerInfo("", "", ServerInfo.ServerType.OTHER);
                            this.client.setScreen(new AddServerScreen(this, Text.of("Add Server"), this::addEntry, this.selectedEntry));
                        },
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();



        this.directConnect = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        "Direct Connect",
                        (button) -> {
                            this.selectedEntry = new ServerInfo("New Server", "", ServerInfo.ServerType.OTHER);
                            this.client.setScreen(new DirectConnectScreen(this, this::directConnect, this.selectedEntry));
                        },
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();



        this.refreshButton = ScreenHelper.build(
                new ScreenHelper.ButtonInfo(
                        "Refresh",
                        button -> {
                            this.refresh();
                        },
                        ScreenHelper.getScreenRightListPos(listOffset.getAndIncrement())
                )
        ).build();

    }

    @Override
    public void removed() {
        if (this.lanServerDetector != null) {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }

        this.serverListPinger.cancel();
        this.serverListWidget.onRemoved();
    }

    private void refresh() {
        this.client.setScreen(new CustomMultiplayerScreen(this.parent));
    }

    private void directConnect(boolean confirmedAction) {
        if (confirmedAction) {
            ServerInfo serverInfo = this.serverList.get(this.selectedEntry.address);
            if (serverInfo == null) {
                this.serverList.add(this.selectedEntry, true);
                this.serverList.saveFile();
                this.connect(this.selectedEntry);
            } else {
                this.connect(serverInfo);
            }
        } else {
            this.client.setScreen(this);
        }
    }

    private void addEntry(boolean confirmedAction) {
        if (confirmedAction) {
            ServerInfo serverInfo = this.serverList.tryUnhide(this.selectedEntry.address);
            if (serverInfo != null) {
                serverInfo.copyFrom(this.selectedEntry);
                this.serverList.saveFile();
            } else {
                this.serverList.add(this.selectedEntry, false);
                this.serverList.saveFile();
            }

            this.serverListWidget.setSelected(null);
            this.serverListWidget.setServers(this.serverList);
        }

        this.client.setScreen(this);
    }

    private void editEntry(boolean confirmedAction) {
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (confirmedAction && entry instanceof MultiplayerServerListWidget.ServerEntry) {
            ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry)entry).getServer();
            serverInfo.name = this.selectedEntry.name;
            serverInfo.address = this.selectedEntry.address;
            serverInfo.copyWithSettingsFrom(this.selectedEntry);
            this.serverList.saveFile();
            this.serverListWidget.setServers(this.serverList);
        }

        this.client.setScreen(this);
    }

    private void removeEntry(boolean confirmedAction) {
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (confirmedAction && entry instanceof MultiplayerServerListWidget.ServerEntry) {
            this.serverList.remove(((MultiplayerServerListWidget.ServerEntry)entry).getServer());
            this.serverList.saveFile();
            this.serverListWidget.setSelected(null);
            this.serverListWidget.setServers(this.serverList);
        }

        this.client.setScreen(this);
    }

    @Override
    protected void updateButtonActivationStates() {
        if (this.buttonJoin != null) this.buttonJoin.active = false;
        if (this.buttonEdit != null) this.buttonEdit.active = false;
        if (this.buttonDelete != null) this.buttonDelete.active = false;
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (entry != null && !(entry instanceof MultiplayerServerListWidget.ScanningEntry)) {
            this.buttonJoin.active = true;
            if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
                this.buttonEdit.active = true;
                this.buttonDelete.active = true;
            }
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (super.keyPressed(input)) {
            return true;
        } else if (input.key() == InputUtil.GLFW_KEY_F5) {
            this.refresh();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void tick() {
        this.serverListPinger.tick();

        if (this.lanServers != null) {
            List<LanServerInfo> list = this.lanServers.getEntriesIfUpdated();
            if (list != null) {
                this.serverListWidget.setLanServers(list);
            }
        }
    }

    @Override
    public ServerList getServerList() {
        return this.serverList;
    }

    @Override
    public MultiplayerServerListPinger getServerListPinger() {
        return this.serverListPinger;
    }

    @Override
    public void connect(ServerInfo entry) {
        ConnectScreen.connect(this, this.client, ServerAddress.parse(entry.address), entry, false, null);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.init();
    }
}
