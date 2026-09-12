package me.redst.healingPassively;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

 
public final class HealingTask implements Runnable {

    private final HealingPassively plugin;

    public HealingTask(HealingPassively plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        PluginConfig config = plugin.settings();
        double distance = config.getDistance();
        double distanceSquared = distance * distance;
        double healAmount = config.getHealAmount();

 
 
        Set<UUID> processed = new HashSet<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            World world = player.getWorld();
            Location center = player.getLocation();

 
 
            Collection<Entity> nearby = world.getNearbyEntities(center, distance, distance, distance,
                    candidate -> candidate instanceof LivingEntity && !(candidate instanceof Player));

            for (Entity entity : nearby) {
 
 
 
                if (center.distanceSquared(entity.getLocation()) > distanceSquared) {
                    continue;
                }
                if (!processed.add(entity.getUniqueId())) {
                    continue;
                }
                if (!(entity instanceof LivingEntity living)) {
                    continue;
                }
                if (living.isDead() || !living.isValid()) {
                    continue;
                }
                if (config.isWhitelistEnabled() && !config.isWhitelisted(living.getType())) {
                    continue;
                }
                heal(living, healAmount);
            }
        }
    }

    private void heal(LivingEntity living, double healAmount) {
        double max = maxHealth(living);
        double current = living.getHealth();

 
        if (current <= 0.0D || current >= max) {
            return;
        }

        try {
            living.setHealth(Math.min(max, current + healAmount));
        } catch (IllegalArgumentException ignored) {
 
        }
    }

     
    @SuppressWarnings("deprecation")
    private double maxHealth(LivingEntity living) {
        AttributeInstance attribute = living.getAttribute(Attribute.MAX_HEALTH);
        return attribute != null ? attribute.getValue() : living.getMaxHealth();
    }
}
