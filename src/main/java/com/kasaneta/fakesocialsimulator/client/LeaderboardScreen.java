package com.kasaneta.fakesocialsimulator.client;

import com.kasaneta.fakesocialsimulator.SocialFormat;
import com.kasaneta.fakesocialsimulator.network.LeaderboardRequestPayload;
import com.kasaneta.fakesocialsimulator.network.ProfileEntry;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

public class LeaderboardScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PANEL_W = 300;
    private static final int ROW_H = 15;
    private static final int HEADER_H = 42;
    private static final int FOOTER_H = 26;
    private static final int MAX_ROWS = 10;
    private static final long REFRESH_MS = 5000L;

    private static final int COLUMN_RANK_X = 10;
    private static final int COLUMN_NAME_X = 30;
    private static final int COLUMN_NAME_WIDTH = 84;
    private static final int COLUMN_FOLLOWERS_RIGHT = 176;
    private static final int COLUMN_VIEWS_RIGHT = 232;
    private static final int COLUMN_LIKES_RIGHT = 290;

    private static final int COLOR_PANEL = 0xF0141822;
    private static final int COLOR_BORDER = 0xFF3A4260;
    private static final int COLOR_ACCENT = 0xFFF2C94C;
    private static final int COLOR_LINE = 0xFF2C3452;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFF98A0B8;
    private static final int COLOR_ZEBRA = 0x12FFFFFF;
    private static final int COLOR_SELF = 0x404F8CFF;
    private static final int COLOR_ONLINE = 0xFF4CD964;
    private static final int COLOR_OFFLINE = 0xFF5C6378;
    private static final int COLOR_GOLD = 0xFFFFD34E;
    private static final int COLOR_SILVER = 0xFFC8CED8;
    private static final int COLOR_BRONZE = 0xFFD08A4E;
    private static final int COLOR_ERROR = 0xFFFF6B6B;

    private static final Component TITLE = Component.translatable("screen.fakesocialsimulator.leaderboard.title");
    private static final Component TITLE_BOLD = Component.translatable("screen.fakesocialsimulator.leaderboard.title").withStyle(ChatFormatting.BOLD);
    private static final Component HEADER_RANK = Component.translatable("screen.fakesocialsimulator.leaderboard.rank");
    private static final Component HEADER_PLAYER = Component.translatable("screen.fakesocialsimulator.leaderboard.player");
    private static final Component HEADER_FOLLOWERS = Component.translatable("screen.fakesocialsimulator.leaderboard.followers");
    private static final Component HEADER_VIEWS = Component.translatable("screen.fakesocialsimulator.leaderboard.views");
    private static final Component HEADER_LIKES = Component.translatable("screen.fakesocialsimulator.leaderboard.likes");
    private static final Component UNSUPPORTED = Component.translatable("screen.fakesocialsimulator.unsupported");
    private static final Component EMPTY = Component.translatable("screen.fakesocialsimulator.leaderboard.empty");

    private List<ProfileEntry> entries = List.of();
    private String[] rankText = new String[0];
    private String[] followersText = new String[0];
    private String[] viewsText = new String[0];
    private String[] likesText = new String[0];
    private int[] followersWidth = new int[0];
    private int[] viewsWidth = new int[0];
    private int[] likesWidth = new int[0];
    private Component rankLine = Component.empty();
    private int yourRank;
    private boolean loaded;
    private boolean firstInit = true;
    private boolean unsupported;
    private long nextRefresh;
    private UUID selfId;

    private int panelX;
    private int panelY;
    private int panelH;
    private int rows;
    private int rowsY;

    public LeaderboardScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        rows = Math.max(1, Math.min(MAX_ROWS, (this.height - 16 - HEADER_H - FOOTER_H) / ROW_H));
        panelH = HEADER_H + rows * ROW_H + FOOTER_H;
        panelX = (this.width - PANEL_W) / 2;
        panelY = Math.max(4, (this.height - panelH) / 2);
        rowsY = panelY + HEADER_H;
        selfId = (this.minecraft != null && this.minecraft.player != null) ? this.minecraft.player.getUUID() : null;

        if (firstInit) {
            firstInit = false;
            sendRequest();
        }
    }

    private void sendRequest() {
        try {
            PacketDistributor.sendToServer(new LeaderboardRequestPayload());
        } catch (UnsupportedOperationException e) {
            unsupported = true;
            LOGGER.error("[FakeSocialSimulator] Could not send the leaderboard request: {}", e.getMessage());
        }
        nextRefresh = System.currentTimeMillis() + REFRESH_MS;
    }

    @Override
    public void tick() {
        super.tick();
        if (!unsupported && System.currentTimeMillis() >= nextRefresh) {
            sendRequest();
        }
    }

    public void setData(List<ProfileEntry> list, int rank) {
        this.entries = list;
        this.yourRank = rank;
        this.loaded = true;

        int size = list.size();
        rankText = new String[size];
        followersText = new String[size];
        viewsText = new String[size];
        likesText = new String[size];
        followersWidth = new int[size];
        viewsWidth = new int[size];
        likesWidth = new int[size];
        for (int i = 0; i < size; i++) {
            ProfileEntry entry = list.get(i);
            rankText[i] = String.valueOf(i + 1);
            followersText[i] = SocialFormat.compact(entry.followers());
            viewsText[i] = SocialFormat.compact(entry.views());
            likesText[i] = SocialFormat.compact(entry.likes());
            followersWidth[i] = this.font.width(followersText[i]);
            viewsWidth[i] = this.font.width(viewsText[i]);
            likesWidth[i] = this.font.width(likesText[i]);
        }

        rankLine = rank > 0
                ? Component.translatable("screen.fakesocialsimulator.leaderboard.your_rank", String.valueOf(rank))
                : Component.empty();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + panelH + 1, COLOR_BORDER);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, COLOR_PANEL);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + 2, COLOR_ACCENT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, TITLE_BOLD, panelX + PANEL_W / 2, panelY + 9, COLOR_TEXT);

        int headerY = panelY + 26;
        graphics.drawString(this.font, HEADER_RANK, panelX + COLUMN_RANK_X, headerY, COLOR_MUTED, false);
        graphics.drawString(this.font, HEADER_PLAYER, panelX + COLUMN_NAME_X, headerY, COLOR_MUTED, false);
        drawRight(graphics, HEADER_FOLLOWERS, panelX + COLUMN_FOLLOWERS_RIGHT, headerY);
        drawRight(graphics, HEADER_VIEWS, panelX + COLUMN_VIEWS_RIGHT, headerY);
        drawRight(graphics, HEADER_LIKES, panelX + COLUMN_LIKES_RIGHT, headerY);
        graphics.fill(panelX + 8, panelY + 37, panelX + PANEL_W - 8, panelY + 38, COLOR_LINE);

        if (unsupported) {
            graphics.drawWordWrap(this.font, UNSUPPORTED, panelX + 14, rowsY + 12, PANEL_W - 28, COLOR_ERROR);
        } else if (entries.isEmpty()) {
            if (loaded) {
                graphics.drawCenteredString(this.font, EMPTY, panelX + PANEL_W / 2, rowsY + 20, COLOR_MUTED);
            }
        } else {
            renderRows(graphics);
        }

        int footerY = panelY + panelH - 17;
        graphics.fill(panelX + 8, footerY - 6, panelX + PANEL_W - 8, footerY - 5, COLOR_LINE);
        graphics.drawString(this.font, rankLine, panelX + COLUMN_RANK_X, footerY, COLOR_MUTED, false);
    }

    private void drawRight(GuiGraphics graphics, Component text, int rightX, int y) {
        graphics.drawString(this.font, text, rightX - this.font.width(text), y, COLOR_MUTED, false);
    }

    private void renderRows(GuiGraphics graphics) {
        int count = Math.min(entries.size(), rows);
        for (int i = 0; i < count; i++) {
            ProfileEntry entry = entries.get(i);
            int y = rowsY + i * ROW_H;

            if (selfId != null && selfId.equals(entry.uuid())) {
                graphics.fill(panelX + 6, y, panelX + PANEL_W - 6, y + ROW_H - 1, COLOR_SELF);
            } else if ((i & 1) == 1) {
                graphics.fill(panelX + 6, y, panelX + PANEL_W - 6, y + ROW_H - 1, COLOR_ZEBRA);
            }

            int rankColor = i == 0 ? COLOR_GOLD : (i == 1 ? COLOR_SILVER : (i == 2 ? COLOR_BRONZE : COLOR_TEXT));
            int textY = y + 3;
            graphics.drawString(this.font, rankText[i], panelX + COLUMN_RANK_X, textY, rankColor, false);

            graphics.fill(panelX + COLUMN_NAME_X, y + 5, panelX + COLUMN_NAME_X + 4, y + 9, entry.online() ? COLOR_ONLINE : COLOR_OFFLINE);
            graphics.drawString(this.font, this.font.plainSubstrByWidth(entry.name(), COLUMN_NAME_WIDTH - 9), panelX + COLUMN_NAME_X + 9, textY, COLOR_TEXT, false);

            graphics.drawString(this.font, followersText[i], panelX + COLUMN_FOLLOWERS_RIGHT - followersWidth[i], textY, COLOR_TEXT, false);
            graphics.drawString(this.font, viewsText[i], panelX + COLUMN_VIEWS_RIGHT - viewsWidth[i], textY, COLOR_MUTED, false);
            graphics.drawString(this.font, likesText[i], panelX + COLUMN_LIKES_RIGHT - likesWidth[i], textY, COLOR_MUTED, false);
        }
    }
}
