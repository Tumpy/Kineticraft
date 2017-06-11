package net.kineticraft.lostcity.commands.player;

import net.kineticraft.lostcity.EnumRank;
import net.kineticraft.lostcity.commands.CommandType;
import net.kineticraft.lostcity.commands.PlayerCommand;
import net.kineticraft.lostcity.data.wrappers.KCPlayer;
import net.kineticraft.lostcity.mechanics.MetadataManager;
import net.kineticraft.lostcity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Send a private message.
 *
 * Created by Kneesnap on 6/10/2017.
 */
public class CommandMessage extends PlayerCommand {

    public CommandMessage() {
        super(EnumRank.MU, CommandType.SLASH, false, "<player> <message>",
                "Send a private message to a player.", "msg", "w", "tell", "whisper", "t");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {

        String sName = sender instanceof Player ?
                KCPlayer.getWrapper((Player) sender).getColoredName() : ChatColor.YELLOW + sender.getName();
        String message = ChatColor.DARK_GRAY + ": " + ChatColor.WHITE + String.join(" ", Utils.shift(args));

        CommandSender receiver = args[0].equalsIgnoreCase("CONSOLE") ? Bukkit.getConsoleSender()
                : Bukkit.getPlayer(args[0]);
        String rName = receiver instanceof Player ? KCPlayer.getWrapper((Player) receiver).getColoredName()
                : ChatColor.YELLOW + receiver.getName();

        if (receiver == null) {
            sender.sendMessage(ChatColor.RED + "Player is offline.");
            return;
        }

        receiver.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "FROM " + sName + message);
        sender.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "TO " + rName + message);

        if (receiver instanceof Player) {
            Player p = (Player) receiver;
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, .75F);
            MetadataManager.setMetadata(p, MetadataManager.Metadata.LAST_WHISPER, sender.getName());
        }

        if (sender instanceof Player)
            MetadataManager.setMetadata((Player) sender, MetadataManager.Metadata.LAST_WHISPER, receiver.getName());
    }
}