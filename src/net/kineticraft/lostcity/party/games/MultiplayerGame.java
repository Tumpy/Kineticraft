package net.kineticraft.lostcity.party.games;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Represents a game that can be played by multiple players.
 * Created by Kneesnap on 9/14/2017.
 */
@Getter @AllArgsConstructor
public class MultiplayerGame extends PartyGame {
    private int minPlayers;

    @Override
    protected void onJoin(Player player) {
        broadcast(player.getName() + " has joined." + (getMinPlayers() > 0 ? " (" + getPlayers().size() + "/" + getMinPlayers() + ")" : ""));
        if (getPlayers().size() == getMinPlayers())
            start();
    }

    @Override
    protected boolean canAdd(Player player) {
        if (isGoing()) {
            player.sendMessage(ChatColor.RED + "You cannot join while this game is in progress.");
            return false;
        }
        return super.canAdd(player);
    }
}
