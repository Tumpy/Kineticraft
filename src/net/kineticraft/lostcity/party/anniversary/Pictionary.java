package net.kineticraft.lostcity.party.anniversary;

import com.destroystokyo.paper.Title;
import net.kineticraft.lostcity.item.ItemManager;
import net.kineticraft.lostcity.mechanics.metadata.Metadata;
import net.kineticraft.lostcity.mechanics.metadata.MetadataManager;
import net.kineticraft.lostcity.party.Parties;
import net.kineticraft.lostcity.party.games.MultiplayerGame;
import net.kineticraft.lostcity.utils.ColorConverter;
import net.kineticraft.lostcity.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pictionary Game for the anniversary party.
 * TODO: Chat system.
 * Created by Kneesnap on 10/2/2017.
 */
public class Pictionary extends MultiplayerGame {
    private Player currentPainter;
    private String currentWord;
    private byte currentColor;
    private long currentEndTime;
    private long penTime;
    private List<Player> drawQueue;

    private static final ItemStack DRAW_ITEM = ItemManager.createItem(Material.STICK, ChatColor.YELLOW + "Pictionary Stick", "Left-Click: Fill", "Right-Click: Draw");
    private static final int DRAW_TIME = 90;
    private static final int MIN_X = -67;
    private static final int MAX_X = -43;
    private static final int MIN_Y = 69;
    private static final int MAX_Y = 94;
    private static final int ARENA_Z = -22;

