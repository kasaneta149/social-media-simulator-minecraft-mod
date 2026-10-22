package com.kasaneta.fakesocialsimulator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import java.util.Locale;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerStats {
    public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("followers_count").forGetter(PlayerStats::getRawFollowersCount),
            Codec.LONG.fieldOf("following_count").forGetter(PlayerStats::getFollowingCount),
            Codec.LONG.fieldOf("posts_count").forGetter(PlayerStats::getPostsCount),
            Codec.DOUBLE.fieldOf("activity_pool").forGetter(PlayerStats::getActivityPool),
            Codec.DOUBLE.fieldOf("trend").forGetter(PlayerStats::getTrend),
            Codec.DOUBLE.fieldOf("pending_followers").forGetter(s -> s.pendingFollowers),
            Codec.LONG.fieldOf("last_seen").forGetter(PlayerStats::getLastSeen),
            Codec.DOUBLE.fieldOf("post_spam_level").forGetter(PlayerStats::getPostSpamLevel),
            Codec.LONG.optionalFieldOf("permanent_followers", 0L).forGetter(PlayerStats::getPermanentFollowers),
            Codec.LONG.optionalFieldOf("view_count", 0L).forGetter(PlayerStats::getViewCount),
            Codec.LONG.optionalFieldOf("like_count", 0L).forGetter(PlayerStats::getLikeCount),
            Codec.LONG.optionalFieldOf("comment_count", 0L).forGetter(PlayerStats::getCommentCount)
    ).apply(instance, PlayerStats::new));

    public enum ActionType {
        BLOCK_BREAK(80.0, 180.0),
        BLOCK_PLACE(80.0, 180.0),
        CRAFT(40.0, 240.0),
        SMELT(40.0, 240.0),
        EAT_DRINK(12.0, 300.0),
        FISH(25.0, 300.0),
        TAME(4.0, 1800.0),
        BREED(8.0, 600.0),
        DIMENSION(3.0, 3600.0),
        ADVANCEMENT(4.0, 3600.0),
        BOSS_KILL(1.5, 7200.0),
        MOB_KILL(30.0, 240.0),
        FOLLOW(30.0, 600.0);

        public final double capacity;
        public final double halfLifeSec;

        ActionType(double capacity, double halfLifeSec) {
            this.capacity = capacity;
            this.halfLifeSec = halfLifeSec;
        }
    }

    private static final ActionType[] ACTION_TYPES = ActionType.values();

    private long followersCount;
    private long permanentFollowers;
    private long followingCount;
    private long postsCount;

    private long viewCount;
    private long likeCount;
    private long commentCount;

    private double pendingViews;
    private double pendingLikes;
    private double pendingComments;

    private double activityPool;
    private double trend;
    private double pendingFollowers;
    private long lastSeen;

    private double postSpamLevel;

    private long seed;
    private long peakFollowers;
    private double exodusPool;
    private double exodusTau;
    private long lastExodusTime;
    private String name;
    private String nameLower;

    private long lastAchievementTime;
    private double momentBoost = 1.0;
    private int deathStreak;
    private long lastDeathTime;
    private double deletionSpamLevel;
    private long spamStamp;
    private long deletionStamp;

    private double appeal = 1.0;
    private double chatty = 1.0;
    private double volatility = 1.0;
    private double luck = 1.0;
    private double quality = 1.0;

    private long lastActivityTime;
    private boolean onlineFlag;
    private long nextAdvancementCheck;
    private long lastProcessTime;

    private long drainedViews;
    private long lastQueryTime;

    private long syncedPosts = -1L;
    private long syncedFollowers = -1L;
    private long syncedFollowing = -1L;

    private final double[] fatigue = new double[ACTION_TYPES.length];
    private final long[] fatigueStamp = new long[ACTION_TYPES.length];
    private int cachedActiveCategories;
    private long categoriesStamp;

    public PlayerStats() {
        this(0L, 0L, 0L, 0.0, 0.0, 0.0, System.currentTimeMillis(), 0.0, 0L, 0L, 0L, 0L);
    }

    public PlayerStats(long followersCount, long followingCount, long postsCount, double activityPool, double trend, double pendingFollowers, long lastSeen, double postSpamLevel, long permanentFollowers, long viewCount, long likeCount, long commentCount) {
        this.followersCount = Math.max(0L, followersCount);
        this.followingCount = Math.max(0L, followingCount);
        this.postsCount = Math.max(0L, postsCount);
        this.activityPool = activityPool;
        this.trend = trend;
        this.pendingFollowers = pendingFollowers;
        this.lastSeen = lastSeen;
        this.postSpamLevel = Math.max(0.0, postSpamLevel);
        this.permanentFollowers = Math.max(0L, permanentFollowers);
        this.viewCount = Math.max(0L, viewCount);
        this.likeCount = Math.max(0L, likeCount);
        this.commentCount = Math.max(0L, commentCount);

        long now = System.currentTimeMillis();
        this.seed = ThreadLocalRandom.current().nextLong();
        initTraits();
        this.peakFollowers = this.followersCount + this.permanentFollowers;
        this.lastActivityTime = lastSeen;
        this.spamStamp = now;
        this.deletionStamp = now;
        this.lastProcessTime = now;
    }

    private void initTraits() {
        SplittableRandom sr = new SplittableRandom(this.seed);
        this.appeal = 0.80 + sr.nextDouble() * 0.50;
        this.chatty = 0.60 + sr.nextDouble() * 1.00;
        this.volatility = 0.70 + sr.nextDouble() * 0.60;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putLong("followers_count", this.followersCount);
        tag.putLong("permanent_followers", this.permanentFollowers);
        tag.putLong("following_count", this.followingCount);
        tag.putLong("posts_count", this.postsCount);
        tag.putDouble("activity_pool", this.activityPool);
        tag.putDouble("trend", this.trend);
        tag.putDouble("pending_followers", this.pendingFollowers);
        tag.putLong("last_seen", this.lastSeen);
        tag.putDouble("post_spam_level", this.postSpamLevel);
        tag.putLong("view_count", this.viewCount);
        tag.putLong("like_count", this.likeCount);
        tag.putLong("comment_count", this.commentCount);
        tag.putDouble("pending_views", this.pendingViews);
        tag.putDouble("pending_likes", this.pendingLikes);
        tag.putDouble("pending_comments", this.pendingComments);
        tag.putLong("seed", this.seed);
        tag.putLong("peak_followers", this.peakFollowers);
        tag.putDouble("exodus_pool", this.exodusPool);
        tag.putDouble("exodus_tau", this.exodusTau);
        tag.putLong("last_exodus", this.lastExodusTime);
        if (this.name != null) tag.putString("name", this.name);
        return tag;
    }

    public static PlayerStats load(CompoundTag tag) {
        PlayerStats stats = new PlayerStats(
            tag.getLong("followers_count"),
            tag.getLong("following_count"),
            tag.getLong("posts_count"),
            tag.getDouble("activity_pool"),
            tag.getDouble("trend"),
            tag.getDouble("pending_followers"),
            tag.getLong("last_seen"),
            tag.getDouble("post_spam_level"),
            tag.contains("permanent_followers") ? tag.getLong("permanent_followers") : 0L,
            tag.contains("view_count") ? tag.getLong("view_count") : 0L,
            tag.contains("like_count") ? tag.getLong("like_count") : 0L,
            tag.contains("comment_count") ? tag.getLong("comment_count") : 0L
        );
        stats.pendingViews = tag.getDouble("pending_views");
        stats.pendingLikes = tag.getDouble("pending_likes");
        stats.pendingComments = tag.getDouble("pending_comments");

        if (tag.contains("seed")) {
            stats.seed = tag.getLong("seed");
            stats.initTraits();
        }
        stats.peakFollowers = Math.max(tag.getLong("peak_followers"), stats.getFollowersCount());
        stats.exodusPool = Math.max(0.0, tag.getDouble("exodus_pool"));
        stats.exodusTau = tag.getDouble("exodus_tau");
        stats.lastExodusTime = tag.getLong("last_exodus");
        if (tag.contains("name")) stats.setName(tag.getString("name"));

        stats.postSpamLevel = Math.min(stats.postSpamLevel, 6.0);
        return stats;
    }

    public long getFollowersCount() { return followersCount + permanentFollowers; }
    public long getRawFollowersCount() { return followersCount; }
    public long getPermanentFollowers() { return permanentFollowers; }
    public long getPeakFollowers() { return peakFollowers; }
    public long getFollowingCount() { return followingCount; }
    public long getPostsCount() { return postsCount; }
    public long getViewCount() { return viewCount; }
    public long getLikeCount() { return likeCount; }
    public long getCommentCount() { return commentCount; }

    public double getActivityPool() { return activityPool; }
    public double getTrend() { return trend; }
    public long getLastSeen() { return lastSeen; }
    public double getPostSpamLevel() { return postSpamLevel; }
    public double getPendingViews() { return pendingViews; }

    public long getLastAchievementTime() { return lastAchievementTime; }
    public void setLastAchievementTime(long time) { this.lastAchievementTime = time; }

    public double getMomentBoost() { return momentBoost; }
    public void setMomentBoost(double boost) { this.momentBoost = Math.max(1.0, Math.min(1.5, boost)); }

    public int getDeathStreak() { return deathStreak; }
    public void setDeathStreak(int streak) { this.deathStreak = streak; }

    public long getLastDeathTime() { return lastDeathTime; }
    public void setLastDeathTime(long time) { this.lastDeathTime = time; }

    public double getDeletionSpamLevel() { return deletionSpamLevel; }
    public void addDeletionSpam(double amount) { this.deletionSpamLevel += amount; }
    public void decayDeletionSpam(double amount) { this.deletionSpamLevel = Math.max(0.0, this.deletionSpamLevel - amount); }

    public double getAppeal() { return appeal; }
    public double getChatty() { return chatty; }
    public double getVolatility() { return volatility; }
    public double getLuck() { return luck; }
    public double getQuality() { return quality; }

    public long getLastActivityTime() { return lastActivityTime; }
    public void touchActivity(long now) { this.lastActivityTime = now; }

    public boolean isOnlineFlag() { return onlineFlag; }
    public void setOnlineFlag(boolean online) { this.onlineFlag = online; }

    public long getNextAdvancementCheck() { return nextAdvancementCheck; }
    public void setNextAdvancementCheck(long time) { this.nextAdvancementCheck = time; }

    public boolean needsNametagSync() {
        return this.syncedPosts != this.postsCount
                || this.syncedFollowers != getFollowersCount()
                || this.syncedFollowing != this.followingCount;
    }

    public void markNametagSynced() {
        this.syncedPosts = this.postsCount;
        this.syncedFollowers = getFollowersCount();
        this.syncedFollowing = this.followingCount;
    }

    public void invalidateNametagSync() {
        this.syncedPosts = -1L;
        this.syncedFollowers = -1L;
        this.syncedFollowing = -1L;
    }

    public String getName() { return name; }
    public String getNameLower() { return nameLower; }

    public void setName(String newName) {
        if (newName == null || newName.equals(this.name)) return;
        this.name = newName;
        this.nameLower = newName.toLowerCase(Locale.ROOT);
    }

    public boolean allowQuery(long now) {
        if (now - this.lastQueryTime < 200L) return false;
        this.lastQueryTime = now;
        return true;
    }

    public long getLastProcessTime() { return lastProcessTime; }
    public void setLastProcessTime(long time) { this.lastProcessTime = time; }

    public double getExodusPool() { return exodusPool; }
    public double getExodusTau() { return exodusTau; }
    public long getLastExodusTime() { return lastExodusTime; }

    public void stepLuck(double dtSec, double chaos) {
        double decay = Math.exp(-dtSec / 600.0);
        double sd = 0.21 * chaos;
        double next = 1.0 + (this.luck - 1.0) * decay
                + Math.sqrt(Math.max(0.0, 1.0 - decay * decay)) * sd * ThreadLocalRandom.current().nextGaussian();
        this.luck = Math.max(0.5, Math.min(1.8, next));
    }

    public void blendQuality(double target, double weight) {
        this.quality += (target - this.quality) * weight;
        this.quality = Math.max(0.5, Math.min(2.2, this.quality));
    }

    public void relaxQuality(double dtSec) {
        this.quality = 1.0 + (this.quality - 1.0) * Math.exp(-dtSec / 600.0);
    }

    private double decayedFatigue(int index, long now) {
        double f = this.fatigue[index];
        if (f <= 0.0) return 0.0;
        double dtSec = (now - this.fatigueStamp[index]) / 1000.0;
        if (dtSec <= 0.0) return f;
        return f * Math.pow(0.5, dtSec / ACTION_TYPES[index].halfLifeSec);
    }

    private int activeCategories(long now) {
        if (now - this.categoriesStamp >= 2000L) {
            int count = 0;
            for (int i = 0; i < ACTION_TYPES.length; i++) {
                if (decayedFatigue(i, now) >= 1.0) count++;
            }
            this.cachedActiveCategories = count;
            this.categoriesStamp = now;
        }
        return this.cachedActiveCategories;
    }

    public double getVarietyScore(long now) {
        return Math.min(1.0, activeCategories(now) / 5.0);
    }

    public double registerAction(ActionType type, long now) {
        this.lastActivityTime = now;
        int i = type.ordinal();
        double f = decayedFatigue(i, now);

        double mult = 1.0;
        if (Config.ENABLE_ACTION_FATIGUE.get()) {
            mult = 1.0 / (1.0 + f / type.capacity);
        }

        this.fatigue[i] = f + 1.0;
        this.fatigueStamp[i] = now;

        int cats = Math.max(1, activeCategories(now));
        mult *= 0.9 + 0.1 * Math.min(5, cats);
        return mult;
    }

    public void decayPostSpamTo(long now) {
        if (this.postSpamLevel > 0.0) {
            double halfLifeSec = Config.POST_SATURATION_HALF_LIFE.get() * 60.0;
            double dtSec = (now - this.spamStamp) / 1000.0;
            if (dtSec > 0.0) {
                this.postSpamLevel *= Math.pow(0.5, dtSec / halfLifeSec);
                if (this.postSpamLevel < 0.01) this.postSpamLevel = 0.0;
            }
        }
        this.spamStamp = now;
    }

    public void decayDeletionSpamTo(long now) {
        if (this.deletionSpamLevel > 0.0) {
            double dtSec = (now - this.deletionStamp) / 1000.0;
            if (dtSec > 0.0) {
                this.deletionSpamLevel *= Math.pow(0.5, dtSec / 1800.0);
                if (this.deletionSpamLevel < 0.01) this.deletionSpamLevel = 0.0;
            }
        }
        this.deletionStamp = now;
    }

    public double registerPost(long now) {
        decayPostSpamTo(now);
        this.lastActivityTime = now;

        double yield = 1.0;
        if (Config.PUNISH_SPAMMERS.get()) {
            double ratio = this.postSpamLevel / Config.POST_SATURATION_THRESHOLD.get();
            yield = 1.0 / (1.0 + ratio * ratio);
        }
        this.postSpamLevel = Math.min(500.0, this.postSpamLevel + 1.0);
        return yield;
    }

    public void addPendingViews(double amount) { this.pendingViews += Math.max(0.0, amount); }
    public void addPendingLikes(double amount) { this.pendingLikes += amount; }
    public void addPendingComments(double amount) { this.pendingComments += amount; }

    public boolean hasPendingLegacyMetrics() { return this.pendingLikes != 0.0 || this.pendingComments != 0.0; }

    public long takeDrainedViews() {
        long v = this.drainedViews;
        this.drainedViews = 0L;
        return v;
    }

    public void addEngagement(long likes, long comments) {
        if (likes > 0L) this.likeCount += likes;
        if (comments > 0L) this.commentCount += comments;
    }

    public boolean processPendingMetrics() {
        boolean changed = false;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        if (this.pendingViews > 0.0) {
            long drain;
            if (this.pendingViews < 1.0) {
                drain = (rnd.nextDouble() < this.pendingViews) ? 1L : 0L;
                this.pendingViews = 0.0;
            } else {
                double viewRate = Config.VIEW_DRAIN_RATE.get();
                drain = Math.max(1L, (long) (this.pendingViews * viewRate));
                drain = Math.min(drain, (long) this.pendingViews);
                this.pendingViews -= drain;
            }
            if (drain > 0L) {
                this.viewCount += drain;
                this.drainedViews += drain;
                changed = true;
            }
        }

        if (this.pendingLikes > 0.0) {
            double likeRate = Config.LIKE_DRAIN_RATE.get();
            long drain = Math.max(1L, (long) (this.pendingLikes * likeRate));
            drain = Math.min(drain, (long) Math.ceil(this.pendingLikes));
            this.likeCount += drain;
            this.pendingLikes -= drain;
            changed = true;
        } else if (this.pendingLikes < 0.0) {
            double likeRate = Config.LIKE_DRAIN_RATE.get();
            long drain = Math.min(-1L, (long) (this.pendingLikes * likeRate));
            drain = Math.max(drain, (long) Math.floor(this.pendingLikes));
            this.likeCount = Math.max(0L, this.likeCount + drain);
            this.pendingLikes -= drain;
            changed = true;
        }

        if (this.pendingComments > 0.0) {
            double commentRate = Config.COMMENT_DRAIN_RATE.get();
            long drain = Math.max(1L, (long) (this.pendingComments * commentRate));
            drain = Math.min(drain, (long) Math.ceil(this.pendingComments));
            this.commentCount += drain;
            this.pendingComments -= drain;
            changed = true;
        } else if (this.pendingComments < 0.0) {
            double commentRate = Config.COMMENT_DRAIN_RATE.get();
            long drain = Math.min(-1L, (long) (this.pendingComments * commentRate));
            drain = Math.max(drain, (long) Math.floor(this.pendingComments));
            this.commentCount = Math.max(0L, this.commentCount + drain);
            this.pendingComments -= drain;
            changed = true;
        }

        return changed;
    }

    public void setFollowersCount(long value) {
        this.followersCount = Math.max(0L, value);
        updatePeak();
    }
    public void setLastSeen(long time) { this.lastSeen = time; }

    public void incrementFollowing() { this.followingCount++; }
    public void decrementFollowing() { if (this.followingCount > 0) this.followingCount--; }

    public void incrementPosts() { this.postsCount++; }
    public void decrementPosts() { if (this.postsCount > 0) this.postsCount--; }

    public void addActivity(double amount) { this.activityPool += amount; }

    public void addTrend(double amount) {
        double min = Config.MIN_TREND.get();
        double max = Config.MAX_TREND.get();
        this.trend = Math.max(min, Math.min(max, this.trend + amount));
    }

    public void scaleTrend(double factor) {
        this.trend *= factor;
        if (Math.abs(this.trend) < 0.005) this.trend = 0.0;
    }

    public void consumeActivity(double amount) { this.activityPool = Math.max(0.0, this.activityPool - amount); }
    public void addPostSpam(double amount) { this.postSpamLevel = Math.min(100.0, this.postSpamLevel + amount); }
    public void decayPostSpam(double amount) { this.postSpamLevel = Math.max(0.0, this.postSpamLevel - amount); }

    private void updatePeak() {
        long total = this.followersCount + this.permanentFollowers;
        if (total > this.peakFollowers) this.peakFollowers = total;
    }

    public long applyFollowerLoss(long loss) {
        if (loss <= 0L) return 0L;
        long total = this.followersCount + this.permanentFollowers;
        double floorRatio = 1.0 - Config.EXODUS_MAX_LOSS_RATIO.get();
        long minTotal = (long) Math.ceil(this.peakFollowers * floorRatio);
        long room = Math.max(0L, total - minTotal);
        long actual = Math.min(loss, Math.min(this.followersCount, room));
        this.followersCount -= actual;
        return actual;
    }

    public void addPendingFollowers(double amount) {
        this.pendingFollowers += amount;

        if (this.pendingFollowers >= 1.0) {
            long gain = (long) this.pendingFollowers;
            this.pendingFollowers -= gain;

            if (Config.ENABLE_PERMANENT_FOLLOWERS.get()) {
                double chance = Config.PERMANENT_FOLLOWER_CHANCE.get();
                long permGain = 0;
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                if (gain <= 64L) {
                    for (long i = 0; i < gain; i++) {
                        if (rnd.nextDouble() < chance) permGain++;
                    }
                } else {
                    double expected = gain * chance;
                    permGain = (long) Math.floor(expected);
                    if (rnd.nextDouble() < expected - permGain) permGain++;
                }
                this.permanentFollowers += permGain;
                this.followersCount += (gain - permGain);
            } else {
                this.followersCount += gain;
            }
            updatePeak();
        } else if (this.pendingFollowers <= -1.0) {
            long loss = (long) Math.abs(this.pendingFollowers);
            applyFollowerLoss(loss);
            this.pendingFollowers += loss;
        }
    }

    public void decayTrend(double amount) {
        if (this.trend > 0) this.trend = Math.max(0.0, this.trend - amount);
        else if (this.trend < 0) this.trend = Math.min(0.0, this.trend + amount);
    }

    public void startExodus(double pool, double tauSec, long now) {
        this.exodusPool = Math.max(0.0, pool);
        this.exodusTau = tauSec;
        this.lastExodusTime = now;
    }

    public void consumeExodus(double amount) {
        this.exodusPool = Math.max(0.0, this.exodusPool - amount);
        if (this.exodusPool < 0.01) this.exodusPool = 0.0;
    }

    public void endExodus() { this.exodusPool = 0.0; }
}
