package org.talor.wurmunlimited.mods.structures;


import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NoSuchActionException;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenCrate implements ModAction {
    private static OpenCrate instance = null;
    private int crateID;
    private int smallCrateID;
    int[] normalItems = {ItemList.sleepPowder, ItemList.compass, ItemList.potionIllusion,ItemList.snowLantern, ItemList.maskEnlightended, ItemList.maskRavager, ItemList.maskPale, ItemList.maskShadow, ItemList.maskChallenge, ItemList.maskIsles, ItemList.goldGreatHelmHorned, ItemList.openPlumedHelm, ItemList.helmetCavalier,  ItemList.gardenGnome, ItemList.candelabra, ItemList.ring, ItemList.necklace, ItemList.horseShoe, ItemList.saddle, ItemList.spyglass, ItemList.axeSmall,ItemList.swordLong,ItemList.swordShort,ItemList.axeMedium,ItemList.sickle,ItemList.maulSmall, ItemList.swordTwoHander,ItemList.scythe,ItemList.maulLarge,ItemList.clubHuge,ItemList.spearLong,ItemList.halberd, ItemList.emeraldStar, ItemList.rubyStar, ItemList.opalBlack, ItemList.diamondStar, ItemList.sapphireStar, ItemList.fireworks, ItemList.potionWeaponSmithing, ItemList.potionRopemaking, ItemList.potionMining, ItemList.potionTailoring, ItemList.potionArmourSmithing, ItemList.potionFletching, ItemList.potionBlacksmithing, ItemList.potionLeatherworking, ItemList.potionShipbuilding, ItemList.potionStonecutting, ItemList.potionMasonry, ItemList.potionWoodcutting, ItemList.potionCarpentry, ItemList.dioptra  };
    public int[] junkList = { ItemList.arrowWar, ItemList.arrowHunting, ItemList.metalRivet, ItemList.nailsIronSmall, ItemList.nailsIronLarge, ItemList.coinCopperFive, ItemList.rope, ItemList.wemp };
    private ArrayList<Integer> lootItemsList = new ArrayList<Integer>();

    private static Logger logger = Logger.getLogger(OpenCrate.class.getName());
    private final short actionId;
    private final ActionEntry actionEntry;

    public OpenCrate(int id, int smallID, ScrollTemplate[] scrollTemplates) {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Open Box", "opening", new int[] { 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */});
        ModActions.registerAction(actionEntry);
        crateID = id;
        smallCrateID = smallID;

        for(int i=0;i<normalItems.length;i++) {
            lootItemsList.add(normalItems[i]);
        }
        for(int i=0;i<scrollTemplates.length;i++) {
            lootItemsList.add(scrollTemplates[i].templateID);
        }
    }

    public void giveItem(Item crate, Creature player){
        if(isCrate(crate.getTemplateId()) && player.isPlayer()){

            try {

                int itemID = lootItemsList.get(Server.rand.nextInt(lootItemsList.size()));
                Item newItem = ItemFactory.createItem(itemID, 55.0F + (Server.rand.nextFloat() * 40.0F), null);

                switch(itemID) {
                    case ItemList.candelabra:
                    case ItemList.ring:
                    case ItemList.necklace:
                        newItem.setMaterial(Materials.MATERIAL_GLIMMERSTEEL);
                        newItem.setRarity((byte)0);
                        break;
                    case ItemList.snowLantern:
                    case ItemList.maskEnlightended:
                    case ItemList.maskRavager:
                    case ItemList.maskPale:
                    case ItemList.maskShadow:
                    case ItemList.maskChallenge:
                    case ItemList.maskIsles:
                    case ItemList.goldGreatHelmHorned:
                    case ItemList.openPlumedHelm:
                    case ItemList.helmetCavalier:
                        newItem.setRarity((byte)0);
                        break;
                    default:
                        newItem.setRarity((byte)1);
                        break;
                }

                //Insert Item
                player.getInventory().insertItem(newItem);
                player.getCommunicator().sendSafeServerMessage("You open the loot box - inside you find a " + newItem.getName().toLowerCase() + ".");
                Items.destroyItem(crate.getWurmId());
            } catch(Exception ex){

            }
        } else {

            try {

                Item cotton = ItemFactory.createItem(ItemList.cotton, 55.0F + (Server.rand.nextFloat() * 40.0F), null);
                player.getInventory().insertItem(cotton);

                String message = "You open the loot box inside you find a cotton, a ";
                for (int i=0;i<=3;i++) {
                    int itemID = junkList[Server.rand.nextInt(junkList.length)];
                    Item newItem = ItemFactory.createItem(itemID, 55.0F + (Server.rand.nextFloat() * 40.0F), null);

                    //Insert Item
                    player.getInventory().insertItem(newItem);
                    if (i==3) {

                    }
                    message = message + newItem.getName().toLowerCase();
                    if (i==2) {
                        message = message + " and a ";
                    } else if (i<2) {
                        message = message + ", a ";
                    } else {
                        message = message + ".";
                    }
                }
                Items.destroyItem(crate.getWurmId());
                player.getCommunicator().sendSafeServerMessage(message);
            } catch(Exception ex){

            }

        }
    }

    public boolean isCrate(int ItemTemplateID){
        if(ItemTemplateID == crateID ){
            return true;
        }
        return false;
    }

    public boolean isSmallCrate(int ItemTemplateID){
        if(ItemTemplateID == smallCrateID ){
            return true;
        }
        return false;
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {

        return new BehaviourProvider() {
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer , Item source , Item target) {
                return getBehavioursFor(performer, target);
            }
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer ,Item target) {


                if (performer instanceof Player && (isSmallCrate(target.getTemplateId())  || isCrate(target.getTemplateId()))) {
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
                return action(action, performer, target, act, counter);
            }
            @Override
            public boolean action(Action action, Creature performer, Item target, short act, float counter) {
                try {

                    if (!performer.isWithinDistanceTo(target, 8)) {
                        performer.getCommunicator().sendNormalServerMessage("You need to be closer to the loot box.");
                        return true;
                    }

                    if (performer.getVehicle() != -10L) {
                        performer.getCommunicator().sendNormalServerMessage("You need to be on solid ground to do that.");
                        return true;
                    }

                    if (counter == 1.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You start to open the loot box.");
                        final int time = 100; // 10th of seconds. 50 means 5 seconds
                        performer.getCurrentAction().setTimeLeft(time);
                        performer.sendActionControl("Opening loot box", true, time);
                    } else if (performer.getCurrentAction().currentSecond() % 7 == 0) {
                        performer.playPersonalSound("sound.fx.drumroll");
                    } else {
                        int time = performer.getCurrentAction().getTimeLeft();
                        if (counter * 10.0f > time) {
                            if (performer instanceof Player && (isSmallCrate(target.getTemplateId())  || isCrate(target.getTemplateId()))) {
                                giveItem(target, performer);
                                return true;
                            } else {
                                performer.getCommunicator().sendNormalServerMessage("That's not a loot box!");
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