    public Pictionary() {
        super(1);
        setExit(-47.925, 79, -9.075, 45, 0);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent evt) {
        if (!evt.getPlayer().equals(currentPainter))
            return;

        Block target = evt.getPlayer().getTargetBlock((Set<Material>) null, 50);
        if (target.getZ() != ARENA_Z || target.getX() < MIN_X || target.getX() > MAX_X)
            return; // Not clicking on the canvas.

        if (target.getY() >= MAX_Y + 2 && target.getY() <= MAX_Y + 5) { // Pick color.
            penTime = 0;
            currentColor = target.getData();
            updateActionBar();
            currentPainter.playSound(currentPainter.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.75F, 1.75F);
        } else if (evt.getAction() == Action.RIGHT_CLICK_AIR) { // Pen Tool
            penTime = System.currentTimeMillis() + 200;
            placeInk();
        } else if (evt.getAction() == Action.LEFT_CLICK_AIR) { // Fill
            playSound(Sound.ENTITY_BOBBER_SPLASH, 1.5F, 0.5F);
            floodFill(target);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent evt) {
        if (!evt.getPlayer().equals(currentPainter))
            return;

        if (System.currentTimeMillis() <= penTime)
            placeInk();

        if (evt.getTo().distance(evt.getFrom()) != 0)
            evt.setTo(evt.getFrom()); // Don't allow moving off the plate.
    }

    @Override
    protected void onStart() {
        broadcastPlayers("The game has begun!");
        drawQueue = new ArrayList<>(getPlayers());

        for (Player p : getPlayers()) { // Reset player data.
            MetadataManager.removeMetadata(p, Metadata.DMT_CORRECT);
            MetadataManager.removeMetadata(p, Metadata.DMT_SCORE);
        }

        nextPlayer();

        getScheduler().runTaskTimer(this::updateActionBar, 0L, 20L);
        getScheduler().runTaskTimer(this::placeInk, 0L, 1L);
    }

    @Override
    protected void onStop() {
        broadcastPlayers("The game has ended.");
        int topPlayer = 0;
        //TODO Show winner.
    }

    /**
     * Update the current drawer's action bar.
     */
    private void updateActionBar() {
        if (currentPainter != null) {
            long time = (currentEndTime - System.currentTimeMillis()) / 1000;
            if (!currentPainter.isOnline() || time < 0) {
                nextPlayer();
                return;
            }

            currentPainter.sendActionBar(ChatColor.BLUE + "Draw: " + ChatColor.YELLOW + currentWord + ChatColor.BLUE
                    + " (" + ColorConverter.getDye(DyeColor.getByWoolData(currentColor)).getDisplayName() + ChatColor.BLUE + ") "
                    + ChatColor.YELLOW + time + "s");
        }
    }

    /**
     * Finish the current player's drawing and move on to the next player.
     */
    private void nextPlayer() {
        if (currentPainter != null) { // If they're not the first drawer.
            broadcastPlayers("Drawing complete. The word was: " + ChatColor.GOLD + currentWord + ChatColor.BLUE + ".");
            currentPainter.teleport(getExitLocation());
        }

        for (Player p : getPlayers()) // Reset
            MetadataManager.removeMetadata(p, Metadata.DMT_CORRECT);

        if (drawQueue.isEmpty()) { // Game has finished.
            currentPainter = null;
            stop();
            return;
        }

        getScheduler().runTaskLater(() -> {
            clearCanvas();
            currentWord = Utils.randElement("Nose", "Lime", "Sun", "Pokeball", "Firework", "Obama",
                    "Lion", "Noteblock", "Fire", "Teacher", "Queen", "Cat", "Violin", "Titanic", "Pirate", "Eggplant", "Camel");

            currentEndTime = System.currentTimeMillis() + (1000 * DRAW_TIME); // Setup variables for next drawer, and announce.
            currentPainter = drawQueue.remove(0);
            Utils.giveItem(currentPainter, DRAW_ITEM);

            currentPainter.sendMessage(ChatColor.BLUE + "Your word is " + ChatColor.GOLD + currentWord + ChatColor.BLUE + ".");
            currentPainter.sendTitle(new Title(ChatColor.BLUE + "Draw: " + ChatColor.GOLD + currentWord));

            broadcastPlayers(currentPainter.getName() + " is now drawing.");
            currentPainter.teleport(new Location(Parties.getPartyWorld(), -54.5, 88, -39));
        }, 100L);
    }

    /**
     * Clears the drawing canvas.
     */
    private void clearCanvas() {
        for (int x = MIN_X; x <= MAX_X; x++)
            for (int y = MIN_Y; y <= MAX_Y; y++)
                setBlock(new Location(Parties.getPartyWorld(), x, y, ARENA_Z).getBlock(), (byte) 0);
    }

    /**
     * Set a given block on the canvas to a certain wool color.
     * @param bk
     * @param color
     */
    private void setBlock(Block bk, byte color) {
        if (bk.getType() == Material.WOOL && bk.getZ() == ARENA_Z)
            bk.setData(color);
    }

    /**
     * Places the ink onto the canvas.
     */
    private void placeInk() {
        if (currentPainter != null && System.currentTimeMillis() <= penTime) {// If the pen is down and someone is painting, draw.
            setBlock(currentPainter.getTargetBlock((Set<Material>) null, 50), currentColor);
            playSound(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 2, 0.2F);
        }
    }

    private void floodFill(Block target) {
        byte originalColor = target.getData();
        List<Block> queue = new ArrayList<>();
        List<Block> covered = new ArrayList<>();
        queue.add(target);
        while (!queue.isEmpty()) {
            Block current = queue.remove(0);
            setBlock(current, currentColor);
            addQueue(queue, covered, current, originalColor, 0, -1);
            addQueue(queue, covered, current, originalColor, 0, 1);
            addQueue(queue, covered, current, originalColor, -1, 0);
            addQueue(queue, covered, current, originalColor, 1, 0);

        }
    }

    private void addQueue(List<Block> queue, List<Block> covered, Block target, byte oldColor, int xOffset, int yOffset) {
        target = target.getLocation().add(xOffset, yOffset, 0).getBlock();
        if (target.getType() == Material.WOOL && target.getData() == oldColor && !covered.contains(target)) {
            queue.add(target);
            covered.add(target);
        }
    }
}
