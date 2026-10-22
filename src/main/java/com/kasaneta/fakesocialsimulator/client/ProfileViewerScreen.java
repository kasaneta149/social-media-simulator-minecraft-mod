package com.kasaneta.fakesocialsimulator.client;

import com.kasaneta.fakesocialsimulator.SocialFormat;
import com.kasaneta.fakesocialsimulator.network.ProfileEntry;
import com.kasaneta.fakesocialsimulator.network.ProfileSearchRequestPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.List;
import java.util.regex.Pattern;

public class ProfileViewerScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PANEL_W = 330;
    private static final int PANEL_H = 182;
    private static final int PADDING = 10;
    private static final int LIST_W = 124;
    private static final int ROW_H = 15;
    private static final int MAX_ROWS = 8;
    private static final int STAT_COUNT = 6;
    private static final int MAX_VALUE_WIDTH = 78;
    private static final long SEND_DELAY_MS = 250L;
    private static final long MIN_SEND_INTERVAL_MS = 220L;
    private static final long REFRESH_MS = 4000L;
    private static final int KEY_ENTER = 257;
    private static final int KEY_KP_ENTER = 335;
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_]*");

    private static final int COLOR_PANEL = 0xF0141822;
    private static final int COLOR_BORDER = 0xFF3A4260;
    private static final int COLOR_ACCENT = 0xFF4F8CFF;
    private static final int COLOR_CARD = 0xFF1B2030;
    private static final int COLOR_CARD_BORDER = 0xFF2C3452;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFF98A0B8;
    private static final int COLOR_HINT = 0xFF7C849C;
    private static final int COLOR_SELECTED = 0x604F8CFF;
    private static final int COLOR_HOVER = 0x28FFFFFF;
    private static final int COLOR_ONLINE = 0xFF4CD964;
    private static final int COLOR_OFFLINE = 0xFF5C6378;
    private static final int COLOR_ERROR = 0xFFFF6B6B;

    private static final Component TITLE = Component.translatable("screen.fakesocialsimulator.profile_viewer.title");
    private static final Component HINT = Component.translatable("screen.fakesocialsimulator.profile_viewer.hint");
    private static final Component EMPTY = Component.translatable("screen.fakesocialsimulator.profile_viewer.empty");
    private static final Component NO_RESULTS = Component.translatable("screen.fakesocialsimulator.profile_viewer.no_results");
    private static final Component LIST_ONLINE = Component.translatable("screen.fakesocialsimulator.profile_viewer.list_online");
    private static final Component LIST_RESULTS = Component.translatable("screen.fakesocialsimulator.profile_viewer.list_results");
    private static final Component UNSUPPORTED = Component.translatable("screen.fakesocialsimulator.unsupported");
    private static final Component STATUS_ONLINE = Component.translatable("screen.fakesocialsimulator.status.online");
    private static final Component STATUS_OFFLINE = Component.translatable("screen.fakesocialsimulator.status.offline");
    private static final Component[] STAT_LABELS = {
            Component.translatable("screen.fakesocialsimulator.stat.followers"),
            Component.translatable("screen.fakesocialsimulator.stat.following"),
            Component.translatable("screen.fakesocialsimulator.stat.posts"),
            Component.translatable("screen.fakesocialsimulator.stat.views"),
            Component.translatable("screen.fakesocialsimulator.stat.likes"),
            Component.translatable("screen.fakesocialsimulator.stat.comments")
    };

    private EditBox searchBox;
    private List<ProfileEntry> results = List.of();
    private ProfileEntry selected;
    private Component selectedTitle = Component.empty();
    private final String[] values = new String[STAT_COUNT];
    private final int[] valueWidths = new int[STAT_COUNT];
    private boolean manualSelection;
    private boolean loaded;
    private boolean firstInit = true;
    private boolean unsupported;
    private boolean pendingSend;
    private long nextSend;
    private long nextRefresh;
    private long lastSent;

    private int panelX;
    private int panelY;
    private int listX;
    private int listY;
    private int cardX;
    private int cardY;
    private int cardW;
    private int cardH;

    public ProfileViewerScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        String previous = searchBox == null ? "" : searchBox.getValue();

        panelX = (this.width - PANEL_W) / 2;
        panelY = Math.max(4, (this.height - PANEL_H) / 2);
        listX = panelX + PADDING;
        listY = panelY + 52;
        cardX = listX + LIST_W + 8;
        cardY = panelY + 36;
        cardW = PANEL_W - PADDING * 2 - LIST_W - 8;
        cardH = MAX_ROWS * ROW_H + 16;

        searchBox = new EditBox(this.font, panelX + PADDING, panelY + PADDING, PANEL_W - PADDING * 2, 20, TITLE);
        searchBox.setMaxLength(16);
        searchBox.setFilter(text -> NAME_PATTERN.matcher(text).matches());
        searchBox.setResponder(this::onQueryChanged);
        this.addRenderableWidget(searchBox);
        this.setInitialFocus(searchBox);

        if (!previous.isEmpty()) {
            searchBox.setValue(previous);
        }
        if (selected != null) {
            select(selected);
        }

        if (firstInit) {
            firstInit = false;
            sendQuery();
        }
    }

    private void onQueryChanged(String query) {
        manualSelection = false;
        schedule(SEND_DELAY_MS);
    }

    private void schedule(long delay) {
        long now = System.currentTimeMillis();
        pendingSend = true;
        nextSend = Math.max(now + delay, lastSent + MIN_SEND_INTERVAL_MS);
    }

    private void sendQuery() {
        long now = System.currentTimeMillis();
        try {
            PacketDistributor.sendToServer(new ProfileSearchRequestPayload(searchBox.getValue().trim()));
        } catch (UnsupportedOperationException e) {
            unsupported = true;
            LOGGER.error("[FakeSocialSimulator] Could not send the profile search request: {}", e.getMessage());
        }
        pendingSend = false;
        lastSent = now;
        nextRefresh = now + REFRESH_MS;
    }

    @Override
    public void tick() {
        super.tick();
        if (unsupported) return;
        long now = System.currentTimeMillis();
        if (pendingSend ? now >= nextSend : now >= nextRefresh) {
            sendQuery();
        }
    }

    public void setResults(List<ProfileEntry> list) {
        this.results = list;
        this.loaded = true;

        if (selected != null) {
            for (ProfileEntry entry : list) {
                if (entry.uuid().equals(selected.uuid())) {
                    select(entry);
                    break;
                }
            }
        }

        boolean hasQuery = !searchBox.getValue().trim().isEmpty();
        if (!manualSelection && hasQuery) {
            if (list.isEmpty()) {
                selected = null;
            } else {
                select(list.get(0));
            }
        }
    }

    private void select(ProfileEntry entry) {
        this.selected = entry;
        this.selectedTitle = Component.literal(entry.name()).withStyle(ChatFormatting.BOLD);

        String separator = Language.getInstance().getOrDefault("fakesocialsimulator.number_separator");
        long[] raw = {entry.followers(), entry.following(), entry.posts(), entry.views(), entry.likes(), entry.comments()};
        for (int i = 0; i < STAT_COUNT; i++) {
            String full = SocialFormat.grouped(raw[i], separator);
            values[i] = this.font.width(full) > MAX_VALUE_WIDTH ? SocialFormat.compact(raw[i]) : full;
            valueWidths[i] = this.font.width(values[i]);
        }
    }

    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < listX || mouseX >= listX + LIST_W || mouseY < listY) return -1;
        int row = (int) ((mouseY - listY) / ROW_H);
        if (row < 0 || row >= Math.min(results.size(), MAX_ROWS)) return -1;
        return row;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int row = rowAt(mouseX, mouseY);
            if (row >= 0) {
                manualSelection = true;
                select(results.get(row));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == KEY_ENTER || keyCode == KEY_KP_ENTER) && searchBox.isFocused()) {
            manualSelection = false;
            schedule(0L);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + PANEL_H + 1, COLOR_BORDER);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, COLOR_PANEL);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + 2, COLOR_ACCENT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (searchBox.getValue().isEmpty()) {
            graphics.drawString(this.font, HINT, searchBox.getX() + 4, searchBox.getY() + 6, COLOR_HINT, false);
        }

        renderList(graphics, mouseX, mouseY);
        renderCard(graphics);
    }

    private void renderList(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean showingOnline = searchBox.getValue().trim().isEmpty();
        graphics.drawString(this.font, showingOnline ? LIST_ONLINE : LIST_RESULTS, listX, listY - 13, COLOR_MUTED, false);

        if (results.isEmpty()) {
            if (loaded && !showingOnline) {
                graphics.drawString(this.font, NO_RESULTS, listX + 2, listY + 4, COLOR_MUTED, false);
            }
            return;
        }

        int rows = Math.min(results.size(), MAX_ROWS);
        for (int i = 0; i < rows; i++) {
            ProfileEntry entry = results.get(i);
            int y = listY + i * ROW_H;

            boolean isSelected = selected != null && selected.uuid().equals(entry.uuid());
            boolean hovered = mouseX >= listX && mouseX < listX + LIST_W && mouseY >= y && mouseY < y + ROW_H;
            if (isSelected) {
                graphics.fill(listX, y, listX + LIST_W, y + ROW_H - 1, COLOR_SELECTED);
            } else if (hovered) {
                graphics.fill(listX, y, listX + LIST_W, y + ROW_H - 1, COLOR_HOVER);
            }

            graphics.fill(listX + 4, y + 5, listX + 8, y + 9, entry.online() ? COLOR_ONLINE : COLOR_OFFLINE);
            graphics.drawString(this.font, this.font.plainSubstrByWidth(entry.name(), LIST_W - 18), listX + 13, y + 3, COLOR_TEXT, false);
        }
    }

    private void renderCard(GuiGraphics graphics) {
        graphics.fill(cardX - 1, cardY - 1, cardX + cardW + 1, cardY + cardH + 1, COLOR_CARD_BORDER);
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, COLOR_CARD);

        if (unsupported) {
            graphics.drawWordWrap(this.font, UNSUPPORTED, cardX + 12, cardY + cardH / 2 - 18, cardW - 24, COLOR_ERROR);
            return;
        }

        if (selected == null) {
            graphics.drawWordWrap(this.font, EMPTY, cardX + 12, cardY + cardH / 2 - 9, cardW - 24, COLOR_MUTED);
            return;
        }

        graphics.drawString(this.font, selectedTitle, cardX + 8, cardY + 8, COLOR_TEXT, false);

        graphics.fill(cardX + 8, cardY + 23, cardX + 12, cardY + 27, selected.online() ? COLOR_ONLINE : COLOR_OFFLINE);
        graphics.drawString(this.font, selected.online() ? STATUS_ONLINE : STATUS_OFFLINE, cardX + 16, cardY + 21, COLOR_MUTED, false);

        graphics.fill(cardX + 8, cardY + 34, cardX + cardW - 8, cardY + 35, COLOR_CARD_BORDER);

        for (int i = 0; i < STAT_COUNT; i++) {
            int y = cardY + 42 + i * 14;
            graphics.drawString(this.font, STAT_LABELS[i], cardX + 8, y, COLOR_MUTED, false);
            graphics.drawString(this.font, values[i], cardX + cardW - 8 - valueWidths[i], y, COLOR_TEXT, false);
        }
    }
}
