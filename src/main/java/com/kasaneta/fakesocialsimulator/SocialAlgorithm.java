package com.kasaneta.fakesocialsimulator;

import java.util.concurrent.ThreadLocalRandom;

public final class SocialAlgorithm {

    private SocialAlgorithm() {}

    private static final double REFERENCE_DIFFICULTY = 3.5;
    private static final double MOOD_TIME_CONSTANT = 1800.0;
    private static final double ACTIVITY_TIME_CONSTANT = 90.0;
    private static final double SLOW_INTERVAL_SEC = 5.0;
    private static final long EXODUS_COOLDOWN_MS = 6L * 3600L * 1000L;

    private static double algoMood = 1.0;
    private static long lastCycleMs = 0L;

    private static double chaos = 1.0;
    private static double difficulty = REFERENCE_DIFFICULTY;
    private static double churnRate = 1.0;
    private static boolean punishSpammers = true;
    private static boolean enablePassive = true;
    private static int passiveStart = 25000;
    private static int passiveFull = 95000;
    private static double passiveMultiplier = 1.0;
    private static double hypeHalfLifeEngaged = 40.0;
    private static double hypeHalfLifeIdle = 20.0;
    private static boolean exodusEnabled = true;
    private static double exodusChancePerHour = 0.0005;
    private static double exodusMaxLoss = 0.80;

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static long randRound(double x) {
        if (x <= 0.0) return 0L;
        long f = (long) x;
        return f + (ThreadLocalRandom.current().nextDouble() < (x - f) ? 1L : 0L);
    }

    private static double logNoise(ThreadLocalRandom r, double sigma) {
        double s = sigma * chaos;
        return Math.exp(s * r.nextGaussian() - 0.5 * s * s);
    }

    public static void beginCycle(long now) {
        double dt = (lastCycleMs == 0L) ? 0.4 : clamp((now - lastCycleMs) / 1000.0, 0.05, 10.0);
        lastCycleMs = now;

        chaos = Config.RANDOMNESS_STRENGTH.get();
        difficulty = Config.FOLLOWER_DIFFICULTY.get();
        churnRate = Config.NATURAL_CHURN_RATE.get();
        punishSpammers = Config.PUNISH_SPAMMERS.get();
        enablePassive = Config.ENABLE_PASSIVE_POPULARITY.get();
        passiveStart = Config.PASSIVE_START_THRESHOLD.get();
        passiveFull = Config.PASSIVE_FULL_THRESHOLD.get();
        passiveMultiplier = Config.PASSIVE_ACTIVITY_MULTIPLIER.get();
        hypeHalfLifeEngaged = Config.ONLINE_TREND_DECAY.get();
        hypeHalfLifeIdle = Config.OFFLINE_TREND_DECAY.get();
        exodusEnabled = Config.ENABLE_EXODUS.get();
        exodusChancePerHour = Config.EXODUS_CHANCE_PER_HOUR.get();
        exodusMaxLoss = Config.EXODUS_MAX_LOSS_RATIO.get();

        double decay = Math.exp(-dt / MOOD_TIME_CONSTANT);
        double noise = Math.sqrt(1.0 - decay * decay) * 0.14 * chaos * ThreadLocalRandom.current().nextGaussian();
        algoMood = clamp(1.0 + (algoMood - 1.0) * decay + noise, 0.65, 1.45);
    }

    public static double getAlgoMood() { return algoMood; }

    public static double hypeScale(long followers) {
        return 0.3 + 0.7 * Math.min(1.0, followers / 300.0);
    }

    private static double hypeBoost(double hype) {
        if (hype >= 0.0) return 1.0 + 0.35 * Math.sqrt(hype);
        return 1.0 / (1.0 + 0.5 * Math.sqrt(-hype));
    }

    private static double reachBoost(PlayerStats s) {
        return clamp(s.getAppeal() * algoMood * s.getLuck() * hypeBoost(s.getTrend()), 0.1, 12.0);
    }

    private static double audienceBase(long followers) {
        return 2.5 + 0.22 * Math.pow((double) followers, 0.86);
    }

    private static double viewsPerActivity(long followers) {
        return 0.05 + 0.45 * followers / (followers + 300.0);
    }

