package org.talor.wurmunlimited.mods.structures;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.players.Player;


public class ArenaSupplyDepotAction implements ModAction {
    private static Logger logger = Logger.getLogger(ArenaSupplyDepotAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;

    public ArenaSupplyDepotAction() {
        logger.log(Level.WARNING, "ArenaSupplyDepotAction()");

        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Claim Supply Cache",
                "claiming",
                new int[] { 6 /* ACTION_TYPE_NOMOVE */ }	// 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
        );
        ModActions.registerAction(actionEntry);
    }


    @Override
    public BehaviourProvider getBehaviourProvider()
    {
        return new BehaviourProvider() {
            // Menu with activated object
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object)
            {
                return this.getBehavioursFor(performer, object);
            }

            // Menu without activated object
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer, Item object)
            {
                if(performer instanceof Player && object != null && object.getTemplateId() == Structures.arenaDepotID) {
                    return Arrays.asList(actionEntry);
                }

                return null;
            }
        };
    }

    @Override
    public ActionPerformer getActionPerformer()
    {
        return new ActionPerformer() {

            @Override
            public short getActionId() {
                return actionId;
            }

            // Without activated object
            @Override
            public boolean action(Action act, Creature performer, Item target, short action, float counter)
            {
                try{
                    if(performer instanceof Player){
                        if(target.getTemplate().getTemplateId() != Structures.arenaDepotID){
                            performer.getCommunicator().sendNormalServerMessage("That is not a supply cache.");
                            return true;
                        }
                        if(!performer.isWithinDistanceTo(target, 5)){
                            performer.getCommunicator().sendNormalServerMessage("You must be closer to claim the supply cache.");
                            return true;
                        }
                        if(!Items.exists(target)){
                            performer.getCommunicator().sendNormalServerMessage("The supply cache has already been captured.");
                            return true;
                        }
                        if(counter == 1.0f){
                            performer.getCommunicator().sendNormalServerMessage("You begin to claim the cache.");
                            Server.getInstance().broadCastAction(performer.getName() + " begins claiming the cache.", performer, 50);
                           act.setTimeLeft(3600);
                            //act.setTimeLeft(600);
                            performer.sendActionControl("Claiming", true, act.getTimeLeft());
                            SupplyDepots.maybeBroadcastOpen(performer);
                        }else if(counter * 10f > performer.getCurrentAction().getTimeLeft()){

                            SupplyDepots.giveCacheReward(performer);

                            performer.getCommunicator().sendSafeServerMessage("You have successfully claimed the cache!");
                            Server.getInstance().broadCastAction(performer.getName() + " successfully claims the cache!", performer, 50);
                            Server.getInstance().broadCastAlert(performer.getName() + " has claimed a supply cache!");
                            SupplyDepots.removeSupplyDepot(target);
                            Items.destroyItem(target.getWurmId());
                            return true;
                        }
                    }else{
                        logger.info("Somehow a non-player activated a Arrow Pack Unpack...");
                    }
                    return false;
                }catch(Exception e){
                    e.printStackTrace();
                    return true;
                }
            }

            @Override
            public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
            {
                return this.action(act, performer, target, action, counter);
            }


        }; // ActionPerformer
    }
}