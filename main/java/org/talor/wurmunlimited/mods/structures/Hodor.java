package org.talor.wurmunlimited.mods.structures;

import com.wurmonline.server.*;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.*;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Hodor {

    //Config
    //public static long TIME_BETWEEN_HODOR = 129600000;
    //public static long HODOR_MESSAGE_DELAY = 3600000;
    public static long TIME_BETWEEN_HODOR = TimeConstants.HOUR_MILLIS * 84;
    public static long HODOR_MESSAGE_DELAY = 30;
    public static int HODORS_SPAWNED = 4;
    public static int HODORS_REQUIRED = 3;
    public static int HODOR_POINT_DISTANCE = 10;
    public static int HODOR_CAPTURE_TIME = 360;

    public Location[] HODOR_LOCATIONS = Structures.mechanismLocations;
    List<Item> HODOR_ITEMS = new ArrayList<Item>();
    public boolean HODOR_ON;
    private long nextRoundMessage;

    private static Hodor instance = null;
    public static Hodor getInstance() {
        if (instance == null)
            instance = new Hodor();
        return instance;
    }
    public Hodor() {
        HODOR_ON = findHodorLocations();
    }

    private static Logger logger = Logger.getLogger(Hodor.class.getName());
    public void hodorPoll() {
        if (Servers.localServer.getNextHota() > 0L) {
            if (System.currentTimeMillis() > Servers.localServer.getNextHota()) {
                startHodor();
            }
            else if (Servers.localServer.getNextHota() < Long.MAX_VALUE)
            {
                if (nextRoundMessage == Long.MAX_VALUE) {
                    nextRoundMessage = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() >= nextRoundMessage) {
                    Server.getInstance().broadCastSafe(getTimeTillHodorMessage());
                    nextRoundMessage = System.currentTimeMillis() + (Servers.localServer.getNextHota() - System.currentTimeMillis()) / 2L;
                }
            }
        }
    }
    public void capturePillar(Creature player, Item hodor){
        if(!HODOR_ON){
            return;
        }
        if(hodor.getTemplateId() != Structures.hodorPillarID){
            return;
        }
        if(player.getCitizenVillage().getAllianceNumber() == 0) {
            hodor.setData1(player.getCitizenVillage().getId() + 2000000);
        }else{
            hodor.setData1(player.getCitizenVillage().getAllianceNumber());
        }
        Server.getInstance().broadCastSafe(player.getName() + " has captured the " + hodor.getName() + "!");
        HistoryManager.addHistory(player.getName(), "has captured the " + hodor.getName() + "!", true);
        hodor.addEffect(EffectFactory.getInstance().createFire(hodor.getWurmId(), hodor.getPosX(), hodor.getPosY(), hodor.getPosZ(), true));
        checkHodorWin(player);
    }
    public void startHodor(){
        if(HODOR_ON){
            return;
        }
        Servers.localServer.setNextHota(Long.MAX_VALUE);
        //nextRoundMessage = Long.MAX_VALUE;
        HODOR_ON = true;
        deleteHodorItems();
        spawnHodorItems();
        Server.getInstance().broadCastSafe(getHodorStartMessage());
    }
    private String getHodorItemName(byte hodorType){
        switch(hodorType){
            case 0:
                return "None";
            case 1:
                return "Weapon";
            case 2:
                return "Plate";
            case 3:
                return "Chain";
            case 4:
                return "Clay";
            case 5:
                return "Sand";
            case 6:
                return "Brick";
            case 7:
                return "Mortar";
            case 8:
                return "Rock";
            case 9:
                return "Iron";
            case 10:
                return "Steel";
            case 11:
                return "Lumber";
            case 12:
                return "Forestry";
        }
        return "Unknown";
    }
    private String getTimeTillHodorMessage(){
        long timeLeft = Servers.localServer.getNextHota() - System.currentTimeMillis();
        String one = "The ";
        String two = "Hunt of the Ancients ";
        String three = "begins ";
        if (Server.rand.nextBoolean()) {
            one = "The next ";
            if (Server.rand.nextBoolean()) {
                one = "A new ";
            }
        }

        if (Server.rand.nextBoolean()) {
            two = "Hunt ";
            if (Server.rand.nextBoolean()) {
                two = "HotA ";
            }
        }

        if (Server.rand.nextBoolean()) {
            three = "starts ";
            if (Server.rand.nextBoolean()) {
                three = "will begin ";
            }
        }
        return one + two + three + "in " + Server.getTimeFor(timeLeft) + ".";
    }
    private String getHodorStartMessage(){
        switch (Server.rand.nextInt(4)) {
            case 0:
                return "The Hunt of the Ancients has begun!";
            case 1:
                return "Let The Hunt Begin!";
            case 2:
                return "Hunt! Hunt! Hunt!";
            case 3:
                return "The Hunt of the Ancients is on!";
            case 4:
                if (WurmCalendar.isNight()) {
                    return "It's the night of the Hunter!";
                } else
                    return "It's a glorious day for the Hunt!";
            default:
                return "The Hunt of the Ancients has begun!";
        }
    }
    private void spawnHodorItems(){
        List<Location> HODOR_LIST =  new ArrayList<>(Arrays.asList(HODOR_LOCATIONS));
        for(int i=1; i<=HODORS_SPAWNED; i++){
            int randomHodorNumber = Server.rand.nextInt(HODOR_LIST.size() - 1);
            Location randomHodor = HODOR_LIST.get(randomHodorNumber);
            HODOR_LIST.remove(randomHodorNumber);

            try {
                Item hodorObject = ItemFactory.createItem(Structures.hodorPillarID, 99.0f, "Hodor");

                hodorObject.setPosXYZRotation((randomHodor.x - 1) * 4, (randomHodor.y - 1) * 4, Zones.calculateHeight((randomHodor.x - 1) * 4, (randomHodor.y - 1) * 4, true), Server.rand.nextInt(359));
                String hodorName = getHodorItemName(randomHodor.type) + " " + hodorObject.getTemplate().getName();
                String hodorZone = EpicServerStatus.getAreaString(hodorObject.getTileX(), hodorObject.getTileY());
                hodorObject.setName(hodorName);
                hodorObject.setRarity((byte)1);

                hodorObject.setIsNoTake(true);
                hodorObject.setIsIndestructible(true);
                hodorObject.setIsNoMove(true);
                hodorObject.setIsPlanted(true);
                hodorObject.setIsNoImprove(true);
                hodorObject.setIsNoRepair(true);
                hodorObject.setIsNotTurnable(true);
                hodorObject.setIsNotLockable(true);
                hodorObject.setIsNoDrag(true);
                hodorObject.setIsNotSpellTarget(true);
                hodorObject.setHasNoDecay(true);

                Zone z = Zones.getZone(hodorObject.getTileX(), hodorObject.getTileY(), true);
                z.addItem(hodorObject);
                HODOR_ITEMS.add(hodorObject);
                Server.getInstance().broadCastSafe("The " + hodorName + " has spawned in the " + hodorZone + "!");
                HistoryManager.addHistory("Hodor", "spawned the " + hodorName + " in the " + hodorZone + "!", true);

            } catch (NoSuchTemplateException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            } catch (NoSuchZoneException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            } catch (FailedException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }
    private void deleteHodorItems(){
        findHodorLocations();
        for (Item item : HODOR_ITEMS) {
            if(item.getTemplateId() == Structures.hodorPillarID){
                Items.destroyItem(item.getWurmId());
            }
        }
        HODOR_ITEMS.clear();
    }
    private void checkHodorWin(Creature player){
        Map<Integer, Integer> alliances = new HashMap();
        for (Item item : HODOR_ITEMS) {
            if (item.getData1() > 0) {
                Integer nums = alliances.get(item.getData1());
                if (nums != null) {
                    nums = nums.intValue() + 1;
                } else {
                    nums = 1;
                }
                alliances.put(item.getData1(), nums);
                if (nums.intValue() >= HODORS_REQUIRED) {
                    hodorWin(item.getData1(), player);
                }
            }
        }
    }
    public void createHodorPrize(Village vill, int totalWins){

        try
        {
            Item statue = ItemFactory.createItem(742, 99.0F, null);
            byte material = 7;

            statue.setMaterial(material);
            float posX = vill.getToken().getPosX() - 2.0F + Server.rand.nextFloat() * 4.0F;
            float posY = vill.getToken().getPosY() - 2.0F + Server.rand.nextFloat() * 4.0F;
            statue.setPosXYZRotation(posX, posY, Zones.calculateHeight(posX, posY, true), Server.rand.nextInt(350));

            for (int i = 0; i < totalWins; i++) {
                if (i / 11 == totalWins % 11)
                {
                    statue.setAuxData((byte)0);
                    statue.setData1(1);
                }
                else
                {
                    statue.setAuxData((byte)totalWins);
                }
            }

            int r = Server.rand.nextInt(254);
            int g = Server.rand.nextInt(254);
            int b = Server.rand.nextInt(254);

            statue.setColor(WurmColor.createColor(r, g, b));
            statue.getColor();
            Zone z = Zones.getZone(statue.getTileX(), statue.getTileY(), true);

            double kingdomInfluence = (double)Zones.getPercentLandForKingdom(vill.kingdom);
            logger.info("Awarding hota prize, village's kingdom has " + kingdomInfluence + "% influence");

            for (int i = 0; i<3;i++) {
                Item riftCrystal = ItemFactory.createItem(ItemList.riftCrystal, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                statue.insertItem(riftCrystal);
                Item riftStone = ItemFactory.createItem(ItemList.riftStone, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                statue.insertItem(riftStone);
                Item riftWood = ItemFactory.createItem(ItemList.riftWood, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                statue.insertItem(riftWood);
            }

            if (kingdomInfluence >= 2) {
                Item riftCrystal = ItemFactory.createItem(ItemList.riftCrystal, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                statue.insertItem(riftCrystal);
                Item riftStone = ItemFactory.createItem(ItemList.riftStone, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                statue.insertItem(riftStone);
                Item riftWood = ItemFactory.createItem(ItemList.riftWood, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                statue.insertItem(riftWood);
                Item drakeHide = ItemFactory.createItem(ItemList.drakeHide, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                drakeHide.setWeight(100, true);
                drakeHide.setData2(CreatureTemplateCreator.getRandomDrakeId());
                statue.insertItem(drakeHide);
            }

             if (kingdomInfluence >= 5) {
                 Item riftCrystal = ItemFactory.createItem(ItemList.riftCrystal, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                 statue.insertItem(riftCrystal);
                 Item riftStone = ItemFactory.createItem(ItemList.riftStone, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                 statue.insertItem(riftStone);
                 Item riftWood = ItemFactory.createItem(ItemList.riftWood, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                 statue.insertItem(riftWood);
                 Item scale = ItemFactory.createItem(ItemList.dragonScale, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                 scale.setWeight(100, true);
                 scale.setData2(getRandomDragonID());
                 statue.insertItem(scale);
            }

            if (kingdomInfluence >= 10) {
                Item riftCrystal = ItemFactory.createItem(ItemList.riftCrystal, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                statue.insertItem(riftCrystal);
                Item riftStone = ItemFactory.createItem(ItemList.riftStone, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                statue.insertItem(riftStone);
                Item riftWood = ItemFactory.createItem(ItemList.riftWood, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                statue.insertItem(riftWood);
                Item drakeHide = ItemFactory.createItem(ItemList.drakeHide, 50.0F + (Server.rand.nextFloat() * 40.0F), null);
                drakeHide.setWeight(100, true);
                drakeHide.setData2(CreatureTemplateCreator.getRandomDrakeId());
                statue.insertItem(drakeHide);
            }

            z.addItem(statue);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }

    }



    public void addAllianceHodorWin(PvPAlliance winAlliance) {
        winAlliance.setWins(winAlliance.getNumberOfWins() + 1);
        for (final Village vill : winAlliance.getVillages()) {
            vill.addHotaWin();
            if (vill.getAllianceNumber() == vill.getId()) {
                createHodorPrize(vill, winAlliance.getNumberOfWins());
            }
        }
    }
    private void hodorWin(int allianceNumber, Creature winner){

        final PvPAlliance winAlliance = PvPAlliance.getPvPAlliance(allianceNumber);
        if (winAlliance != null) {
            Server.getInstance().broadCastSafe(winner.getName() + " has secured victory for " + winAlliance.getName() + "!");
            HistoryManager.addHistory(winner.getName(), "has secured victory for " + winAlliance.getName() + "!", true);
            addAllianceHodorWin(winAlliance);
        }
        else {
            try {
                final Village v = Villages.getVillage(allianceNumber - 2000000);
                Server.getInstance().broadCastSafe(winner.getName() + " has secured victory for " + v.getName() + "!");
                HistoryManager.addHistory(winner.getName(), "has secured victory for " + v.getName() + "!", true);
                v.addHotaWin();
                createHodorPrize(v, v.getHotaWins());
            }
            catch (NoSuchVillageException e) {
                logger.log(Level.WARNING, e.getMessage(), (Throwable)e);
            }
        }
        HODOR_ON = false;
        deleteHodorItems();
        Servers.localServer.setNextHota(System.currentTimeMillis() + TIME_BETWEEN_HODOR);
        nextRoundMessage = System.currentTimeMillis() + HODOR_MESSAGE_DELAY;
    }

    public void resetHodor(){
        resetHodor(TIME_BETWEEN_HODOR);
    }

    public void resetHodor(long time) {
        HODOR_ON = false;
        deleteHodorItems();
        Servers.localServer.setNextHota(System.currentTimeMillis() + time);
        nextRoundMessage = System.currentTimeMillis() + HODOR_MESSAGE_DELAY;
    }

    private boolean findHodorLocations(){
        HODOR_ITEMS.clear();
        for(Item item : Items.getAllItems()){
            if(item.getTemplateId() == Structures.hodorPillarID){
                HODOR_ITEMS.add(item);
            }
        }
        if(HODOR_ITEMS.size() > 0){
            return true;
        }
        return false;

    }

    private int getRandomDragonID() {

        int rnd = Server.rand.nextInt(7);

        switch(rnd) {
            case 0:
                return 16;
            case 1:
                return 17;
            case 2:
                return 18;
            case 3:
                return 19;
            case 4:
                return 89;
            case 5:
                return 90;
            case 6:
                return 91;
            case 7:
            default:
                return 92;
        }

    }
}
