package org.talor.wurmunlimited.mods.structures;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.Zones;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class SupplyDepots {

    private static boolean isLight = false;
    private static Logger logger = Logger.getLogger(SupplyDepots.class.getName());
    public static ArrayList<Item> depots = new ArrayList<Item>();
    public static Creature host = null;
    public static long depotRespawnTime = TimeConstants.HOUR_MILLIS*3L;
    //public static long depotRespawnTime = TimeConstants.MINUTE_MILLIS*10L;
    public static long lastSpawnedDepot = 0;
    public static void sendDepotEffect(Player player, Item depot){
        player.getCommunicator().sendAddEffect(depot.getWurmId(), (byte) 25, depot.getPosX(), depot.getPosY(), depot.getPosZ(), (byte) 0);
    }
    public static void sendDepotEffectsToPlayer(Player player){
        logger.info("Sending depot effects to player "+player.getName());
        for(Item depot : depots){
            sendDepotEffect(player, depot);
        }
    }
/*    public static void sendDepotEffectsToPlayers(Item depot){
        for(Player p : Players.getInstance().getPlayers()){
            sendDepotEffect(p, depot);
        }
    }*/
    public static void sendDepotEffectsToPlayers(Item depot){
        removeDepotEffect(depot);
        for(Player p : Players.getInstance().getPlayers()){
            sendDepotEffect(p, depot);
        }
    }

    public static void removeDepotEffect(Item depot){
        for(Player player : Players.getInstance().getPlayers()){
            player.getCommunicator().sendRemoveEffect(depot.getWurmId());
        }
    }
    public static void removeSupplyDepot(Item depot){
        if(depots.contains(depot)){
            depots.remove(depot);
        }
        removeDepotEffect(depot);
        isLight = false;
    }
    private static boolean isSupplyDepot(Item item){
        return item.getTemplateId() == Structures.arenaDepotID;
    }
    public static void pollDepotSpawn(){
        for(int i = 0; i < depots.size(); i++){
            Item depot = depots.get(i);
            if(!Items.exists(depot)){
                logger.info("Supply depot was destroyed, removing from list.");
                depots.remove(depot);
                removeDepotEffect(depot);
                isLight = false;
            }
        }
        for(Item item : Items.getAllItems()){
            if(isLight == false && isSupplyDepot(item) && !depots.contains(item)){
                logger.info("Found existing supply depots, adding to list and sending data to players. item name: " + item.getName() + ", id: " + item.getWurmId());
                depots.add(item);
                sendDepotEffectsToPlayers(item);
                isLight = true;
            }
        }
        if(depots.isEmpty()){
            if(System.currentTimeMillis() > lastSpawnedDepot + depotRespawnTime){
                logger.info("No Depots were found, and the timer has expired. Spawning a new one.");
                boolean spawned = false;
                int i = 0;
                while(!spawned && i < 20){
                    float worldSizeX = Zones.worldTileSizeX;
                    float worldSizeY = Zones.worldTileSizeY;
                    float minX = worldSizeX*0.25f;
                    float minY = worldSizeY*0.25f;
                    int tilex = (int) (minX+(minX*2*Server.rand.nextFloat()));
                    int tiley = (int) (minY+(minY*2*Server.rand.nextFloat()));
                    int tile = Server.surfaceMesh.getTile(tilex, tiley);
                    try {
                        if(Tiles.decodeHeight((int)tile) > 0){

                            Item depot = ItemFactory.createItem(Structures.arenaDepotID, 50+Server.rand.nextFloat()*40f, (float)(tilex << 2) + 2.0f, (float)(tiley << 2) + 2.0f, Server.rand.nextFloat() * 360.0f, true, (byte) 0, -10, null);

                            if (isLight) {
                                removeDepotEffect(depot);
                            }

                            sendDepotEffectsToPlayers(depot);

                            isLight = true;

                            Server.getInstance().broadCastAlert("A supply cache has been delivered by the gods of Valrei.");

                            logger.info("New supply depot being placed at "+tilex+", "+tiley);
                            spawned = true;
                            host = null;
                            lastSpawnedDepot = System.currentTimeMillis();
                            depotRespawnTime = (TimeConstants.HOUR_MILLIS*3L) + (TimeConstants.MINUTE_MILLIS * (long)Server.rand.nextFloat() * 60L);
                            logger.info("setting depotRespawnTime: " + depotRespawnTime);
                           // depotRespawnTime = (TimeConstants.MINUTE_MILLIS*3L);
                        }else{
                            logger.info("Position "+tilex+", "+tiley+" was invalid, attempting another spawn...");
                            i++;
                        }
                    } catch (Exception e) {
                        logger.severe("Failed to create Arena Depot.");
                        e.printStackTrace();
                    }
                }
                if(i >= 20){
                    logger.warning("Could not find a valid location within 20 tries for a supply depot.");
                }
            }
        }
    }

    public static long lastAttemptedDepotCapture = 0;
    public static final long captureMessageInterval = TimeConstants.MINUTE_MILLIS*3L;
    public static void maybeBroadcastOpen(Creature performer){
        if(System.currentTimeMillis() > lastAttemptedDepotCapture + captureMessageInterval){

            Server.getInstance().broadCastAlert(performer.getName() + " is beginning to claim the supply cache!");
            lastAttemptedDepotCapture = System.currentTimeMillis();
        }
    }
    public static void giveCacheReward(Creature performer){
        try {
            Item inv = performer.getInventory();
            int chance = Server.rand.nextInt(10);

            switch(chance) {

                case 5:
                    Item bone = ItemFactory.createItem(ItemList.boneCollar, 55.0F + (Server.rand.nextFloat() * 40.0F), null);
                    bone.setRarity((byte)1);
                    inv.insertItem(bone);
                    break;

                case 6:
                    Item basket = ItemFactory.createItem(ItemList.xmasLunchbox, 55.0F + (Server.rand.nextFloat() * 40.0F), null);
                    inv.insertItem(basket);
                    break;

                default:
                   int[] starGems = { ItemList.emeraldStar, ItemList.rubyStar, ItemList.opalBlack, ItemList.diamondStar, ItemList.sapphireStar };
                    int gemID = starGems[Server.rand.nextInt(starGems.length)];
                    Item gem = ItemFactory.createItem(gemID, 55.0F + (Server.rand.nextFloat() * 40.0F), null);
                    inv.insertItem(gem);
                    break;
            }

            for (int i=0;i<=3;i++) {
                Item newItem = ItemFactory.createItem(ItemList.sleepPowder, 55.0F + (Server.rand.nextFloat() * 40.0F), null);
                inv.insertItem(newItem);
            }

            for (int i=0;i<=3;i++) {
                Item newItem = ItemFactory.createItem(ItemList.arrowWar, 55.0F + (Server.rand.nextFloat() * 40.0F), null);
                inv.insertItem(newItem);
            }

        } catch (FailedException | NoSuchTemplateException e) {
            e.printStackTrace();
        }
    }

}
