package me.redst.healingPassively;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

 
public final class PluginConfig {

    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_TIMER = "timer";
    public static final String KEY_HEAL_AMOUNT = "heal-amount";
    public static final String KEY_IN_WHITELIST = "in-whitelist";
    public static final String KEY_WHITELIST = "white list";

    private static final double DEFAULT_DISTANCE = 64.0D;
    private static final int DEFAULT_TIMER = 10;
    private static final double DEFAULT_HEAL_AMOUNT = 1.0D;
    private static final boolean DEFAULT_IN_WHITELIST = true;

    private final HealingPassively plugin;

    private double distance = DEFAULT_DISTANCE;
    private int timerSeconds = DEFAULT_TIMER;
    private double healAmount = DEFAULT_HEAL_AMOUNT;
    private boolean whitelistEnabled = DEFAULT_IN_WHITELIST;
    private final Set<EntityType> whitelist = new LinkedHashSet<>();

    public PluginConfig(HealingPassively plugin) {
        this.plugin = plugin;
    }

     
    public List<String> load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        List<String> problems = new ArrayList<>();

        double rawDistance = config.getDouble(KEY_DISTANCE, DEFAULT_DISTANCE);
        if (rawDistance > 0.0D) {
            distance = rawDistance;
        } else {
            problems.add("'" + KEY_DISTANCE + "' must be greater than 0 (found " + rawDistance
                    + "), keeping " + distance + ".");
        }

        int rawTimer = config.getInt(KEY_TIMER, DEFAULT_TIMER);
        if (rawTimer >= 1) {
            timerSeconds = rawTimer;
        } else {
            problems.add("'" + KEY_TIMER + "' must be at least 1 second (found " + rawTimer
                    + "), keeping " + timerSeconds + ".");
        }

        double rawHeal = config.getDouble(KEY_HEAL_AMOUNT, DEFAULT_HEAL_AMOUNT);
        if (rawHeal > 0.0D) {
            healAmount = rawHeal;
        } else {
            problems.add("'" + KEY_HEAL_AMOUNT + "' must be greater than 0 (found " + rawHeal
                    + "), keeping " + healAmount + ".");
        }

        whitelistEnabled = config.getBoolean(KEY_IN_WHITELIST, DEFAULT_IN_WHITELIST);

        whitelist.clear();
        for (String name : config.getStringList(KEY_WHITELIST)) {
            EntityType type = EntityNames.resolve(name);
            if (type == null) {
                problems.add("Unknown entity '" + name + "' in '" + KEY_WHITELIST + "', ignoring it.");
            } else {
                whitelist.add(type);
            }
        }

        return problems;
    }

    public double getDistance() {
        return distance;
    }

    public int getTimerSeconds() {
        return timerSeconds;
    }

    public double getHealAmount() {
        return healAmount;
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    public boolean isWhitelisted(EntityType type) {
        return whitelist.contains(type);
    }

    public int getWhitelistSize() {
        return whitelist.size();
    }

     
    public List<EntityType> getWhitelistSorted() {
        List<EntityType> sorted = new ArrayList<>(whitelist);
        sorted.sort(Comparator.comparing(EntityNames::displayName));
        return sorted;
    }

    public void setDistance(double value) {
        distance = value;
        write(KEY_DISTANCE, tidy(value));
    }

    public void setTimerSeconds(int value) {
        timerSeconds = value;
        write(KEY_TIMER, value);
    }

    public void setHealAmount(double value) {
        healAmount = value;
        write(KEY_HEAL_AMOUNT, tidy(value));
    }

    public void setWhitelistEnabled(boolean value) {
        whitelistEnabled = value;
        write(KEY_IN_WHITELIST, value);
    }

     
    public boolean addToWhitelist(EntityType type) {
        if (!whitelist.add(type)) {
            return false;
        }
        saveWhitelist();
        return true;
    }

     
    public boolean removeFromWhitelist(EntityType type) {
        if (!whitelist.remove(type)) {
            return false;
        }
        saveWhitelist();
        return true;
    }

    private void saveWhitelist() {
        List<String> names = new ArrayList<>(whitelist.size());
        for (EntityType type : getWhitelistSorted()) {
            names.add(EntityNames.configName(type));
        }
        write(KEY_WHITELIST, names);
    }

     
    private static Object tidy(double value) {
        return value == Math.floor(value) ? (Object) (long) value : (Object) value;
    }

     
    private void write(String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
    }
}
