package io.github.stonley890.eyeofonyx.commands.tabcomplete;

import io.github.stonley890.eyeofonyx.EyeOfOnyx;
import io.github.stonley890.eyeofonyx.files.Banned;
import io.github.stonley890.eyeofonyx.files.RoyaltyBoard;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shanerx.mojang.Mojang;

import java.util.ArrayList;
import java.util.List;

public class TabEyeOfOnyx implements TabCompleter {

    List<String> suggestions = new ArrayList<>();

    Mojang mojang = new Mojang().connect();
    EyeOfOnyx plugin = EyeOfOnyx.getPlugin();

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Clear previous suggestions or create a new ArrayList
        if (suggestions != null)  suggestions.clear();
        else suggestions = new ArrayList<>();

        if (args.length == 1) {

            // eyeofonyx <ban|unban|freeze|disable|config>
            if (sender.hasPermission("eyeofonyx.ban")) {
                suggestions.add("ban");
                suggestions.add("unban");
            }
            if (sender.isOp()) {
                suggestions.add("freeze");
                suggestions.add("disable");
                suggestions.add("config");
            }

        } else if (args.length == 2) {

            // eyeofonyx <ban> <player>
            if (args[0].equals("ban")) {
                if (sender.hasPermission("eyeofonyx.ban"))
                    for (Player player : Bukkit.getOnlinePlayers())
                        suggestions.add(player.getName());


            } // eyeofonyx <unban> <player>
            else if (args[0].equals("unban")) {
                if (sender.hasPermission("eyeofonyx.ban"))
                    for (String bannedPlayer : Banned.getBannedPlayers()) {
                        String name = mojang.getPlayerProfile(bannedPlayer).getUsername();
                        suggestions.add(name);
                    }

            } // eyeofonyx config <key>
            else if (args[0].equals("config")) {

                if (sender.isOp()) {
                    suggestions.add("challenge-cool-down");
                    suggestions.add("challenge-acknowledgement-time");
                    suggestions.add("challenge-time-period");
                    suggestions.add("time-selection-period");
                    suggestions.add("inactivity-timer");
                    suggestions.add("waiting-rooms");
                }
            }
        } else if (args.length == 3) {

            if (sender.isOp())
                // eyeofonyx config waiting-rooms <tribe>
                if (args[1].equals("waiting-rooms")) {
                    suggestions.addAll(List.of(RoyaltyBoard.getTribes()));
                }

        } else if (args.length == 4) {

            if (sender.isOp())
                if (args[0].equals("config") && args[1].equals("waiting-rooms")) {
                    if (sender instanceof Player player) {
                        player.getLocation().getX();
                    }
                }
        }  else if (args.length == 5) {

            if (sender.isOp())
                if (args[0].equals("config") && args[1].equals("waiting-rooms")) {
                    if (sender instanceof Player player) {
                        player.getLocation().getY();
                    }
                }
        }  else if (args.length == 6) {

            if (sender.isOp())
                if (args[0].equals("config") && args[1].equals("waiting-rooms")) {
                    if (sender instanceof Player player) {
                        player.getLocation().getZ();
                    }
                }
        }  else if (args.length == 7) {

            if (sender.isOp())
                if (args[0].equals("config") && args[1].equals("waiting-rooms")) {
                    for (World world : Bukkit.getWorlds()) {
                        suggestions.add(world.getName());
                    }
                }
        }

        return suggestions;
    }
}