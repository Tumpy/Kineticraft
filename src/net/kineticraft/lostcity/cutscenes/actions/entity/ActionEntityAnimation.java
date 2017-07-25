package net.kineticraft.lostcity.cutscenes.actions.entity;

import lombok.Getter;
import lombok.Setter;
import net.kineticraft.lostcity.cutscenes.CutsceneEvent;
import org.bukkit.EntityEffect;

/**
 * Execute an entity animation.
 * Created by Kneesnap on 7/22/2017.
 */
@Getter @Setter
public class ActionEntityAnimation extends ActionEntity {

    private EntityEffect effect = EntityEffect.HURT;

    @Override
    public void execute(CutsceneEvent event) {
        getEntity(event).playEffect(getEffect());
    }
}