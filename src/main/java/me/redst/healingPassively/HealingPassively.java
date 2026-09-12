package me.redst.healingPassively;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

 
public final class HealingPassively extends JavaPlugin {

    private final PluginConfig settings = new PluginConfig(this);
    private BukkitTask healingTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reportProblems(settings.load());

        PluginCommand command = getCommand("healingpassively");
        if (command != null) {
            HealingCommand executor = new HealingCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().severe("Command 'healingpassively' is missing from plugin.yml.");
        }

        startHealingTask();
        getLogger().info("Healing every " + settings.getTimerSeconds() + "s within "
                + settings.getDistance() + " blocks of each player.");
    }

    @Override
    public void onDisable() {
        stopHealingTask();
    }

    public PluginConfig settings() {
        return settings;
    }

     
    public List<String> reloadSettings() {
        List<String> problems = settings.load();
        reportProblems(problems);
        restartHealingTask();
        return problems;
    }

    public boolean isHealingRunning() {
        return healingTask != null && !healingTask.isCancelled();
    }

    public void restartHealingTask() {
        stopHealingTask();
        startHealingTask();
    }

    private void startHealingTask() {
        long period = Math.max(1L, settings.getTimerSeconds() * 20L);
        healingTask = getServer().getScheduler().runTaskTimer(this, new HealingTask(this), period, period);
    }

    private void stopHealingTask() {
        if (healingTask != null) {
            healingTask.cancel();
            healingTask = null;
        }
    }

    private void reportProblems(List<String> problems) {
        for (String problem : problems) {
            getLogger().warning(problem);
        }
    }
}
