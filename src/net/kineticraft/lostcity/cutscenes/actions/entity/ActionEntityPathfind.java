package net.kineticraft.lostcity.cutscenes.actions.entity;

import net.kineticraft.lostcity.cutscenes.annotations.ActionData;
import net.kineticraft.lostcity.cutscenes.CutsceneEvent;
import net.kineticraft.lostcity.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/**
 * Make an entity walk from point A to point B.
 * Created by Kneesnap on 7/27/2017.
 */
@ActionData(Material.MAP)
public class ActionEntityPathfind extends ActionEntity {
    private Location target;

    @Override
    public void execute(CutsceneEvent event) {
        LivingEntity e = getLivingEntity(event);
        e.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(50);

        // Create the goal this entity should walk to.
        ArmorStand goal = (ArmorStand) target.getWorld().spawnEntity(target, EntityType.ARMOR_STAND);
        goal.setInvulnerable(true);
        goal.setVisible(false);

        // Set the target of this entity
        ((Creature) e).setTarget(goal);
        toggleAI(event, le -> {
            ((Creature) le).setTarget(goal);
            boolean done = e.getLocation().distance(target) < .25D || !e.isValid();
            if (done)
                goal.remove();
            return done;
        });
    }

    @Override
    public String toString() {
        return Utils.toCleanString(target) + super.toString();
    }
}
