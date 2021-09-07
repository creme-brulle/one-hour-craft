package com.github.kuruppa.onehour;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;


public final class OneHour extends JavaPlugin {

    private int totalTimeSeconds = 3600;
    private BukkitTask task;

    private BossBar bar;
    private Collection<? extends Player> players;

    String[] onEnableMessages = {
            "   LLLL     EEEEEEEE  OOOOOO",
            "   L  L     E      E OO    OO",
            "   L  L     E  EEEEE O  OO  O",
            "   L  L     E      E O O  O O",
            "   L  LLLLL E  EEEEE O  OO  O",
            "   L      L E      E OO    OO",
            "   LLLLLLLL EEEEEEEE  OOOOOO",
            "============================================",
            "* countdown timer for 1 hour building game.",
            "============================================"
    };

    @Override
    public void onEnable() {
        // Plugin startup logic
        Arrays.stream(onEnableMessages).forEach(getLogger()::info);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("plugin is disable");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "not plugin");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName();

        if(!(cmd.equalsIgnoreCase("oh"))) {
            return true;
        }

        String subCmd = args[0];
        if(subCmd.equalsIgnoreCase("start")) {
            return onStartCommand(player);
        }

        if(subCmd.equalsIgnoreCase("stop")) {
            return onStopCommand();
        }

        if(subCmd.equalsIgnoreCase("set")) {
            this.totalTimeSeconds = Integer.parseInt(args[1]);
            player.sendMessage(ChatColor.GREEN + "タイマーが" + totalTimeSeconds + "秒に設定されました。");
            return true;
        }

        return true;
    }

    private boolean onStartCommand(Player player) {

        if (bar != null) {
            player.sendMessage(ChatColor.RED
                    + "this command has already been executed.");
            return true;
        }
        players = getServer().getOnlinePlayers();

        bar = createTimeBar();

        players.forEach((p)->{
            bar.addPlayer(p);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
            p.sendMessage(ChatColor.GREEN + "カウントダウン開始");
        });

        countdownTask(totalTimeSeconds);

        return true;
    }

    private boolean onStopCommand() {
        if(!task.isCancelled()) {
            task.cancel();
            bar.setVisible(false);
            bar = null;
            players.forEach((p)->
                    p.sendMessage(ChatColor.RED + "カウントダウンを中止しました。"));
            return true;
        }
        players.forEach((p)->
                p.sendMessage(ChatColor.RED + "カウントダウンが開始されていません。"));
        return true;
    }

    private void countdownTask(int second) {

        final Runnable runnable = new BukkitRunnable() {
            int timeLeftSec = second;

            public void run() {

                String dispHMS = parseHHMMSS(timeLeftSec);
                bar.setTitle(ChatColor.WHITE + dispHMS);

                double dp = (double) timeLeftSec / (double) second;
                bar.setProgress(dp);

                alertMessage(timeLeftSec);

                shutDown(timeLeftSec);

                timeLeftSec--;
            }
        };
        task = this.getServer().getScheduler().runTaskTimer(this, runnable, 0L, 20L);
    }

    private void alertMessage(int timeLeftSec){
        if (timeLeftSec <= 5) {
            players.forEach((p)-> {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
                p.sendMessage(ChatColor.RED + "残り" + timeLeftSec + "秒...");
            });
            return;
        }

        if (timeLeftSec == 60) {
            players.forEach((p)-> {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
                p.sendMessage(ChatColor.RED + "残り" + timeLeftSec + "秒...");
            });
        }
    }

    private void shutDown(int timeLeftSec) {
        if (timeLeftSec <= 0) {
            players.forEach(p-> p.kickPlayer(ChatColor.GREEN + "建築終了です。おつかれさまでした。"));
            task.cancel();
            bar.setVisible(false);
            bar = null;
        }
    }

    private String parseHHMMSS(int second){
        long millis = second * 1000L;
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private BossBar createTimeBar() {
        BossBar bar = getServer().createBossBar(
                ChatColor.WHITE + "テスト"
                , BarColor.RED
                , BarStyle.SOLID
        );
        bar.setVisible(true);
        return bar;
    }

}