    private static double suspicion(PlayerStats s) {
        if (!punishSpammers) return 1.0;
        long following = s.getFollowingCount();
        long followers = s.getFollowersCount();
        if (following > 0L && following <= 30L && followers < 100L) return 0.8;
        if (following > 50L && following > followers * 2L) {
            return 1.0 + Math.min(1.2, following * 0.0002);
        }
        return 1.0;
    }

    public static double rewardAction(PlayerStats s, PlayerStats.ActionType type, double baseAmount, long now) {
        double mult = s.registerAction(type, now);
        if (baseAmount > 0.0) {
            double variance = 0.75 + ThreadLocalRandom.current().nextDouble() * 0.50;
            s.addActivity(baseAmount * variance * mult);
        }
        return mult;
    }

    public static void onFollow(PlayerStats s, long now) {
        double mult = s.registerAction(PlayerStats.ActionType.FOLLOW, now);

        long followers = s.getFollowersCount();
        long following = s.getFollowingCount();

        double base = Config.BASE_FOLLOW_BACK_CHANCE.get();
        double min = Config.MIN_FOLLOW_BACK_CHANCE.get();
        double chance = base;
        if (following > 50L && following > followers * 2L) {
            double ratio = (double) following / Math.max(1L, followers);
            chance = Math.max(min, base / Math.log10(ratio + 1.0));
        }

        double coldStart = 0.35 + 0.65 * Math.pow(Math.min(1.0, followers / 60.0), 0.7);
        double trust = 0.3 + 0.7 * Math.min(1.0, s.getPostsCount() / 12.0);
        double difficultyFactor = REFERENCE_DIFFICULTY / Math.max(0.1, Config.FOLLOWER_DIFFICULTY.get());

        chance *= coldStart * trust * difficultyFactor * mult * s.getAppeal();
        chance = clamp(chance, 0.0, 1.0);

        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        long allowed = s.getLikeCount() + 2L - followers;
        if (allowed <= 0L) return;

        s.addPendingFollowers(1.0);
    }

    public static void onPostPublished(PlayerStats s, long now) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        long followers = s.getFollowersCount();

        double y = s.registerPost(now);
        if (y < 0.03) {
            return;
        }

        double hs = hypeScale(followers);
        double boost = reachBoost(s);
        double variety = s.getVarietyScore(now);
        double content = 0.75 + 0.5 * variety;

        double m = Math.exp(0.85 * chaos * r.nextGaussian());
        double burst = 1.0;

        double burstChance = 0.010 * s.getLuck() * algoMood * (0.5 + variety) * y;
        if (r.nextDouble() < burstChance) {
            burst = 5.0 * Math.pow(1.0 / Math.max(0.05, r.nextDouble()), 0.6);
            s.addTrend((1.0 + r.nextDouble() * 3.0) * hs);
        } else if (m < 0.4 && r.nextDouble() < 0.4) {
            s.addTrend(-(0.1 + r.nextDouble() * 0.4) * hs);
        }

        double total = m * burst;
        if (total > 2.0) {
            s.addTrend(0.05 * (total - 2.0) * hs);
        }

        double moment = 1.0;
        if (now - s.getLastAchievementTime() <= 60_000L) {
            moment = s.getMomentBoost();
        }

