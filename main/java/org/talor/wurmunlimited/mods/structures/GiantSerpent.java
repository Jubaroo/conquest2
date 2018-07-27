package org.talor.wurmunlimited.mods.structures;

import com.wurmonline.shared.constants.CreatureTypes;
import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

public class GiantSerpent implements ModCreature, CreatureTypes {

    public int templateId;

    public CreatureTemplateBuilder createCreateTemplateBuilder() {
        int[] types = {C_TYPE_AGG_HUMAN, C_TYPE_MOVE_LOCAL, C_TYPE_CARNIVORE, C_TYPE_MONSTER, C_TYPE_CLIMBER};
        int[] butcheredItems = new int[]{636, 92};

        CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.giantserpent", "Giant Serpent", "A gigantic snake", "model.creature.snake.serpent.sea", types, (byte) 9, (short) 8, (byte) 10, (short) 100, (short) 1000,
                (short) 100, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.05f, 0.0f, 0.0f, 56.0f, 30.0f, 0.0f, 2.0f, 50, butcheredItems, 10, 94);

        templateId = builder.getTemplateId();

        builder.skill(102, 50.0f);
        builder.skill(104, 50.0f);
        builder.skill(103, 50.0f);
        builder.skill(100, 50.0f);
        builder.skill(101, 50.0f);
        builder.skill(105, 50.0f);
        builder.skill(106, 50.0f);
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
        builder.handDamString("bite");
        builder.maxAge(100);
        builder.armourType(3);
        builder.baseCombatRating(70.0f);
        builder.combatDamageType((byte) 2);
        builder.maxGroupAttackSize(6);
        builder.maxPercentOfCreatures(0.01f);

        return builder;
    }

}