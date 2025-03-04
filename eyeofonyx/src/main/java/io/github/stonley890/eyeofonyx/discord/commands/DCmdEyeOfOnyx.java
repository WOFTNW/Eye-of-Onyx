package io.github.stonley890.eyeofonyx.discord.commands;

import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.PlayerTribe;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.data.Tribe;
import io.github.stonley890.dreamvisitor.discord.commands.DiscordCommand;
import io.github.stonley890.eyeofonyx.EyeOfOnyx;
import io.github.stonley890.eyeofonyx.files.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class DCmdEyeOfOnyx implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("eyeofonyx", "Manage Eye of Onyx.")
                .addSubcommands(
                        new SubcommandData("ban", "Disallow a player from participating in royalty.")
                                .addOption(OptionType.USER, "user", "The user to set.", true, false),
                        new SubcommandData("unban", "Allow a previously disallowed player to participate in royalty.")
                                .addOption(OptionType.USER, "user", "The user to set.", true, false),
                        new SubcommandData("banlist", "List all banned players."),
                        new SubcommandData("freeze", "Toggle the freezing functionality.")
                ).setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        String subCommand = event.getSubcommandName();

        if (subCommand == null) {
            event.reply("You must specify a subcommand!").setEphemeral(true).queue();
            return;
        }

        switch (subCommand) {
            case "ban" -> {

                User targetUser;

                try {
                    targetUser = Objects.requireNonNull(event.getOption("user")).getAsUser();
                } catch (NullPointerException e) {
                    event.reply("Missing arguments!").queue();
                    return;
                }

                UUID uuid = AccountLink.getUuid(targetUser.getIdLong());

                if /* the username is invalid */ (uuid == null) {
                    event.reply("That user could not be found.").queue();
                } else /* the username is valid */ {
                    if /* player is already banned */ (Banned.isPlayerBanned(uuid)) {
                        event.reply("That player is already banned.").queue();
                    } else /* player is not yet banned */ {

                        // Add player to ban list, remove them from the royalty board, and send them a notification.
                        Banned.addPlayer(uuid);
                        Tribe tribe = PlayerTribe.getTribeOfPlayer(uuid);

                        int pos = RoyaltyBoard.getPositionIndexOfUUID(uuid);

                        if (pos != -1 && pos != RoyaltyBoard.CIVILIAN) {

                            // Remove any challenges
                            Challenge.removeChallengesOfPlayer(uuid, "The player who was challenging you was removed from the royalty board, so your challenge was canceled.");

                            if (tribe != null) {
                                BoardState oldBoard = RoyaltyBoard.getBoardOf(tribe);
                                RoyaltyBoard.removePlayer(tribe, pos, true);
                                RoyaltyBoard.updateBoard(tribe, false, false);
                                RoyaltyBoard.reportChange(new RoyaltyAction(event.getUser().getId(), "Player was banned.", tribe, oldBoard, RoyaltyBoard.getBoardOf(tribe)));
                                RoyaltyBoard.updateDiscordBoard(tribe);
                            }

                        }
                        new Notification(uuid, "Royalty Ban", "You are no longer allowed to participate in royalty. Contact staff if you think this is a mistake.", Notification.Type.GENERIC).create();
                        event.reply(targetUser.getAsMention() + " has been banned.").queue();

                    }
                }

            }
            case "unban" -> {

                User targetUser;

                try {
                    targetUser = Objects.requireNonNull(event.getOption("user")).getAsUser();
                } catch (NullPointerException e) {
                    event.reply("Missing arguments!").queue();
                    return;
                }

                UUID uuid = AccountLink.getUuid(targetUser.getIdLong());

                if /* the username is invalid */ (uuid == null) {
                    event.reply("That user could not be found.").queue();
                } else /* the username is valid */ {
                    if /* player is banned */ (Banned.isPlayerBanned(uuid)) {
                        Banned.removePlayer(uuid);
                        event.reply(targetUser.getAsMention() + " has been unbanned.").queue();
                    } else /* player is not banned */ {
                        event.reply("That player is not banned.").queue();
                    }
                }

            }
            case "banlist" -> {

                event.deferReply().queue();

                Bukkit.getScheduler().runTaskAsynchronously(EyeOfOnyx.getPlugin(), () -> {

                    EmbedBuilder message = new EmbedBuilder();
                    message.setTitle("Banned players:\n");

                    StringBuilder description = new StringBuilder();

                    for (String bannedPlayer : Banned.getBannedPlayers()) {

                        String player;

                        String username = PlayerUtility.getUsernameOfUuid(bannedPlayer);
                        Long discordId = null;
                        try {
                            discordId = AccountLink.getDiscordId(UUID.fromString(bannedPlayer));
                        } catch (NullPointerException ignored) {}

                        description.append("`").append(username).append("`");
                        if (discordId != null) description.append("<@").append(discordId).append(">");
                        description.append("\n");
                    }
                    message.setDescription(description);
                    event.getHook().editOriginalEmbeds(message.build()).queue();
                });

            }
            case "freeze" -> {

                if (RoyaltyBoard.isBoardFrozen()) {
                    RoyaltyBoard.setFrozen(false);
                    event.reply("The royalty board is now unfrozen.").queue();
                } else {
                    RoyaltyBoard.setFrozen(true);
                    event.reply("The royalty board is now frozen.").queue();
                }

            }
        }
    }
}
