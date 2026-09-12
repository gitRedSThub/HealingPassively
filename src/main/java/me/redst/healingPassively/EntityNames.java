package me.redst.healingPassively;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

 
public final class EntityNames {

     
    private static final Map<String, List<String>> ALIASES = Map.ofEntries(
            Map.entry("mooshroom", List.of("MOOSHROOM", "MUSHROOM_COW")),
            Map.entry("mushroom_cow", List.of("MOOSHROOM", "MUSHROOM_COW")),
            Map.entry("snow_golem", List.of("SNOW_GOLEM", "SNOWMAN")),
            Map.entry("snowman", List.of("SNOW_GOLEM", "SNOWMAN")),
            Map.entry("zombie_pigman", List.of("ZOMBIFIED_PIGLIN")),
            Map.entry("iron_golem", List.of("IRON_GOLEM")),
            Map.entry("tropicalfish", List.of("TROPICAL_FISH"))
    );

    private EntityNames() {
    }

     
    public static EntityType resolve(String input) {
        if (input == null) {
            return null;
        }
        String key = normalise(input);
        if (key.isEmpty()) {
            return null;
        }

        for (String candidate : ALIASES.getOrDefault(key, List.of(key.toUpperCase(Locale.ROOT)))) {
            EntityType type = byEnumName(candidate);
            if (type != null) {
                return type;
            }
        }

 
        for (EntityType type : EntityType.values()) {
            if (type == EntityType.UNKNOWN) {
                continue;
            }
            try {
                if (type.getKey().getKey().equals(key)) {
                    return type;
                }
            } catch (IllegalArgumentException | UnsupportedOperationException ignored) {
 
            }
        }
        return null;
    }

     
    public static String configName(EntityType type) {
        return type.name().toLowerCase(Locale.ROOT);
    }

     
    public static String displayName(EntityType type) {
        String[] words = type.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word, 1, word.length());
        }
        return out.toString();
    }

     
    public static List<String> livingTypeNames() {
        List<String> names = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            if (type == EntityType.UNKNOWN) {
                continue;
            }
            Class<?> clazz = type.getEntityClass();
            if (clazz != null && LivingEntity.class.isAssignableFrom(clazz)) {
                names.add(configName(type));
            }
        }
        Collections.sort(names);
        return names;
    }

    private static String normalise(String input) {
        String key = input.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        if (key.startsWith("minecraft:")) {
            key = key.substring("minecraft:".length());
        }
        return key;
    }

    private static EntityType byEnumName(String enumName) {
        try {
            EntityType type = EntityType.valueOf(enumName);
            return type == EntityType.UNKNOWN ? null : type;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
