package org.talor.wurmunlimited.mods.structures;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.*;
import com.wurmonline.server.spells.SpellEffect;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UseEnchantScroll implements ModAction, BehaviourProvider, ActionPerformer {

    private final short enchantScrollActionId;
    private final ActionEntry enchantScrollActionEntry;
    private EnchantScrollTemplate[] enchantScrollTemplate;

    public UseEnchantScroll(EnchantScrollTemplate[] templates) {
        enchantScrollActionId = (short) ModActions.getNextActionId();
        enchantScrollActionEntry = ActionEntry.createEntry(enchantScrollActionId, "Enchant Item", "enchanting", new int[] { 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */});
        ModActions.registerAction(enchantScrollActionEntry);
        enchantScrollTemplate = templates;
    }

    public boolean isScroll(int itemTemplateID){

        for (EnchantScrollTemplate template : enchantScrollTemplate) {

            if (itemTemplateID == template.templateID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer , Item source , Item target) {
        if (performer instanceof Player && isScroll(source.getTemplateId()) && mayBeEnchanted(target)) {
            return Arrays.asList(enchantScrollActionEntry);
        } else {
            return null;
        }
    }

    @Override
    public short getActionId() {
        return enchantScrollActionId;
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short act, float counter) {
        if (performer.isPlayer() && isScroll(source.getTemplateId())) {

            float power = 30.0F + (Server.rand.nextFloat() * 70.0F);
            EnchantScrollTemplate foundTemplate = null;

            for (EnchantScrollTemplate template : enchantScrollTemplate) {
                if (source.getTemplateId() == template.templateID) {
                    foundTemplate = template;
                }
            }

            if (foundTemplate != null) {

                if (!mayBeEnchanted(target)) {
                    performer.getCommunicator().sendNormalServerMessage("The scroll cannot be used on the " + target.getName() + ".", (byte) 3);
                    return true;
                }

                if ((target.isWeapon() && !mayWeaponBeEnchanted(target, performer, (byte) foundTemplate.enchantID)) ||
                        (target.isArmour() && !mayArmourBeEnchanted(target, performer, (byte) foundTemplate.enchantID)) ||
                        (target.isEnchantableJewelry() && !mayJewelryBeEnchanted(target, performer, (byte) foundTemplate.enchantID)) ||
                        !mayReceiveSkillgainBuff(target, performer, (byte) foundTemplate.enchantID)) {
                    performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that conflicts with this scroll.", (byte) 3);
                    return true;
                }

                ItemSpellEffects effs = target.getSpellEffects();
                if (effs == null) {
                    effs = new ItemSpellEffects(target.getWurmId());
                }
                SpellEffect eff = effs.getSpellEffect((byte) foundTemplate.enchantID);
                if (eff == null) {
                    performer.getCommunicator().sendNormalServerMessage("You use the scroll, and enchant the " + target.getName() + ".", (byte) 2);

                    eff = new SpellEffect(target.getWurmId(), (byte) foundTemplate.enchantID, power, 20000000);
                    effs.addSpellEffect(eff);
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " looks pleased.", performer, 5);
                    Items.destroyItem(source.getWurmId());
                    return true;
                } else if (eff.getPower() > power) {
                    performer.getCommunicator().sendNormalServerMessage("You use the scroll, but fail to improve the enchantment.", (byte) 3);
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " frowns.", performer, 5);
                    Items.destroyItem(source.getWurmId());
                    return true;
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You use the scroll, and succeed in improving the power of the enchantment.", (byte) 2);
                    eff.improvePower(power);
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " looks pleased.", performer, 5);
                    Items.destroyItem(source.getWurmId());
                    return true;
                }
            }
        }
        performer.getCommunicator().sendSafeServerMessage("That is not a valid scroll!");
        return true;
    }

    public boolean mayBeEnchanted(Item target)
    {
        if (target.isOverrideNonEnchantable()) {
            return true;
        }
        if ((target.isBodyPart()) || isScroll(target.getTemplateId()) || (target.isNewbieItem()) || (target.isNoTake()) ||
                ((target.isHollow()) && (target.getTemplateId() != 287) && (target.getTemplateId() != 621) && (!target.isSaddleBags())) ||
                (target.getTemplateId() == 179) || (target.getTemplateId() == 386) ||
                (target.isTemporary()) || (target.getTemplateId() == 272) ||
                ((target.isLockable()) && (target.getLockId() != -10L)) || (target.isIndestructible()) || (target.isHugeAltar()) ||
                (target.isDomainItem()) || (target.isKingdomMarker()) || (target.isTraded()) || (target.isBanked()) ||
                (target.isArtifact()) || (target.isEgg()) || (target.isChallengeNewbieItem()) || (target.isRiftLoot()) || (target.isRiftAltar()) ||
                (target.getTemplateId() == 1307)) {
            return false;
        }
        return true;
    }
    public  boolean mayJewelryBeEnchanted(Item target, Creature performer, byte enchantment)
    {
        if ((enchantment != 29) && (target.getNolocateBonus() > 0.0F))
        {
            return false;
        }
        return true;
    }

    public boolean mayArmourBeEnchanted(Item target, Creature performer, byte enchantment)
    {
        if (enchantment != 17 && enchantment != 46) {
            return true;
        }

        if ((enchantment != 17) && (target.getSpellPainShare() > 0.0F))
        {
            return false;
        }
        if ((enchantment != 46) && (target.getSpellSlowdown() > 0.0F))
        {
            return false;
        }
        return true;
    }

    public boolean mayReceiveSkillgainBuff(Item target, Creature performer, byte enchantment)
    {
        if (enchantment != 47  && enchantment != 13  && enchantment != 16) {
            return true;
        }
        if (enchantment != 47)
        {
            if (target.getBonusForSpellEffect((byte)47) > 0.0F)
            {
                return false;
            }
        }
        else if ((target.getBonusForSpellEffect((byte)13) > 0.0F) ||
                (target.getBonusForSpellEffect((byte)16) > 0.0F))
        {
            return false;
        }
        return true;
    }

    public boolean mayWeaponBeEnchanted(Item target, Creature performer, byte enchantment)
    {

        if (enchantment != 26 && enchantment != 33 && enchantment != 27 && enchantment != 14 && enchantment != 18 && enchantment != 45) {
            return true;
        }

        if ((enchantment != 18) && (target.getSpellRotModifier() > 0.0F))
        {
            return false;
        }
        if ((enchantment != 26) && (target.getSpellLifeTransferModifier() > 0.0F))
        {
            return false;
        }
        if ((enchantment != 27) && (target.getSpellVenomBonus() > 0.0F))
        {
            return false;
        }
        if ((enchantment != 33) && (target.getSpellFrostDamageBonus() > 0.0F))
        {
            return false;
        }
        if ((enchantment != 45) && (target.getSpellExtraDamageBonus() > 0.0F))
        {
            return false;
        }
        if ((enchantment != 14) && (target.getSpellDamageBonus() > 0.0F))
        {
            return false;
        }
        return true;
    }


}
