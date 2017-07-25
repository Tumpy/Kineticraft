package net.kineticraft.lostcity.mechanics;

import net.kineticraft.lostcity.mechanics.metadata.Metadata;
import net.kineticraft.lostcity.mechanics.metadata.MetadataManager;
import net.kineticraft.lostcity.mechanics.system.Mechanic;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Prevents farms from being too economically overpowered.
 *
 * Provides Restrictions:
 *  - Limits overpopulating chicken farms.
 *  - Limits the effectiveness of iron / gold / etc farms by requiring players to perform most of the damage to get a drop.
 *
 * Created by Kneesnap on 6/3/2017.
 */
public class FarmLimiter extends Mechanic {

    private static final int RADIUS = 4;
    private static List<EntityType> IGNORE = Arrays.asList(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN,
            EntityType.ARMOR_STAND, EntityType.PLAYER, EntityType.WITHER, EntityType.ENDER_DRAGON);

    @EventHandler(ignoreCancelled = true)
    public void onChickenSpawn(CreatureSpawnEvent evt) {
        evt.setCancelled((evt.getSpawnReason() == SpawnReason.DISPENSE_EGG || evt.getSpawnReason() == SpawnReason.EGG)
                && getEntityCount(evt.getEntity()) >= 32);// There are more chickens here than we allow, don't spawn another one.
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent evt) {
        LivingEntity e = evt.getEntity();
        EntityType type = evt.getEntityType();
        boolean limit = (getEntityCount(e) >= 5 && e instanceof Monster)
                || getPlayerDamage(e) < getDamageNeeded(e)
                || type == EntityType.PIG_ZOMBIE;
        if (!IGNORE.contains(type) && limit)
            evt.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent evt) {
        Entity e = evt.getEntity();
        Entity a = evt.getDamager();
        if (a instanceof Player || (a instanceof Projectile && ((Projectile) a).getShooter() instanceof Player))
            addPlayerDamage(e, evt.getDamage()); // Count all damage dealt by players.
    }

    /**
     * Get the amount of damage inflicted by a player on this entity.
     * @param entity
     * @return playerDamage
     */
    private static double getPlayerDamage(Entity entity) {
        return MetadataManager.getMetadata(entity, Metadata.PLAYER_DAMAGE).asDouble();
    }

    /**
     * Add to the amount of player damage inflicted on an entity.
     * @param entity
     * @param damage
     */
    private static void addPlayerDamage(Entity entity, double damage) {
        MetadataManager.setMetadata(entity, Metadata.PLAYER_DAMAGE, getPlayerDamage(entity) + damage);
    }

    /**
     * Get the amount of required player-inflicted damage to kill this entity.
     * Takes in factors such as the number of entities nearby and max health.
     *
     * @param entity
     * @return damageNeeded
     */
    private static double getDamageNeeded(LivingEntity entity) {
        int entityCount = getEntityCount(entity);
        double percentNeeded = Math.max(1, entityCount - 3) / 10D; //10% needed for each mob nearby, if > 3 mobs are nearby.
        percentNeeded = Math.max(Math.min(percentNeeded, .75D), .25D); // Restrict the damage amounts between 25% and 75%.
        return percentNeeded * entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    /**
     * Get the number of entities near an entity..
     * @param entity
     * @return entityCount
     */
    private static int getEntityCount(Entity entity) {
        return (int) entity.getWorld().getNearbyEntities(entity.getLocation(), RADIUS, RADIUS, RADIUS).stream()
                .filter(e -> e.getType() == entity.getType()).count();
    }
}
