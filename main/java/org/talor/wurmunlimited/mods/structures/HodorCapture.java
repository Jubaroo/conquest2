package org.talor.wurmunlimited.mods.structures;


        import com.wurmonline.server.Players;
        import com.wurmonline.server.Server;
        import com.wurmonline.server.behaviours.Action;
        import com.wurmonline.server.behaviours.ActionEntry;
        import com.wurmonline.server.behaviours.NoSuchActionException;
        import com.wurmonline.server.creatures.Creature;
        import com.wurmonline.server.items.Item;
        import com.wurmonline.server.players.Player;
        import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
        import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
        import org.gotti.wurmunlimited.modsupport.actions.ModAction;
        import org.gotti.wurmunlimited.modsupport.actions.ModActions;

        import java.util.Arrays;
        import java.util.List;
        import java.util.logging.Level;
        import java.util.logging.Logger;


public class HodorCapture implements ModAction {
    private static HodorCapture instance = null;
    private static int hodorPillarID;

    public static HodorCapture getInstance()
    {
        if (instance == null)
            instance = new HodorCapture(0);
        return instance;
    }

    private static Logger logger = Logger.getLogger(HodorCapture.class.getName());
    private final short actionId;
    private final ActionEntry actionEntry;

    public HodorCapture( int hpid) {
        hodorPillarID = hpid;
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Capture", "capturing", new int[] { 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 37 /* ACTION_TYPE_NEVER_USE_ACTIVE_ITEM */});
        ModActions.registerAction(actionEntry);
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
                boolean CanCapture = false;
                if (performer instanceof Player && target.getTemplateId() == hodorPillarID) {
                    CanCapture = true;
                }
                if (CanCapture) {
                    return Arrays.asList(actionEntry);
                }
                return null;
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

                if(!Hodor.getInstance().HODOR_ON){
                    performer.getCommunicator().sendNormalServerMessage("The hunt has not started yet!");
                    return true;
                }
                if(performer instanceof Player == false || target.getTemplateId() != hodorPillarID){
                    performer.getCommunicator().sendNormalServerMessage("This is not a hunt item!");
                    return true;
                }
                if (performer.getCitizenVillage() == null) {
                    performer.getCommunicator().sendNormalServerMessage("Your have no village or alliance and can't assume control of the " + target.getName() + ".");
                    return true;
                }
                if(performer.getCitizenVillage().getAllianceNumber() == 0) {
                    if (performer.getCitizenVillage().getId() + 2000000 == target.getData1()) {
                        performer.getCommunicator().sendNormalServerMessage("You own this already!");
                        return true;
                    }
                } else if (performer.getCitizenVillage().getAllianceNumber() == target.getData1()) {
                        performer.getCommunicator().sendNormalServerMessage("You own this already!");
                        return true;
                }
                if(performer.rangeTo(performer, target) > Hodor.HODOR_POINT_DISTANCE){
                    performer.getCommunicator().sendNormalServerMessage("To far to capture this flag!");
                    return true;
                }

                for (Player p : Players.getInstance().getPlayers())
                {
                    if (p.getWurmId() != performer.getWurmId()) {
                        try
                        {
                            Action pact = p.getCurrentAction();
                            if ((action.getNumber() == pact.getNumber()) && (action.getTarget() == pact.getTarget())) {
                                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already being captured by " + p.getName() + ".", (byte)3);
                                return true;
                            }
                        }
                        catch (NoSuchActionException e) {
                            logger.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                }

                try {
                    if (counter == 1.0f) {
                        int time = Hodor.HODOR_CAPTURE_TIME;
                        performer.getCommunicator().sendNormalServerMessage("You start capturing the " + target.getName() + "!");
                        Server.getInstance().broadCastSafe(performer.getName() + " is capturing the " + target.getName() + "!");
                        performer.getCurrentAction().setTimeLeft(time * 10);
                        performer.sendActionControl("Capturing the " + target.getName() + "!",true, time * 10);
                        performer.setSecondsToLogout(1200);

                    } else {
                        int time = performer.getCurrentAction().getTimeLeft();

                        if (counter * 10.0f <= time) {
                            if (action.justTickedSecond() && ((int)counter % 60 == 0)) {
                                performer.getCommunicator().sendNormalServerMessage("You continue to capture the " + target.getName() + "!");
                            }
                        }else{
                            //Captured Hodor
                            Hodor.getInstance().capturePillar(performer, target);
                            return true;
                        }
                    }
                    return false;
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                    return true;
                }
            }
        };
    }
}


