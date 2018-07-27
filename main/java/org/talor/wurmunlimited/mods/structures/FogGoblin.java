package org.talor.wurmunlimited.mods.structures;

import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.EncounterBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviour;
import org.gotti.wurmunlimited.modsupport.vehicles.VehicleFacade;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.creatures.AttackAction;
import com.wurmonline.server.creatures.AttackIdentifier;
import com.wurmonline.server.creatures.AttackValues;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.server.items.Item;

public class FogGoblin implements ModCreature, CreatureTypes {

    private int templateId;

    public CreatureTemplateBuilder createCreateTemplateBuilder() {
        int[] types = {C_TYPE_MOVE_LOCAL, C_MOD_GREENISH, C_TYPE_CARNIVORE, C_TYPE_PREY, C_TYPE_FLEEING, C_TYPE_MONSTER, C_TYPE_CLIMBER, C_TYPE_OPENDOORS};
        int[] butcheredItems = new int[]{303, 310};

        CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.foggoblin", "Fog-goblin potioneer", "The fog-goblins rely on potioneers to keep them well-stocked with magical brews.", "model.creature.humanoid.goblin.leader", types, (byte) 0, (short) 8, (byte) 0, (short) 130, (short) 30,
                (short) 30, "sound.death.goblin", "sound.death.goblin", "sound.combat.hit.goblin", "sound.combat.hit.goblin", 0.75f, 7.0f, 4.0f, 5.0f, 0.0f, 0.0f, 1.6f, 1500, butcheredItems, 5, 10);

        this.templateId = builder.getTemplateId();
        System.out.println("gobbo id" + this.templateId);

        builder.skill(102, 30.0f);
        builder.skill(104, 30.0f);
        builder.skill(103, 30.0f);
        builder.skill(100, 12.0f);
        builder.skill(101, 15.0f);
        builder.skill(105, 30.0f);
        builder.skill(106, 7.0f);
        builder.skill(10052, 60.0f);

        builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
        builder.handDamString("claw");
        builder.maxAge(100);
        builder.armourType(3);
        builder.baseCombatRating(40.0f);
        builder.combatDamageType((byte) 2);
        builder.maxGroupAttackSize(2);
        builder.maxPercentOfCreatures(0.01f);

        return builder;
    }
}