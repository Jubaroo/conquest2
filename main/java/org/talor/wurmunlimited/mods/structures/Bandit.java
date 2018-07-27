package org.talor.wurmunlimited.mods.structures;

import com.wurmonline.shared.constants.CreatureTypes;
import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

public class Bandit implements ModCreature, CreatureTypes {

    public int templateId;

    public CreatureTemplateBuilder createCreateTemplateBuilder() {
        int[] types = {C_TYPE_AGG_HUMAN, C_TYPE_MOVE_LOCAL, C_TYPE_HUNTING, C_TYPE_MONSTER, C_TYPE_CARNIVORE, C_TYPE_CAVEDWELLER, C_TYPE_OPENDOORS};
        int[] butcheredItems = new int[]{636, 92};

        CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.bandit", "Bandit", "The bandits are thieving brigands.", "model.creature.humanoid.human.guard.tower", types, (byte) 0, (short) 8, (byte) 10, (short) 200, (short) 45,
                (short) 30, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 0.2f, 5.0f, 7.0f, 0.0f, 0.0f, 0.0f, 1.0f, 200, butcheredItems, 10, 94);

        templateId = builder.getTemplateId();
        Structures.banditID = templateId;

        builder.skill(102, 30.0f);
        builder.skill(104, 30.0f);
        builder.skill(103, 30.0f);
        builder.skill(100, 12.0f);
        builder.skill(101, 15.0f);
        builder.skill(105, 30.0f);
        builder.skill(106, 7.0f);
        builder.skill(10052, 60.0f);
        builder.skill(10005, 60.0f);
        builder.skill(10028, 60.0f);
        builder.skill(10025, 60.0f);
        builder.skill(10001, 60.0f);
        builder.skill(10024, 60.0f);
        builder.skill(10023, 60.0f);
        builder.skill(10021, 60.0f);
        builder.skill(10020, 60.0f);
        builder.skill(10006, 60.0f);

        builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
        builder.handDamString("claw");
        builder.maxAge(100);
        builder.armourType(3);
        builder.baseCombatRating(40.0f);
        builder.combatDamageType((byte) 2);
        builder.maxGroupAttackSize(6);
        builder.maxPercentOfCreatures(0.01f);
        builder.hasHands(true);

        return builder;
    }

}