        double views = audienceBase(followers) * total * content * y * moment * boost;
        s.addPendingViews(views);
        s.blendQuality(clamp(0.6 + 0.4 * Math.sqrt(total), 0.5, 2.2), 0.6);
    }

    public static boolean hasFastWork(PlayerStats s) {
        return s.getPendingViews() > 0.0
                || s.hasPendingLegacyMetrics()
                || s.getActivityPool() > 0.001
                || s.getExodusPool() > 0.0;
    }

    public static boolean hasSlowWork(PlayerStats s) {
        long f = s.getFollowersCount();
        return f > 15L
                || s.getTrend() != 0.0
                || s.getPostSpamLevel() > 0.0
                || s.getDeletionSpamLevel() > 0.0
                || (enablePassive && f >= passiveStart);
    }

    public static boolean runCycle(PlayerStats s, boolean online, long now) {
        if (online != s.isOnlineFlag()) {
            s.setOnlineFlag(online);
            if (online) s.touchActivity(now);
        }

        double dtP = (now - s.getLastProcessTime()) / 1000.0;
        if (!online) {
            boolean fast = hasFastWork(s);
            if (!fast) {
                if (!hasSlowWork(s)) return false;
                if (dtP < SLOW_INTERVAL_SEC) return false;
            }
        }

        s.setLastProcessTime(now);
        dtP = clamp(dtP, 0.05, 30.0);
        if (online) s.setLastSeen(now);

        if (s.processPendingMetrics()) {
            convertViews(s, s.takeDrainedViews());
        }
        processPlayer(s, online, now, dtP);
        return true;
    }

    private static void processPlayer(PlayerStats s, boolean online, long now, double dtP) {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        long followers = s.getFollowersCount();
        double idleSec = Math.max(0.0, (now - (online ? s.getLastActivityTime() : s.getLastSeen())) / 1000.0);
        double idleHours = idleSec / 3600.0;
        boolean engaged = online && idleSec < 300.0;

        s.stepLuck(dtP, chaos);
        s.relaxQuality(dtP);
        s.decayPostSpamTo(now);
        s.decayDeletionSpamTo(now);

        double halfLifeSec = (engaged ? hypeHalfLifeEngaged : hypeHalfLifeIdle) * 60.0;
        if (s.getTrend() != 0.0) {
            s.scaleTrend(Math.pow(0.5, dtP / Math.max(1.0, halfLifeSec)));
        }
        double hype = s.getTrend();
        double boost = reachBoost(s);

        double pool = s.getActivityPool();
        if (pool > 0.0) {
            double consumed = (pool < 0.05) ? pool : pool * (1.0 - Math.exp(-dtP / ACTIVITY_TIME_CONSTANT));
            s.consumeActivity(consumed);
            s.addPendingViews(consumed * viewsPerActivity(followers) * boost);
        }

        if (hype > 0.05) {
            double perMinute = hype * (0.3 + 0.004 * Math.pow((double) followers, 0.9));
            s.addPendingViews(perMinute * (dtP / 60.0) * algoMood * s.getLuck() * s.getAppeal());
        }

        if (enablePassive && followers >= passiveStart) {
            double span = Math.max(1.0, (double) (passiveFull - passiveStart));
            double progress = clamp((followers - passiveStart) / span, 0.0, 1.0);
            double reachShare = 0.02 + 0.98 * Math.pow(progress, 1.2);
            double freshness = 0.15 + 0.85 * Math.exp(-idleHours / 72.0);
            double viewsPerHour = followers * 0.06 * reachShare * passiveMultiplier * freshness * algoMood * s.getLuck();
            s.addPendingViews(viewsPerHour * dtP / 3600.0);

            double waveScale = 0.2 + 0.8 * progress;
            if (r.nextDouble() < dtP / 28800.0) {
                s.addTrend((0.3 + r.nextDouble() * 1.7) * waveScale);
            } else if (punishSpammers && r.nextDouble() < dtP / 43200.0) {
                s.addTrend(-(0.2 + r.nextDouble() * 0.8) * waveScale);
            }
        }

        applyNaturalChurn(s, online, idleSec, dtP, r);
        processExodus(s, now, dtP, r);

        if (s.getLikeCount() > 50L && r.nextDouble() < dtP / 10800.0) {
            long n = randRound(s.getLikeCount() * (0.0005 + r.nextDouble() * 0.002));
            if (n > 0L) s.addPendingLikes(-n);
        }
        if (s.getCommentCount() > 20L && r.nextDouble() < dtP / 21600.0) {
            long n = randRound(s.getCommentCount() * (0.0005 + r.nextDouble() * 0.002));
            if (n > 0L) s.addPendingComments(-n);
        }
    }

    public static void convertViews(PlayerStats s, long views) {
        if (views <= 0L) return;
        ThreadLocalRandom r = ThreadLocalRandom.current();

        long followers = s.getFollowersCount();
        double q = s.getQuality();
        double hype = s.getTrend();

        double early = 1.0 / (1.0 + followers / 30.0);

        double likeRate = 0.065 * (1.0 + 1.5 * early) * q * s.getAppeal() * logNoise(r, 0.25);
        likeRate = clamp(likeRate, 0.01, 0.45);
        long newLikes = randRound(views * likeRate);

        double commentRate = 0.025 * s.getChatty() * (0.7 + 0.3 * q) * logNoise(r, 0.45);
        commentRate = clamp(commentRate, 0.002, 0.15);
        long newComments = randRound(newLikes * commentRate);

        s.addEngagement(newLikes, newComments);

        double trust = 0.3 + 0.7 * Math.min(1.0, s.getPostsCount() / 12.0);
        double coldStart = 0.35 + 0.65 * Math.pow(Math.min(1.0, followers / 60.0), 0.7);
        double rate = 0.014
                * coldStart
                * (REFERENCE_DIFFICULTY / Math.max(0.1, difficulty))
                * trust
                * (0.6 + 0.4 * q)
                * (1.0 + 0.15 * Math.sqrt(Math.max(0.0, hype)))
                / Math.pow(1.0 + followers / 200.0, 0.5)
                / suspicion(s);
        if (hype < 0.0) rate /= 1.0 + (-hype) / 40.0;
        rate *= logNoise(r, 0.35);

        long gain = randRound(views * rate);
        long allowed = Math.max(0L, s.getLikeCount() + 2L - s.getFollowersCount());
        gain = Math.min(gain, allowed);
        gain = Math.min(gain, (long) Math.ceil(views * 0.25));
        if (gain > 0L) {
            s.addPendingFollowers((double) gain);
        }
    }

    private static void applyNaturalChurn(PlayerStats s, boolean online, double idleSec, double dtP, ThreadLocalRandom r) {
        if (churnRate <= 0.0) return;
        long total = s.getFollowersCount();
        if (total <= 15L) return;

        double idleHours = idleSec / 3600.0;
        double idleMult = 1.0 + Math.min(2.0, idleHours / 72.0);
        double presenceMult = (online && idleSec < 600.0) ? 0.35 : 1.0;
        double hype = s.getTrend();
        double hypeMult = (hype >= 0.0)
                ? 1.0 / (1.0 + 0.05 * Math.sqrt(hype))
                : 1.0 + Math.min(4.0, -hype / 20.0);

        double perHour = total * 0.00012 * churnRate * idleMult * presenceMult * hypeMult * s.getVolatility();
        double meanLoss = Math.max(1.0, total * 0.0006);
        double eventsPerSec = perHour / meanLoss / 3600.0;
        if (r.nextDouble() >= eventsPerSec * dtP) return;

        double size = meanLoss * -Math.log(1.0 - r.nextDouble());
        if (r.nextDouble() < 0.03) size *= 3.0 + 5.0 * r.nextDouble();
        s.applyFollowerLoss(Math.max(1L, randRound(size)));
    }

    private static void processExodus(PlayerStats s, long now, double dtP, ThreadLocalRandom r) {
        double pool = s.getExodusPool();
        if (pool > 0.0) {
            double tau = Math.max(300.0, s.getExodusTau());
            double portion = (pool < 1.0) ? pool : pool * (1.0 - Math.exp(-dtP / tau));
            long loss = randRound(portion * logNoise(r, 0.3));
            s.consumeExodus(portion);
            if (loss > 0L) {
                long done = s.applyFollowerLoss(loss);
                if (done < loss) s.endExodus();
            }
            return;
        }

        if (!exodusEnabled) return;
        if (s.getFollowersCount() < 200L) return;
        if (now - s.getLastExodusTime() < EXODUS_COOLDOWN_MS) return;

        double hazard = exodusChancePerHour * (1.0 + Math.max(0.0, -s.getTrend()) / 20.0);
        if (suspicion(s) > 1.0) hazard *= 1.5;
        hazard = Math.min(hazard, 0.05);
        if (r.nextDouble() >= hazard * dtP / 3600.0) return;

        double ratio = Math.min(exodusMaxLoss, 0.03 + (-Math.log(1.0 - r.nextDouble())) * 0.07);
        double tauSec = (60.0 + 300.0 * r.nextDouble()) * 60.0;
        s.startExodus(s.getFollowersCount() * ratio, tauSec, now);
        s.addTrend(-(2.0 + ratio * 30.0));
    }
}
