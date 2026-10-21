package com.kasaneta.fakesocialsimulator.client;

import com.kasaneta.fakesocialsimulator.SocialFormat;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientStatsCache {

    private static final Map<UUID, PlayerMetrics> CACHE = new HashMap<>();

    public static final class PlayerMetrics {
        private final long posts;
        private final long followers;
        private final long following;
        private Component text;
        private String textLang;

        PlayerMetrics(long posts, long followers, long following) {
            this.posts = posts;
            this.followers = followers;
            this.following = following;
        }

        public long posts() { return posts; }
        public long followers() { return followers; }
        public long following() { return following; }

        public Component text(String langCode) {
            if (this.text == null || !langCode.equals(this.textLang)) {
                this.text = buildText(langCode);
                this.textLang = langCode;
            }
            return this.text;
        }

        private Component buildText(String langCode) {
            boolean zeroIsSingular = langCode.startsWith("fr");
            String separator = Language.getInstance().getOrDefault("fakesocialsimulator.number_separator");

            Component postsPart = part("fakesocialsimulator.nametag.posts", posts, SocialFormat.grouped(posts, separator), zeroIsSingular);
            Component followersPart = part("fakesocialsimulator.nametag.followers", followers, SocialFormat.compact(followers), zeroIsSingular);
            Component followingPart = part("fakesocialsimulator.nametag.following", following, SocialFormat.grouped(following, separator), zeroIsSingular);

            MutableComponent result = Component.empty();
            result.append(postsPart).append(" ■ ").append(followersPart).append(" ■ ").append(followingPart);
            return result;
        }

        private static Component part(String baseKey, long count, String shown, boolean zeroIsSingular) {
            boolean singular = count == 1 || (count == 0 && zeroIsSingular);
            return Component.translatable(baseKey + (singular ? ".one" : ".other"), shown);
        }
    }

    public static void update(UUID uuid, long posts, long followers, long following) {
        PlayerMetrics old = CACHE.get(uuid);
        if (old != null && old.posts == posts && old.followers == followers && old.following == following) {
            return;
        }
        CACHE.put(uuid, new PlayerMetrics(posts, followers, following));
    }

    public static PlayerMetrics get(UUID uuid) {
        return CACHE.get(uuid);
    }

    public static void remove(UUID uuid) {
        CACHE.remove(uuid);
    }

    public static void clear() {
        CACHE.clear();
    }
}
