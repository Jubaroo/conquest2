package org.talor.wurmunlimited.mods.structures;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaceGem implements ModAction {

    private final short actionId;
    private final ActionEntry actionEntry;
    private int mechanismID;
    private static Logger logger = Logger.getLogger(OpenCrate.class.getName());
    private EnchantScrollTemplate[] enchantScrollTemplates;



    public PlaceGem(int id, EnchantScrollTemplate[] est) {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Place Gem", "placing", new int[] { 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */});
        ModActions.registerAction(actionEntry);
        mechanismID = id;
        enchantScrollTemplates = est;
    }

    public void giveItem(Item mechanism, Item gem, Creature player, EnchantScrollTemplate[] enchantScrollTemplates){
        if(isMechanism(mechanism.getTemplateId()) && player.isPlayer()){

            try {

                int itemID = enchantScrollTemplates[Server.rand.nextInt(enchantScrollTemplates.length)].templateID;


                Item newItem = ItemFactory.createItem(itemID, 55.0F + (Server.rand.nextFloat() * 40.0F), null);
                newItem.setRarity((byte)1);
                //Insert Item
                player.getInventory().insertItem(newItem);
                player.getCommunicator().sendSafeServerMessage("A " + newItem.getName().toLowerCase() + " emerges from the mechanism.");
                Items.destroyItem(gem.getWurmId());
            } catch(Exception ex){

            }
        }
    }

    public boolean isMechanism(int itemTemplateID){
        if(itemTemplateID == mechanismID ){
            return true;
        }
        return false;
    }

    public boolean isStarGem(int itemTemplateID){
        switch (itemTemplateID) {
            case 375:
            case 377:
            case 379:
            case 381:
            case 383:
                return true;
            default:
                return false;
        }
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {

        return new BehaviourProvider() {
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer , Item source , Item target) {


                if (performer instanceof Player && isMechanism(target.getTemplateId()) && isStarGem(source.getTemplateId())) {
                    return Arrays.asList(actionEntry);
                } else {
                    return null;
                }
            }
        };
    }
    @Override
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {

            @Override
            public short getActionId() {
                return actionId;
            }

            @Override
            public boolean action(Action action, Creature performer, Item source, Item target, short act, float counter) {
                try {
                    if (!performer.isWithinDistanceTo(target, 8)) {
                        performer.getCommunicator().sendNormalServerMessage("You need to be closer to the mechanism.");
                        return true;
                    }

                    if (counter == 1.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You start to place the gem into the mechanism.");
                        final int time = 130; // 10th of seconds. 50 means 5 seconds
                        performer.getCurrentAction().setTimeLeft(time);
                        performer.sendActionControl("Placing Gem", true, time);
                    } else if (performer.getCurrentAction().currentSecond() % 7 == 0) {
                        performer.getCommunicator().sendNormalServerMessage("The mechanism makes a strange sound and some parchment violently judders from a crevice.");
                    } else {
                        int time = performer.getCurrentAction().getTimeLeft();
                        if (counter * 10.0f > time) {
                            if (performer instanceof Player && isMechanism(target.getTemplateId())) {

                                if (performer.getDeity() == null) {
                                    performer.getCommunicator().sendNormalServerMessage("You have no deity, causing the parchment to crumple and tear.");
                                    return true;
                                } else {
                                    giveItem(target, source, performer, enchantScrollTemplates);
                                }
                                return true;
                            } else {
                                performer.getCommunicator().sendNormalServerMessage("There is nowhere to place a gem in this.");
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                    return true;
                }
                return false;
            }
        };
    }


}
