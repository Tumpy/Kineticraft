package net.kineticraft.lostcity.mechanics;

import lombok.Getter;
import net.kineticraft.lostcity.Core;
import net.kineticraft.lostcity.EnumRank;
import net.kineticraft.lostcity.commands.Command;
import net.kineticraft.lostcity.commands.CommandType;
import net.kineticraft.lostcity.commands.misc.CommandGUI;
import net.kineticraft.lostcity.commands.misc.CommandInfo;
import net.kineticraft.lostcity.commands.player.*;
import net.kineticraft.lostcity.commands.staff.*;
import net.kineticraft.lostcity.config.Configs;
import net.kineticraft.lostcity.guis.GUIType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles command usage.
 *
 * Created by Kneesnap on 5/29/2017.
 */
public class Commands extends Mechanic {

    @Getter
    private static List<Command> commands = new ArrayList<>();

    /**
     * Register all commands.
     */
    private static void registerCommands() {

        // Player Commands
        addCommand(new CommandDelHome());
        addCommand(new CommandDungeon());
        addCommand(new CommandHelp());
        addCommand(new CommandInfo(Configs.ConfigType.DONATE, "How to donate / donor perks.", "donate", "shop"));
        addCommand(new CommandHome());
        addCommand(new CommandIgnore());
        addCommand(new CommandMessage());
        addCommand(new CommandMail());
        addCommand(new CommandReply());
        addCommand(new CommandRanks());
        addCommand(new CommandRankup());
        addCommand(new CommandRTP());
        addCommand(new CommandInfo(Configs.ConfigType.RULES, "Server rules.", "rules", "info"));
        addCommand(new CommandSetHome());
        addCommand(new CommandSpawn());
        addCommand(new CommandUnignore());
        addCommand(new CommandVote());
        addCommand(new CommandVotes());

        // Staff Commands
        addCommand(new CommandAnnounce());
        addCommand(new CommandBright());
        addCommand(new CommandConfig());
        addCommand(new CommandDeathTeleport());
        addCommand(new CommandGUIs());
        addCommand(new CommandMined());
        addCommand(new CommandReboot());
        addCommand(new CommandRescue());
        addCommand(new CommandSetRank());
        addCommand(new CommandSpectator());
        addCommand(new CommandTestVote());
    }

    private static void addCommand(Command command) {
        getCommands().add(command);
    }

    @Override
    public void onEnable() {
        registerCommands();
    }

    /**
     * Gets a list of commands by their specified type.
     * @param type
     * @return
     */
    public static List<Command> getCommands(CommandType type) {
        return getCommands().stream().filter(c -> c.getType() == type || type == null).collect(Collectors.toList());
    }

    /**
     * Gets a command by its alias.
     * @param alias
     * @return
     */
    public static Command getCommand(CommandType type,  String alias) {
        return getCommands(type).stream().filter(c -> c.getAlias().contains(alias.toLowerCase()))
                .findAny().orElse(null);
    }

    /**
     * Get a command label from a chat input line.
     * @param input
     * @return
     */
    private static String getLabel(CommandType type,  String input) {
        String f = input.split(" ")[0];
        return f.length() > type.getPrefix().length() ? f.substring(type.getPrefix().length()) : "";
    }

    /**
     * Tries to run a command. Returns true if the command was found / executed.
     * @param sender
     * @param type
     * @return commandSuccess
     */
    public static boolean handleCommand(CommandSender sender, CommandType type, String input) {
        if (!input.startsWith(type.getPrefix()))
            return false; // Not this command type.

        List<String> split = new ArrayList<>(Arrays.asList(input.split(" "))); // remove() won't work with just asList
        String cmd = getLabel(type, input);
        Command command = getCommand(type, cmd);
        if (command == null) {
            if (type == CommandType.CHAT)
                sender.sendMessage(ChatColor.RED + "Unknown command. Type '.help' for help.");
            return false; // Not a command.
        }

        split.remove(0); // Remove the command from args.
        String[] args = split.toArray(new String[split.size()]);

        // Don't run this async.
        Bukkit.getScheduler().runTask(Core.getInstance(), () -> runCommand(command, sender, cmd, args));
        return true;
    }

    private static void runCommand(Command cmd, CommandSender sender, String label, String[] args) {
        try {
            cmd.handle(sender, label, args); // Handle command logic.
        } catch (Exception e) {
            e.printStackTrace();
            Core.warn("Error executing " + cmd.getName() + " as '" + sender.getName() + "'");
            sender.sendMessage(ChatColor.RED + "There was an error while running this command.");
        }
    }

    @EventHandler(priority = EventPriority.LOW) // Commands are top priority.
    public void onChat(AsyncPlayerChatEvent evt) {
        handleCommand(evt.getPlayer(), CommandType.CHAT, evt.getMessage());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent evt) {
        if (handleCommand(evt.getPlayer(), CommandType.SLASH, evt.getMessage()))
            evt.setCancelled(true); // Don't show 'unknown command....'

        String label = getLabel(CommandType.CHAT, evt.getMessage());
        Command cmd = getCommand(CommandType.CHAT, label);
        if (cmd != null) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(ChatColor.RED + "We use . commands due to sVanilla rules. (Try ." + label +")");
        }

        if (evt.getMessage().startsWith("/ ")) {
            Core.alertStaff(ChatColor.RED + "[AC] " + evt.getPlayer().getName() + ": " + ChatColor.GREEN
                    + evt.getMessage().substring(2));
            evt.setCancelled(true);
            return;
        }

        Core.alertStaff(ChatColor.RED + evt.getPlayer().getName() + ": " + ChatColor.GRAY + evt.getMessage());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onServerCommand(ServerCommandEvent evt) {
        if (handleCommand(evt.getSender(), CommandType.SLASH, CommandType.SLASH.getPrefix() + evt.getCommand()))
            evt.setCancelled(true); // Handle console commands.

        if (evt.getCommand().startsWith(" ")) {
            Core.alertStaff(ChatColor.RED + "[AC] Server: " + ChatColor.GREEN + evt.getCommand().substring(1));
            evt.setCancelled(true);
        }
    }

    //TODO: Command block commands.
}