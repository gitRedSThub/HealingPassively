package me.redst.healingPassively;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

 
public final class HealingCommand implements TabExecutor {

    public static final String PERMISSION = "healingpassively.admin";

    private static final NamedTextColor TITLE = NamedTextColor.GOLD;
    private static final NamedTextColor LABEL = NamedTextColor.GRAY;
    private static final NamedTextColor VALUE = NamedTextColor.WHITE;
    private static final NamedTextColor OK = NamedTextColor.GREEN;
    private static final NamedTextColor ERROR = NamedTextColor.RED;
    private static final NamedTextColor USAGE = NamedTextColor.YELLOW;

    private final HealingPassively plugin;

    public HealingCommand(HealingPassively plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text(
                    "You do not have permission to use HealingPassively commands.", ERROR));
            return true;
        }

 
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> sendHelp(sender, label);
            case "reload" -> reload(sender);
            case "get", "info" -> getInfo(sender, label, args);
            case "set" -> set(sender, label, args);
            case "whitelist" -> whitelist(sender, label, args);
            default -> {
                sender.sendMessage(Component.text("Unknown subcommand: " + args[0], ERROR));
                sender.sendMessage(Component.text("Try /" + label + " help", USAGE));
            }
        }
        return true;
    }

 
 
 

    private void reload(CommandSender sender) {
        List<String> problems = plugin.reloadSettings();
        if (problems.isEmpty()) {
            sender.sendMessage(Component.text(
                    "HealingPassively configuration reloaded successfully.", OK));
            return;
        }

        sender.sendMessage(Component.text(
                "HealingPassively configuration reloaded with problems:", ERROR));
        for (String problem : problems) {
            sender.sendMessage(Component.text("  - " + problem, ERROR));
        }
        sender.sendMessage(Component.text(
                "Invalid values were ignored and the previous values kept.", LABEL));
    }

    private void getInfo(CommandSender sender, String label, String[] args) {
        boolean bareInfo = args[0].equalsIgnoreCase("info") && args.length == 1;
        boolean getInfo = args.length >= 2 && args[1].equalsIgnoreCase("info");
        if (!bareInfo && !getInfo) {
            sender.sendMessage(Component.text("Usage: /" + label + " get info", USAGE));
            return;
        }

        PluginConfig config = plugin.settings();
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("=== HealingPassively ===", TITLE));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Status:", LABEL));
        sender.sendMessage(line("  Enabled: ", plugin.isHealingRunning() ? "YES" : "NO"));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Healing:", LABEL));
        sender.sendMessage(line("  Amount: ", format(config.getHealAmount()) + " HP"));
        sender.sendMessage(line("  Timer: ", config.getTimerSeconds() + " seconds"));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Search:", LABEL));
        sender.sendMessage(line("  Distance: ", format(config.getDistance()) + " blocks"));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Whitelist:", LABEL));
        sender.sendMessage(line("  Enabled: ", config.isWhitelistEnabled() ? "YES" : "NO"));
        sender.sendMessage(line("  Entities: ", String.valueOf(config.getWhitelistSize())));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Use:", LABEL));
        sender.sendMessage(Component.text("  /" + label + " help", USAGE));
        sender.sendMessage(Component.empty());
    }

    private void set(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(
                    "Usage: /" + label + " set <distance|timer|heal-amount|if-in-whitelist> <value>", USAGE));
            return;
        }

        String setting = args[1].toLowerCase(Locale.ROOT);
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /" + label + " set " + setting + " <value>", USAGE));
            return;
        }
        String raw = args[2];

        switch (setting) {
            case "distance" -> {
                Double value = parseDouble(sender, raw);
                if (value == null) {
                    return;
                }
                if (value <= 0.0D) {
                    sender.sendMessage(Component.text("Distance must be greater than 0.", ERROR));
                    return;
                }
                plugin.settings().setDistance(value);
                sender.sendMessage(Component.text(
                        "Search distance set to " + format(value) + " blocks.", OK));
            }
            case "timer" -> {
                Integer value = parseInt(sender, raw);
                if (value == null) {
                    return;
                }
                if (value < 1) {
                    sender.sendMessage(Component.text("Timer must be at least 1 second.", ERROR));
                    return;
                }
                plugin.settings().setTimerSeconds(value);
                plugin.restartHealingTask();
                sender.sendMessage(Component.text(
                        "Healing timer set to " + value + " seconds.", OK));
            }
            case "heal-amount", "healamount" -> {
                Double value = parseDouble(sender, raw);
                if (value == null) {
                    return;
                }
                if (value <= 0.0D) {
                    sender.sendMessage(Component.text("Heal amount must be greater than 0.", ERROR));
                    return;
                }
                plugin.settings().setHealAmount(value);
                sender.sendMessage(Component.text(
                        "Heal amount set to " + format(value) + " HP per cycle.", OK));
            }
            case "if-in-whitelist", "in-whitelist" -> {
                Boolean value = parseBoolean(sender, raw);
                if (value == null) {
                    return;
                }
                plugin.settings().setWhitelistEnabled(value);
                sender.sendMessage(Component.text("Whitelist mode "
                        + (value ? "ENABLED. Only whitelisted entities are healed."
                        : "DISABLED. Every valid living entity in range can be healed."), OK));
            }
            default -> {
                sender.sendMessage(Component.text("Unknown setting: " + args[1], ERROR));
                sender.sendMessage(Component.text(
                        "Available: distance, timer, heal-amount, if-in-whitelist", USAGE));
            }
        }
    }

    private void whitelist(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(
                    "Usage: /" + label + " whitelist <add|remove|list> [entity]", USAGE));
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "list" -> whitelistList(sender);
            case "add" -> {
                EntityType type = parseEntity(sender, label, args, "add");
                if (type == null) {
                    return;
                }
                String name = EntityNames.displayName(type);
                if (plugin.settings().addToWhitelist(type)) {
                    sender.sendMessage(Component.text(name + " was added to the whitelist.", OK));
                } else {
                    sender.sendMessage(Component.text(name + " is already in the whitelist.", USAGE));
                }
            }
            case "remove" -> {
                EntityType type = parseEntity(sender, label, args, "remove");
                if (type == null) {
                    return;
                }
                String name = EntityNames.displayName(type);
                if (plugin.settings().removeFromWhitelist(type)) {
                    sender.sendMessage(Component.text(name + " was removed from the whitelist.", OK));
                } else {
                    sender.sendMessage(Component.text(
                            name + " is not currently in the whitelist.", USAGE));
                }
            }
            default -> {
                sender.sendMessage(Component.text("Unknown whitelist action: " + args[1], ERROR));
                sender.sendMessage(Component.text(
                        "Usage: /" + label + " whitelist <add|remove|list> [entity]", USAGE));
            }
        }
    }

    private void whitelistList(CommandSender sender) {
        PluginConfig config = plugin.settings();
        List<EntityType> entries = config.getWhitelistSorted();

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("=== HealingPassively Whitelist ===", TITLE));
        sender.sendMessage(Component.empty());
        if (entries.isEmpty()) {
            sender.sendMessage(Component.text("The whitelist is empty.", LABEL));
        } else {
            for (EntityType type : entries) {
                sender.sendMessage(Component.text(EntityNames.displayName(type), VALUE));
            }
        }
        sender.sendMessage(Component.empty());
        sender.sendMessage(line("Total: ", entries.size() + " entities"));
        sender.sendMessage(line("Whitelist mode: ",
                config.isWhitelistEnabled() ? "ENABLED" : "DISABLED"));
        sender.sendMessage(Component.empty());
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("=== HealingPassively Commands ===", TITLE));
        sender.sendMessage(Component.empty());
        help(sender, "/" + label + " set distance <amount>",
                "Set how far around players entities are checked.");
        help(sender, "/" + label + " set timer <seconds>",
                "Set how often the healing scan runs.");
        help(sender, "/" + label + " set heal-amount <amount>",
                "Set how much health each entity receives per cycle.");
        help(sender, "/" + label + " set if-in-whitelist <true/false>",
                "Enable or disable whitelist filtering.");
        help(sender, "/" + label + " whitelist add <entity>",
                "Add an entity to the whitelist.");
        help(sender, "/" + label + " whitelist remove <entity>",
                "Remove an entity from the whitelist.");
        help(sender, "/" + label + " whitelist list",
                "Show the current whitelist.");
        help(sender, "/" + label + " get info",
                "Show the current plugin configuration.");
        help(sender, "/" + label + " reload",
                "Reload the configuration from disk.");
        help(sender, "/" + label + " help",
                "Show this help message.");
        sender.sendMessage(Component.empty());
    }

 
 
 

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return filter(List.of("set", "whitelist", "get", "reload", "help"), args[0]);
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "set" -> filter(List.of("distance", "timer", "heal-amount", "if-in-whitelist"), args[1]);
                case "whitelist" -> filter(List.of("add", "remove", "list"), args[1]);
                case "get" -> filter(List.of("info"), args[1]);
                default -> List.of();
            };
        }

        if (args.length == 3) {
            String first = args[0].toLowerCase(Locale.ROOT);
            String second = args[1].toLowerCase(Locale.ROOT);
            if (first.equals("set")) {
                return switch (second) {
                    case "distance" -> filter(List.of("16", "32", "64", "128"), args[2]);
                    case "timer" -> filter(List.of("1", "5", "10", "30", "60"), args[2]);
                    case "heal-amount", "healamount" -> filter(List.of("1", "2", "4", "10"), args[2]);
                    case "if-in-whitelist", "in-whitelist" -> filter(List.of("true", "false"), args[2]);
                    default -> List.of();
                };
            }
            if (first.equals("whitelist")) {
                if (second.equals("add")) {
                    List<String> options = new ArrayList<>();
                    for (String name : EntityNames.livingTypeNames()) {
                        EntityType type = EntityNames.resolve(name);
                        if (type != null && !plugin.settings().isWhitelisted(type)) {
                            options.add(name);
                        }
                    }
                    return filter(options, args[2]);
                }
                if (second.equals("remove")) {
                    List<String> options = new ArrayList<>();
                    for (EntityType type : plugin.settings().getWhitelistSorted()) {
                        options.add(EntityNames.configName(type));
                    }
                    return filter(options, args[2]);
                }
            }
        }

        return List.of();
    }

 
 
 

    private EntityType parseEntity(CommandSender sender, String label, String[] args, String action) {
        if (args.length < 3) {
            sender.sendMessage(Component.text(
                    "Usage: /" + label + " whitelist " + action + " <entity>", USAGE));
            return null;
        }
 
        String input = String.join("_", List.of(args).subList(2, args.length));
        EntityType type = EntityNames.resolve(input);
        if (type == null) {
            sender.sendMessage(Component.text("There is no entity called " + input + ".", ERROR));
            sender.sendMessage(Component.text(
                    "Example: /" + label + " whitelist " + action + " cow", USAGE));
        }
        return type;
    }

    private Double parseDouble(CommandSender sender, String raw) {
        try {
            double value = Double.parseDouble(raw);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                sender.sendMessage(Component.text(raw + " is not a valid number.", ERROR));
                return null;
            }
            return value;
        } catch (NumberFormatException ignored) {
            sender.sendMessage(Component.text(raw + " is not a valid number.", ERROR));
            return null;
        }
    }

    private Integer parseInt(CommandSender sender, String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(Component.text(raw + " is not a whole number.", ERROR));
            return null;
        }
    }

    private Boolean parseBoolean(CommandSender sender, String raw) {
        if (raw.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (raw.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        sender.sendMessage(Component.text("Please use true or false.", ERROR));
        return null;
    }

    private void help(CommandSender sender, String usage, String description) {
        sender.sendMessage(Component.text(usage, USAGE));
        sender.sendMessage(Component.text("  " + description, LABEL));
    }

    private Component line(String label, String value) {
        return Component.text(label, LABEL).append(Component.text(value, VALUE));
    }

    private static List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }

     
    private static String format(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
