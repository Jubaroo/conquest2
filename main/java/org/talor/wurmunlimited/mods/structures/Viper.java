package org.talor.wurmunlimited.mods.structures;

import com.wurmonline.shared.constants.CreatureTypes;
import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;

public class Viper implements ModCreature, CreatureTypes {

    public int templateId;

    public CreatureTemplateBuilder createCreateTemplateBuilder() {
        int[] types = {C_TYPE_AGG_HUMAN, C_TYPE_MOVE_LOCAL, C_TYPE_CARNIVORE, C_TYPE_MONSTER, C_TYPE_CLIMBER};
        int[] butcheredItems = new int[]{303, 310};

        CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.viper", "Viper", "Deadly serpent", "model.creature.snake.kingcobra", types, (byte) 0, (short) 8, (byte) 10, (short) 60, (short) 60,
                (short) 540, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.75f, 7.0f, 4.0f, 5.0f, 0.0f, 0.0f, 0.8f, 50, butcheredItems, 10, 94);

        templateId = builder.getTemplateId();

        builder.skill(102, 30.0f);
        builder.skill(104, 30.0f);
        builder.skill(103, 30.0f);
        builder.skill(100, 12.0f);
        builder.skill(101, 15.0f);
        builder.skill(105, 30.0f);
        builder.skill(106, 7.0f);
        builder.skill(10052, 70.0f);

        builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
        builder.handDamString("bite");
        builder.maxAge(100);
        builder.armourType(3);
        builder.baseCombatRating(65.0f);
        builder.combatDamageType((byte) 5);
        builder.maxGroupAttackSize(2);
        builder.maxPercentOfCreatures(0.01f);

        return builder;
    }
}