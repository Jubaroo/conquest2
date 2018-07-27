package org.talor.wurmunlimited.mods.structures;

import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.*;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LearnScroll implements ModAction, BehaviourProvider, ActionPerformer {

    private final short learnScrollActionId;
    private final ActionEntry learnScrollActionEntry;
    private ScrollTemplate[] scrollTemplates;

    public LearnScroll(ScrollTemplate[] templates) {
        learnScrollActionId = (short) ModActions.getNextActionId();
        learnScrollActionEntry = ActionEntry.createEntry(learnScrollActionId, "Learn Scroll", "reading", new int[] { 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */});
        ModActions.registerAction(learnScrollActionEntry);
        scrollTemplates = templates;
    }

    public boolean isScroll(int itemTemplateID){

        for (ScrollTemplate template : scrollTemplates) {

            if (itemTemplateID == template.templateID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer , Item source , Item target) {
        return getBehavioursFor(performer, target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer ,Item target) {
        if (performer instanceof Player && isScroll(target.getTemplateId())) {
            return Arrays.asList(learnScrollActionEntry);
        } else {
            return null;
        }
    }

    @Override
    public short getActionId() {
        return learnScrollActionId;
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short act, float counter) {
        return action(action, performer, target, act, counter);
    }

    @Override
    public boolean action(Action action, Creature performer, Item target, short act, float counter) {
        if (performer.isPlayer() && isScroll(target.getTemplateId())) {

            for (ScrollTemplate template : scrollTemplates) {
                if (target.getTemplateId() == template.templateID) {
                    if (template.skillID == 2147483645) {
                        try {
                            if (performer.getDeity() != null) {
                                if (performer.getFaith() <= 100) {

                                    if (performer.getFaith() + template.skillModifier >= 100) {
                                        performer.setFaith(100.0F);
                                        performer.setFavor(100.0F);
                                        performer.getCommunicator().sendSafeServerMessage("You have gained max faith!");
                                        Items.destroyItem(target.getWurmId());
                                        return true;
                                    } else {
                                        performer.setFaith(performer.getFaith() + template.skillModifier);
                                        performer.setFavor(performer.getFavor() + template.skillModifier);
                                        performer.getCommunicator().sendSafeServerMessage("You have gained 10 faith and favor!");
                                        Items.destroyItem(target.getWurmId());
                                        return true;
                                    }
                                } else {
                                    performer.getCommunicator().sendSafeServerMessage("You have max faith and cannot learn any more from this scroll!");
                                    return true;
                                }
                            } else {
                                performer.getCommunicator().sendSafeServerMessage("You must be following a god to use this item!");
                                return true;
                            }
                        } catch (Exception e) {
                            performer.getCommunicator().sendSafeServerMessage("Scroll wont open!");
                        }
                    } else if (template.skillID == 0) {
                        try {
                            int skillNum = SkillSystem.getRandomSkillNum();
                            Skills skills = performer.getSkills();
                            Skill s = skills.getSkill(skillNum);
                            if (s.affinity < 5) {
                                Affinities.setAffinity(performer.getWurmId(), skillNum, s.affinity + 1, false);
                                performer.getCommunicator().sendSafeServerMessage("You gain an affinity for " + s.getName());
                                Items.destroyItem(target.getWurmId());
                                return true;
                            } else {
                                performer.getCommunicator().sendSafeServerMessage("You start thinking about " + s.getName() + ", but you already have a very strong affinity for this.");
                                return true;
                            }

                        } catch (Exception e) {

                        }
                    } else {
                        try {
                            Skills skills = performer.getSkills();
                            Skill s = skills.getSkill(template.skillID);

                            if (s.getKnowledge() + template.skillModifier >= 90) {
                                performer.getCommunicator().sendSafeServerMessage("Your " + s.getName().toLowerCase() + " is so high that you would not benefit from this scroll.");
                                return true;
                            } else if (s.getKnowledge() + template.skillModifier >= 70) {
                                s.setKnowledge(s.getKnowledge() + (template.skillModifier / 20), false);
                            } else if (s.getKnowledge() + template.skillModifier >= 50) {
                                s.setKnowledge(s.getKnowledge() + (template.skillModifier / 10), false);
                            } else {
                                s.setKnowledge(s.getKnowledge() + template.skillModifier, false);
                            }

                           performer.getCommunicator().sendSafeServerMessage("You gain some " + s.getName().toLowerCase() + ".");
                           Items.destroyItem(target.getWurmId());
                            return true;
                        } catch (Exception e) {
                            performer.getCommunicator().sendSafeServerMessage("Scroll wont open!");
                            return true;
                        }
                    }
                }
            }
        }
        performer.getCommunicator().sendSafeServerMessage("That is not a valid scroll!");
        return true;
    }
}





