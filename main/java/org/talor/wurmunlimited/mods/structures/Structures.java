package org.talor.wurmunlimited.mods.structures;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.*;
import com.wurmonline.server.creatures.*;
import com.wurmonline.server.epic.EpicMissionEnum;
import com.wurmonline.server.items.*;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.KarmaQuestion;
import com.wurmonline.server.questions.VillageFoundationQuestion;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import javassist.CtClass;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;


import org.gotti.wurmunlimited.modsupport.creatures.ModCreatures;


public class Structures implements WurmServerMod, Configurable,  PreInitable, Initable, ServerStartedListener, ItemTemplatesCreatedListener, ItemTypes, MiscConstants, PlayerMessageListener  {

    // Configuration default values
    private boolean enableStructures = true;

    private static Logger logger = Logger.getLogger(Structures.class.getName());


    // Timers to poll repeated actions
    // ******   REAL TIMERS     *****
    private static long delayFogGoblins = 300000L;
    private static long delayTradeTents = 600000L;
    private static long delayResourcePoints = 60000L;
    private static long delayLootCarpets = 3600000L;
    private static long delayMobSpawners = 600000L;
    private static long delayAthanorMechanism = 3600000L;
    // *****    TEST TIMERS     *****
/*    private static long delayFogGoblins = 60000L;
    private static long delayTradeTents = 60000L;
    private static long delayResourcePoints = 60000L;
    private static long delayLootCarpets = 60000L;
    private static long delayMobSpawners = 60000L;
    private static long delayAthanorMechanism = 60000L;*/


    private static long lastPolledFogGoblins = 0;
    private static long lastPolledTradeTents = 0;
    private static long lastPolledDepots = 0;
    private static long lastPolledResourcePoints = 0;
    private static long lastPolledLootCarpets = 0;
    private static long lastPolledMobSpawners = 0;
    private static long lastPolledAthanorMechanism = 0;

    private static boolean initialGoblinCensus = false;
    private static int maxFogGoblins = 100;
    private static ArrayList<Creature> fogGoblins = new ArrayList<Creature>();
    private int fogGoblinID = 2147483636;

    // Items to act upon
    private ArrayList<Item> tradeTents = new ArrayList<Item>();
    private ArrayList<Item> lootCarpets = new ArrayList<Item>();
    private ArrayList<Item> mobSpawners = new ArrayList<Item>();
    private ArrayList<Item> resourcePoints = new ArrayList<Item>();
    private Item athanorMechanism;

    private int tradeGoodsID;
    private int tradeTentID;
    private int lootBoxID;
    private int smallLootBoxID;
    private int lootFlagID;
    private int smallLootFlagID;
    private int mechanismID;
    public static int arenaDepotID;
    public static int arenaCacheID;
    public static int banditID;
    public static int hodorPillarID;

    public static int snakeCultistID;
    public static final byte DEPO_NONE = 0;
    public static final byte DEPO_WEAPON = 1;
    public static final byte DEPO_PLATE = 2;
    public static final byte DEPO_CHAIN = 3;
    public static final byte DEPO_CLAY = 4;
    public static final byte DEPO_SAND = 5;
    public static final byte DEPO_BRICKS = 6;
    public static final byte DEPO_MORTAR = 7;
    public static final byte DEPO_ROCKS = 8;
    public static final byte DEPO_IRON = 9;
    public static final byte DEPO_STEEL = 10;
    public static final byte DEPO_LUMBER = 11;
    public static final byte DEPO_FORESTRY = 12;
    public static Location[] mechanismLocations = { new Location(881, 1276, DEPO_CHAIN), new Location( 949, 1439, DEPO_CLAY), new Location(635,1315, DEPO_SAND), new Location(669,1112, DEPO_BRICKS), new Location(587,973, DEPO_LUMBER),  new Location(614,851, DEPO_MORTAR),  new Location(690,645, DEPO_ROCKS),  new Location(754,568, DEPO_FORESTRY),  new Location(810,820, DEPO_STEEL),  new Location(856,859, DEPO_WEAPON),  new Location(954,774, DEPO_CHAIN),  new Location(955,602, DEPO_CLAY),  new Location(1257,717, DEPO_IRON),  new Location(1323,590, DEPO_SAND),  new Location(1404,712, DEPO_BRICKS),  new Location(1336,869, DEPO_MORTAR),  new Location(1126,803, DEPO_PLATE),  new Location(1440,1079, DEPO_LUMBER),  new Location(1313,1240, DEPO_STEEL),  new Location(1426,1448, DEPO_ROCKS),  new Location(1198, 1349, DEPO_FORESTRY),  new Location(1124,1107, DEPO_WEAPON),   new Location(879,1193, DEPO_PLATE),   new Location(782,1433, DEPO_IRON)  };


    private StructureTemplate[] structureTemplates = {
            new StructureTemplate("Brick Factory", "sound.work.stonecutting", "model.structure.war.supplydepot.2.0", ItemList.stoneBrick, ItemList.rock, 0, 20, 1000, 60000L),
            new StructureTemplate("Lumber Mill", "sound.work.carpentry.saw", "model.structure.war.supplydepot.2.0", ItemList.plank, ItemList.log, 0, 20, 1000, 60000L),
            new StructureTemplate("Forestry", "sound.work.woodcutting1", "model.structure.war.supplydepot.2.0", ItemList.log, 0, 0, 20, 1000, 60000L),
            new StructureTemplate("Quarry", "sound.work.mining1", "model.structure.war.supplydepot.2.0", ItemList.rock, 0, 0, 20, 1000, 60000L),
            new StructureTemplate("Clay Pit", "sound.work.digging1", "model.structure.war.supplydepot.2.0", ItemList.clay, 0, 0, 20, 1000, 60000L),
            new StructureTemplate("Sand Pit", "sound.work.digging1", "model.structure.war.supplydepot.2.0", ItemList.sand, 0, 0, 20, 1000, 60000L),
            new StructureTemplate("Steel Mill", "sound.fire.lighting.flintsteel", "model.structure.war.supplydepot.2.0", ItemList.steelBar, ItemList.ironBar, 0, 20, 1000, 60000L),
            new StructureTemplate("Iron Mine", "sound.work.mining1", "model.structure.war.supplydepot.2.0", ItemList.ironBar, 0, 0, 20, 1000, 60000L),
            new StructureTemplate("Mortar Factory", "sound.work.mining1", "model.structure.war.supplydepot.2.0", ItemList.mortar, ItemList.clay, ItemList.sand, 20, 1000, 60000L),
            new StructureTemplate("Chain Smithy", "sound.work.smithing.metal", "model.structure.war.supplydepot.2.0", 274, 46, 0, 1, 100, 60000L),
            new StructureTemplate("Plate Smithy", "sound.work.smithing.metal", "model.structure.war.supplydepot.2.0", 280, 205, 0, 1, 100, 60000L),
            new StructureTemplate("Weapon Smithy", "sound.work.smithing.metal", "model.structure.war.supplydepot.2.0", 2, 46, 0, 1, 100, 60000L),
    };

    private SpawnerTemplate[] spawnerTemplates = {

            // Ungrouped mobs
            new SpawnerTemplate("Bandit Totem", "sound.fx.humm", "model.fireplace.campfire.", 2147483647, 5, 600000L),

            // Snake mob group
            new SpawnerTemplate("Cobra Totem", "sound.fx.humm", "model.decoration.amphora.large.pottery.", 2147483635, 4, 600000L),
            new SpawnerTemplate("Viper Totem", "sound.fx.humm", "model.decoration.amphora.large.pottery.", 2147483641, 4, 600000L), // add
            new SpawnerTemplate("Snake Cultist Totem", "sound.fx.humm", "model.decoration.amphora.small.pottery.", 2147483642, 4, 600000L), // add
            new SpawnerTemplate("Giant Serpent Totem", "sound.fx.humm", "model.decoration.altar.silver.", 2147483637, 1, 6000000L), // add

            // Pyramid mob group
            new SpawnerTemplate("Scorpion Totem", "sound.fx.humm", "model.decoration.altar.stone.", 59, 2, 900000L),
            new SpawnerTemplate("Cave Bug Totem", "sound.fx.humm", "model.decoration.altar.stone.", 43, 2, 900000L),
            new SpawnerTemplate("Temple Guardian Totem", "sound.fx.humm", "model.decoration.altar.stone.", 2147483633, 1, 900000L),
            new SpawnerTemplate("Temple Priest Totem", "sound.fx.humm", "model.decoration.altar.stone.", 2147483632, 1, 900000L),
            new SpawnerTemplate("Temple Patriarch Totem", "sound.fx.humm", "model.decoration.altar.stone.", 2147483631, 1, 900000L),
            new SpawnerTemplate("Rift Beast Totem", "sound.fx.humm", "model.decoration.altar.stone.", 106, 1, 900000L),

            // Goblin mob group
            new SpawnerTemplate("Goblin Totem", "sound.fx.humm", "model.fireplace.campfire.", 23, 4, 600000L),
            new SpawnerTemplate("Rat Totem", "sound.fx.humm", "model.container.storagebin.trash.empty.birchwood.", 13, 2, 600000L),
            new SpawnerTemplate("Goblin Warlord Totem", "sound.fx.humm", "model.fireplace.campfire.", 2147483639, 1, 1200000L), // add
            new SpawnerTemplate("Troll Totem", "sound.fx.humm", "model.fireplace.campfire.", 11, 1, 1200000L),

            // Undead mob group
            new SpawnerTemplate("Wraith Totem", "sound.fx.humm", "model.decoration.altar.stone.", 2147483638, 1, 600000L), // add
            new SpawnerTemplate("Zombie Totem", "sound.fx.humm", "model.decoration.altar.stone.", 2147483630, 4, 600000L),
            new SpawnerTemplate("Skeleton Totem", "sound.fx.humm", "model.furniture.coffin.stone.", 2147483629, 4, 600000L),
            new SpawnerTemplate("Necromancer Totem", "sound.fx.humm", "model.decoration.altar.silver.", 2147483640, 1, 6000000L), // add

            // Fire mob group
            new SpawnerTemplate("Lavafiend Totem", "sound.fx.humm", "model.resource.boulder.adamantine.", 57, 2, 1200000L),
            new SpawnerTemplate("Fire Spider Totem", "sound.fx.humm", "model.resource.boulder.adamantine.", 2147483634, 2, 1200000L),
            new SpawnerTemplate("Hell hound Totem", "sound.fx.humm", "model.resource.ore.adamantine.", 84, 2, 1200000L),
            new SpawnerTemplate("Hell scorpious Totem", "sound.fx.humm", "model.resource.boulder.adamantine.", 85, 2, 1200000L),
            new SpawnerTemplate("Sol demon Totem", "sound.fx.humm", "model.resource.boulder.adamantine.", 56, 2, 1200000L),
    };

    private ScrollTemplate[] scrollTemplates = {
            new ScrollTemplate("faith scroll", "A scroll that grants faith.", "model.writ.", 2147483645, 10.0f),
            new ScrollTemplate("body strength scroll", "A scroll that grants body strength.", "model.writ.", 102, 1.0f),
            new ScrollTemplate("body stamina scroll", "A scroll that grants body stamina.", "model.writ.", 103, 1.0f),
            new ScrollTemplate("body control scroll", "A scroll that grants body control", "model.writ.", 104, 1.0f),
            new ScrollTemplate("soul strength scroll", "A scroll that grants soul strength.", "model.writ.", 105, 1.0f),
            new ScrollTemplate("soul depth scroll", "A scroll that grants soul depth.", "model.writ.", 106, 1.0f),
            new ScrollTemplate("mind logic scroll", "A scroll that grants mind logic.", "model.writ.", 100, 1.0f),
            new ScrollTemplate("mind speed scroll", "A scroll that grants mind speed.", "model.writ.", 101, 1.0f),
            new ScrollTemplate("affinity scroll", "A scroll that grants a random affinity.", "model.writ.", 0, 0.2f),
    };

    private EnchantScrollTemplate[] enchantScrollTemplates = {
            new EnchantScrollTemplate("aura of shared pain scroll", "A scroll on strange green and white parchment.", "model.writ.", 17),
            new EnchantScrollTemplate("life transfer scroll", "A scroll on strange green and white parchment.", "model.writ.", 26),
            new EnchantScrollTemplate("nimbleness scroll", "A scroll on strange green and white parchment.", "model.writ.", 32),
            new EnchantScrollTemplate("wind of ages scroll", "A scroll on strange green and white parchment.", "model.writ.", 16),
            new EnchantScrollTemplate("frostbrand scroll", "A scroll on strange green and white parchment.", "model.writ.", 33),
            new EnchantScrollTemplate("mindstealer scroll", "A scroll on strange green and white parchment.", "model.writ.", 31),
            new EnchantScrollTemplate("venom scroll", "A scroll on strange green and white parchment.", "model.writ.", 27),
            new EnchantScrollTemplate("flaming aura scroll", "A scroll on strange green and white parchment.", "model.writ.", 14),
            new EnchantScrollTemplate("circle of cunning scroll", "A scroll on strange green and white parchment.", "model.writ.", 13),
            new EnchantScrollTemplate("web armour scroll", "A scroll on strange green and white parchment.", "model.writ.", 46),
            new EnchantScrollTemplate("rotting touch scroll", "A scroll on strange green and white parchment.", "model.writ.", 18),
            new EnchantScrollTemplate("bloodthirst scroll", "A scroll on strange green and white parchment.", "model.writ.", 45),
            new EnchantScrollTemplate("blessings of the dark scroll", "A scroll on strange green and white parchment.", "model.writ.", 47),
    };


	@Override
	public void configure(Properties properties) {
        // Check .properties file for configuration values
        enableStructures = Boolean.parseBoolean(properties.getProperty("enableStructures", Boolean.toString(enableStructures)));
	}

    @Override
    public boolean onPlayerMessage(Communicator communicator, String msg) {
        if (msg.startsWith("/resethodor") && communicator.getPlayer().getPower() == 5) {

            String[] splitStr = msg.trim().split("\\s+");
            double time = 0;
            try {
                if (splitStr.length > 1) {
                    time = Double.parseDouble(splitStr[1]) * TimeConstants.HOUR_MILLIS;
                }
            } catch (NumberFormatException nfe) {
                logger.info (communicator.getPlayer().getName() + " tried to reset hota but gave a bad time.");
                return true;
            }

            if (time == 0) {
                //Reset and enable hodor
                Hodor.getInstance().resetHodor();
                communicator.sendSafeServerMessage("Hodor reset.");
                logger.info (communicator.getPlayer().getName() + " reset hota using default timers.");
                return true;
            } else {
                //Reset and enable hodor using custom timer
                Hodor.getInstance().resetHodor((long) time);
                communicator.sendSafeServerMessage("Hodor reset with custom time.");
                logger.info (communicator.getPlayer().getName() + " reset hota to start in  " + (time/1000) + " seconds.");
                return true;
            }


        }
        return false;
    }

    @Override
    public void onItemTemplatesCreated() {

        try {
                String name = "supply cache";
                ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.arenacache");
                itemBuilder.name(name, "supply caches", "A supply cache delivered by the gods of Valrei.");
                itemBuilder.itemTypes(new short[]{
                        ItemTypes.ITEM_TYPE_MAGIC,
                        ItemTypes.ITEM_TYPE_FULLPRICE,
                        ItemTypes.ITEM_TYPE_NOSELLBACK,
                        ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
                });
                itemBuilder.imageNumber((short) 243);
                itemBuilder.behaviourType((short) 1);
                itemBuilder.combatDamage(0);
                itemBuilder.decayTime(Long.MAX_VALUE);
                itemBuilder.dimensions(1, 1, 1);
                itemBuilder.primarySkill((int) NOID);
                itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
                itemBuilder.modelName("model.container.giftbox.");
                itemBuilder.difficulty(5.0f);
                itemBuilder.weightGrams(500);
                itemBuilder.material(Materials.MATERIAL_GOLD);
                itemBuilder.value(10000);
                itemBuilder.isTraded(true);

                ItemTemplate template = itemBuilder.build();
                arenaCacheID = template.getTemplateId();
                logger.info(name+" TemplateID: "+arenaCacheID);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            String name = "supply cache";
            ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.arena.depot");
            itemBuilder.name(name, "supply caches", "Supplies sent by the gods.");
            // {108, 135, 1, 31, 25, 51, 86, 52, 59, 44, 147, 176, 180, 209, 199}
            itemBuilder.itemTypes(new short[]{
                    ItemTypes.ITEM_TYPE_NAMED,
                    ItemTypes.ITEM_TYPE_WOOD,
                    ItemTypes.ITEM_TYPE_NOTAKE,
                    ItemTypes.ITEM_TYPE_LOCKABLE,
                    ItemTypes.ITEM_TYPE_DECORATION,
                    ItemTypes.ITEM_TYPE_ONE_PER_TILE,
                    ItemTypes.ITEM_TYPE_OWNER_TURNABLE,
                    ItemTypes.ITEM_TYPE_REPAIRABLE,
                    ItemTypes.ITEM_TYPE_MISSION,
                    ItemTypes.ITEM_TYPE_PLANTABLE
            });
            itemBuilder.imageNumber((short) 462);
            itemBuilder.behaviourType((short) 1);
            itemBuilder.combatDamage(0);
            itemBuilder.decayTime(Long.MAX_VALUE);
            itemBuilder.dimensions(300, 300, 300);
            itemBuilder.primarySkill((int) NOID);
            itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemBuilder.modelName("model.structure.war.supplydepot.2.0.");
            itemBuilder.difficulty(5.0f);
            itemBuilder.weightGrams(50000);
            itemBuilder.material(Materials.MATERIAL_WOOD_BIRCH);
            itemBuilder.value(5000);

            ItemTemplate template = itemBuilder.build();
            arenaDepotID = template.getTemplateId();
            logger.info(name+" TemplateID: "+arenaDepotID);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            String name = "Hota pillar";
            ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.hodor.pillar");
            itemBuilder.name(name, "Hota pillar", "A pillar placed by the gods.");
            // {108, 135, 1, 31, 25, 51, 86, 52, 59, 44, 147, 176, 180, 209, 199}
            itemBuilder.itemTypes(new short[]{ 25, 49, 31, 52, 40, 67, 109, 123, 92, 48, 114, 32, 156, 60, 178 });
            itemBuilder.imageNumber((short) 60);
            itemBuilder.behaviourType((short) 1);
            itemBuilder.combatDamage(0);
            itemBuilder.decayTime(Long.MAX_VALUE);
            itemBuilder.dimensions(100, 100, 500);
            itemBuilder.primarySkill((int) NOID);
            itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemBuilder.modelName("model.structure.pillar.hota.");
            itemBuilder.difficulty(39.0f);
            itemBuilder.weightGrams(50000);
            itemBuilder.material(Materials.MATERIAL_STONE);
            itemBuilder.value(5000);

            ItemTemplate template = itemBuilder.build();
            hodorPillarID = template.getTemplateId();
            logger.info(name+" TemplateID: "+hodorPillarID);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Small loot carpet

        try {

            ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("Small loot carpet");
            itemTemplateBuilder.name("Small loot carpet", "Small loot carpet", "A place where loot is stored");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] {
                    ITEM_TYPE_WOOD,
                    ITEM_TYPE_NOTAKE,
                    ITEM_TYPE_DECORATION,
                    ITEM_TYPE_USE_GROUND_ONLY,
                    ITEM_TYPE_ONE_PER_TILE,
                    ITEM_TYPE_INDESTRUCTIBLE,
                    ITEM_TYPE_OUTSIDE_ONLY,
                    ITEM_TYPE_OWNER_MOVEABLE,
                    ITEM_TYPE_OWNER_TURNABLE,
                    ITEM_TYPE_NOMOVE,
                    ITEM_TYPE_HASDATA,
                    ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION
            });
            itemTemplateBuilder.imageNumber((short) 311);
            itemTemplateBuilder.behaviourType((short) 1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(9072000L);
            itemTemplateBuilder.dimensions(120, 120, 120);
            itemTemplateBuilder.primarySkill((int) NOID);
            itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.decoration.carpet.medi.three.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(100000);
            itemTemplateBuilder.material((byte) 14);
            ItemTemplate thisTemplate = itemTemplateBuilder.build();
            smallLootFlagID = thisTemplate.getTemplateId();
            logger.log(Level.INFO, "Using template id " + smallLootFlagID);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Loot carpet

        try {

            ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("Loot carpet");
            itemTemplateBuilder.name("Loot carpet", "Loot carpet", "A place where loot is stored");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] {
                    ITEM_TYPE_WOOD,
                    ITEM_TYPE_NOTAKE,
                    ITEM_TYPE_DECORATION,
                    ITEM_TYPE_USE_GROUND_ONLY,
                    ITEM_TYPE_ONE_PER_TILE,
                    ITEM_TYPE_INDESTRUCTIBLE,
                    ITEM_TYPE_OUTSIDE_ONLY,
                    ITEM_TYPE_OWNER_MOVEABLE,
                    ITEM_TYPE_OWNER_TURNABLE,
                    ITEM_TYPE_NOMOVE,
                    ITEM_TYPE_HASDATA,
                    ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION
            });
            itemTemplateBuilder.imageNumber((short) 311);
            itemTemplateBuilder.behaviourType((short) 1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(9072000L);
            itemTemplateBuilder.dimensions(120, 120, 120);
            itemTemplateBuilder.primarySkill((int) NOID);
            itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.decoration.colorful.carpet.large.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(100000);
            itemTemplateBuilder.material((byte) 14);
            ItemTemplate thisTemplate = itemTemplateBuilder.build();
            lootFlagID = thisTemplate.getTemplateId();
            logger.log(Level.INFO, "Using template id " + lootFlagID);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Trade delivery

        try {

            ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("Trade goods tent");
            itemTemplateBuilder.name("Trade goods tent", "Trade goods tent", "A place where trade goods are delivered");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] {
                    ITEM_TYPE_WOOD,
                    ITEM_TYPE_NOTAKE,
                    ITEM_TYPE_DECORATION,
                    ITEM_TYPE_USE_GROUND_ONLY,
                    ITEM_TYPE_ONE_PER_TILE,
                    ITEM_TYPE_INDESTRUCTIBLE,
                    ITEM_TYPE_OUTSIDE_ONLY,
                    ITEM_TYPE_OWNER_MOVEABLE,
                    ITEM_TYPE_OWNER_TURNABLE,
                    ITEM_TYPE_NOMOVE,
                    ITEM_TYPE_HASDATA,
                    ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION
            });
            itemTemplateBuilder.imageNumber((short) 311);
            itemTemplateBuilder.behaviourType((short) 1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(9072000L);
            itemTemplateBuilder.dimensions(120, 120, 120);
            itemTemplateBuilder.primarySkill((int) NOID);
            itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.structure.tent.pavilion.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(100000);
            itemTemplateBuilder.material((byte) 14);
            ItemTemplate thisTemplate = itemTemplateBuilder.build();
            tradeTentID = thisTemplate.getTemplateId();
            logger.log(Level.INFO, "Using template id " + tradeTentID);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Trade crates

        try {

            ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("Trade goods");
            itemTemplateBuilder.name("Trade goods", "Trade goods", "A crate of goods for trade");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] {
                    ITEM_TYPE_NAMED,
                    ITEM_TYPE_OWNER_DESTROYABLE,
                    ITEM_TYPE_WOOD,
                    ITEM_TYPE_NOTAKE,
                    ITEM_TYPE_TURNABLE,
                    ITEM_TYPE_DECORATION,
                    ITEM_TYPE_REPAIRABLE,
                    ITEM_TYPE_TRANSPORTABLE,
                    ITEM_TYPE_HASDATA,
                    ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION
            });
            itemTemplateBuilder.imageNumber((short) 311);
            itemTemplateBuilder.behaviourType((short) 1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(9072000L);
            itemTemplateBuilder.dimensions(120, 120, 120);
            itemTemplateBuilder.primarySkill((int) NOID);
            itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.container.crate.large.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(100000);
            itemTemplateBuilder.material((byte) 14);
            ItemTemplate thisTemplate = itemTemplateBuilder.build();
           tradeGoodsID = thisTemplate.getTemplateId();
            logger.log(Level.INFO, "Using template id " + tradeGoodsID);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Small loot Box

        try {

            ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("Small loot Box");
            itemTemplateBuilder.name("Small loot box", "Small loot boxes", "Some loot stashed by the denizens of the dungeon");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] {
                    ITEM_TYPE_NOTAKE,
                    ITEM_TYPE_NOPUT,
                    ITEM_TYPE_NAMED,
                    ITEM_TYPE_OWNER_DESTROYABLE,
                    ITEM_TYPE_WOOD,
                    ITEM_TYPE_TURNABLE,
                    ITEM_TYPE_DECORATION,
                    ITEM_TYPE_REPAIRABLE,
                    ITEM_TYPE_TRANSPORTABLE,
                    ITEM_TYPE_HASDATA,
                    ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION
            });
            itemTemplateBuilder.imageNumber((short) 244);
            itemTemplateBuilder.behaviourType((short) 1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(9072000L);
            itemTemplateBuilder.dimensions(120, 120, 120);
            itemTemplateBuilder.primarySkill((int) NOID);
            itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.container.chest.small.magical.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(1000);
            itemTemplateBuilder.material(Materials.MATERIAL_GOLD);
            ItemTemplate thisTemplate = itemTemplateBuilder.build();
            smallLootBoxID = thisTemplate.getTemplateId();
            logger.log(Level.INFO, "Using template id " + smallLootBoxID);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Loot Box

        try {

            ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("Loot Box");
            itemTemplateBuilder.name("Loot box", "Loot boxes", "Some loot stashed by the denizens of the dungeon");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] {
                    ITEM_TYPE_NOTAKE,
                    ITEM_TYPE_NOPUT,
                    ITEM_TYPE_NAMED,
                    ITEM_TYPE_OWNER_DESTROYABLE,
                    ITEM_TYPE_WOOD,
                    ITEM_TYPE_TURNABLE,
                    ITEM_TYPE_DECORATION,
                    ITEM_TYPE_REPAIRABLE,
                    ITEM_TYPE_TRANSPORTABLE,
                    ITEM_TYPE_HASDATA,
                    ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION
            });
            itemTemplateBuilder.imageNumber((short) 244);
            itemTemplateBuilder.behaviourType((short) 1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(9072000L);
            itemTemplateBuilder.dimensions(120, 120, 120);
            itemTemplateBuilder.primarySkill((int) NOID);
            itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.container.chest.large.magical.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(1000);
            itemTemplateBuilder.material(Materials.MATERIAL_GOLD);
            ItemTemplate thisTemplate = itemTemplateBuilder.build();
            lootBoxID = thisTemplate.getTemplateId();
            logger.log(Level.INFO, "Using template id " + lootBoxID);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Various production structures

        for (StructureTemplate template : structureTemplates) {

            try {
                ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("talor." + template.name);
                itemTemplateBuilder.name(template.name, template.name + "s", "A " + template.name);
                itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
                itemTemplateBuilder.itemTypes(new short[] {
                        ITEM_TYPE_WOOD,
                        ITEM_TYPE_NOTAKE,
                        ITEM_TYPE_DECORATION,
                        ITEM_TYPE_USE_GROUND_ONLY,
                        ITEM_TYPE_ONE_PER_TILE,
                        ITEM_TYPE_INDESTRUCTIBLE,
                        ITEM_TYPE_OUTSIDE_ONLY,
                        ITEM_TYPE_OWNER_MOVEABLE,
                        ITEM_TYPE_OWNER_TURNABLE,
                        ITEM_TYPE_NOMOVE,
                        ITEM_TYPE_HASDATA,
                        ITEM_TYPE_HOLLOW,
                        ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION

                });
                itemTemplateBuilder.imageNumber((short) 60);
                itemTemplateBuilder.behaviourType((short) 41);
                itemTemplateBuilder.combatDamage(0);
                itemTemplateBuilder.decayTime(9072000L);
                itemTemplateBuilder.dimensions(400, 400, 400);
                itemTemplateBuilder.primarySkill((int) NOID);
                itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
                itemTemplateBuilder.modelName(template.model);
                itemTemplateBuilder.difficulty(5.0f);
                itemTemplateBuilder.weightGrams(70000);
                itemTemplateBuilder.material((byte) 14);
                ItemTemplate thisTemplate = itemTemplateBuilder.build();
                template.templateID = thisTemplate.getTemplateId();
                logger.log(Level.INFO, "Using template id " + template.templateID);


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        for (SpawnerTemplate template : spawnerTemplates) {

            try {
                ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("talor." + template.name);
                itemTemplateBuilder.name(template.name, template.name + "s", "A " + template.name);
                itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
                itemTemplateBuilder.itemTypes(new short[]{
                        ITEM_TYPE_WOOD,
                        ITEM_TYPE_NOTAKE,
                        ITEM_TYPE_DECORATION,
                        ITEM_TYPE_USE_GROUND_ONLY,
                        ITEM_TYPE_ONE_PER_TILE,
                        ITEM_TYPE_INDESTRUCTIBLE,
                        ITEM_TYPE_OUTSIDE_ONLY,
                        ITEM_TYPE_OWNER_MOVEABLE,
                        ITEM_TYPE_OWNER_TURNABLE,
                        ITEM_TYPE_NOMOVE,
                        ITEM_TYPE_HASDATA,
                        ITEM_TYPE_HOLLOW,
                        ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION

                });
                itemTemplateBuilder.imageNumber((short) 60);
                itemTemplateBuilder.behaviourType((short) 41);
                itemTemplateBuilder.combatDamage(0);
                itemTemplateBuilder.decayTime(9072000L);
                itemTemplateBuilder.dimensions(400, 400, 400);
                itemTemplateBuilder.primarySkill((int) NOID);
                itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
                itemTemplateBuilder.modelName(template.model);
                itemTemplateBuilder.difficulty(5.0f);
                itemTemplateBuilder.weightGrams(70000);
                itemTemplateBuilder.material((byte) 14);
                ItemTemplate thisTemplate = itemTemplateBuilder.build();
                template.templateID = thisTemplate.getTemplateId();
                logger.log(Level.INFO, "Using template id " + template.templateID);


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Scrolls

        for (ScrollTemplate template : scrollTemplates) {

            try {
                ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("talor." + template.name);
                itemTemplateBuilder.name(template.name, template.name + "s", template.description);
                itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
                itemTemplateBuilder.itemTypes(new short[]{
                        ITEM_TYPE_FULLPRICE,
                        ITEM_TYPE_HASDATA,
                        ITEM_TYPE_NAMED,
                        ITEM_TYPE_DECORATION,
                        ITEM_TYPE_WOOD,
                        ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION

                });
                itemTemplateBuilder.imageNumber((short) 60);
                itemTemplateBuilder.behaviourType((short) 41);
                itemTemplateBuilder.combatDamage(0);
                itemTemplateBuilder.decayTime(9072000L);
                itemTemplateBuilder.dimensions(15, 15, 15);
                itemTemplateBuilder.primarySkill((int) NOID);
                itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
                itemTemplateBuilder.modelName(template.model);
                itemTemplateBuilder.difficulty(5.0f);
                itemTemplateBuilder.weightGrams(100);
                itemTemplateBuilder.material((byte) 14);
                ItemTemplate thisTemplate = itemTemplateBuilder.build();
                template.templateID = thisTemplate.getTemplateId();
                logger.log(Level.INFO, "Using template id " + template.templateID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Enchant Scrolls

        for (EnchantScrollTemplate template : enchantScrollTemplates) {

            try {
                ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("talor." + template.name);
                itemTemplateBuilder.name(template.name, template.name + "s", template.description);
                itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
                itemTemplateBuilder.itemTypes(new short[]{
                        ITEM_TYPE_FULLPRICE,
                        ITEM_TYPE_HASDATA,
                        ITEM_TYPE_NAMED,
                        ITEM_TYPE_DECORATION,
                        ITEM_TYPE_WOOD,
                        ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION

                });
                itemTemplateBuilder.imageNumber((short) 60);
                itemTemplateBuilder.behaviourType((short) 41);
                itemTemplateBuilder.combatDamage(0);
                itemTemplateBuilder.decayTime(9072000L);
                itemTemplateBuilder.dimensions(15, 15, 15);
                itemTemplateBuilder.primarySkill((int) NOID);
                itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
                itemTemplateBuilder.modelName(template.model);
                itemTemplateBuilder.difficulty(5.0f);
                itemTemplateBuilder.weightGrams(100);
                itemTemplateBuilder.material((byte) 14);
                ItemTemplate thisTemplate = itemTemplateBuilder.build();
                template.templateID = thisTemplate.getTemplateId();
                logger.log(Level.INFO, "Using template id " + template.templateID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        // Athanor Mechanism

        try {

            ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("Athanor Mechanism");
            itemTemplateBuilder.name("Athanor Mechanism", "Athanor Mechanism", "A strange mechanism that seems out of place here.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] {
                    ITEM_TYPE_WOOD,
                    ITEM_TYPE_NOTAKE,
                    ITEM_TYPE_DECORATION,
                    ITEM_TYPE_USE_GROUND_ONLY,
                    ITEM_TYPE_ONE_PER_TILE,
                    ITEM_TYPE_INDESTRUCTIBLE,
                    ITEM_TYPE_OUTSIDE_ONLY,
                    ITEM_TYPE_OWNER_MOVEABLE,
                    ITEM_TYPE_OWNER_TURNABLE,
                    ITEM_TYPE_NOMOVE,
                    ITEM_TYPE_HASDATA,
                    ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION
            });
            itemTemplateBuilder.imageNumber((short) 311);
            itemTemplateBuilder.behaviourType((short) 1);
            itemTemplateBuilder.combatDamage(0);
            itemTemplateBuilder.decayTime(9072000L);
            itemTemplateBuilder.dimensions(120, 120, 120);
            itemTemplateBuilder.primarySkill((int) NOID);
            itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.structure.obelisque.");
            itemTemplateBuilder.difficulty(5.0f);
            itemTemplateBuilder.weightGrams(100000);
            itemTemplateBuilder.material((byte) 14);
            ItemTemplate thisTemplate = itemTemplateBuilder.build();
            mechanismID = thisTemplate.getTemplateId();
            logger.log(Level.INFO, "Using template id " + mechanismID);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onServerStarted() {
        ModActions.registerAction(new LearnScroll(scrollTemplates));
        ModActions.registerAction(new UseEnchantScroll(enchantScrollTemplates));
        ModActions.registerAction(new OpenCrate(lootBoxID, smallLootBoxID, scrollTemplates));
        ModActions.registerAction(new PlaceGem(mechanismID, enchantScrollTemplates));
        ModActions.registerAction(new ArenaSupplyDepotAction());
        ModActions.registerAction(new HodorCapture(hodorPillarID));

        if (initialGoblinCensus == false) {

            for (Creature c : Creatures.getInstance().getCreatures()) {
                if(c.getTemplate().getTemplateId() == fogGoblinID && !fogGoblins.contains(c)) {
                    fogGoblins.add(c);
                }
            }

            logger.info("Performed census of fog goblins, there are : " + fogGoblins.size());
            initialGoblinCensus = true;

        }
    }

    @Override
    public void init() {

        ModCreatures.init();

        Bandit bandit = new Bandit();
        ModCreatures.addCreature(bandit);

        SnakeCultist snakecultist = new SnakeCultist();
        ModCreatures.addCreature(snakecultist);

        Viper viper = new Viper();
        ModCreatures.addCreature(viper);

        Necromancer necromancer = new Necromancer();
        ModCreatures.addCreature(necromancer);

        GoblinWarlord goblinwarlord = new GoblinWarlord();
        ModCreatures.addCreature(goblinwarlord);

        Ghoul ghoul = new Ghoul();
        ModCreatures.addCreature(ghoul);

        ModCreatures.addCreature(new GiantSerpent());
        ModCreatures.addCreature(new FogGoblin());
        ModCreatures.addCreature(new Cobra());
        ModCreatures.addCreature(new FireSpider());
        ModCreatures.addCreature(new TempleGuardian());
        ModCreatures.addCreature(new TemplePriest());
        ModCreatures.addCreature(new TemplePatriarch());
        ModCreatures.addCreature(new Fleshwalker());
        ModCreatures.addCreature(new Bonewalker());

        HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Creature", "getMountSpeedPercent", "(Z)F", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        Creature creature = (Creature) object;
                        float speed = (float) method.invoke(object, args);

                        logger.info("old speed: " + speed);

                        if (!creature.isRidden()) {

                            try
                            {
                                if (creature.getBonusForSpellEffect((byte)22) > 0.0F) {
                                    speed -= 0.2F * (creature.getBonusForSpellEffect((byte)22) / 100.0F);
                                    logger.info("decrease because oakshell: " + (0.2F * (creature.getBonusForSpellEffect((byte)22) / 100.0F)));
                                }

                                Item barding = creature.getArmour((byte) 2);

                                if (barding != null) {

                                    switch (barding.getMaterial()) {

                                        case Materials.MATERIAL_LEATHER:
                                            speed -= 0.1F;
                                            logger.info("decrease because leather: 0.1");
                                            break;
                                        default:
                                            speed -= 0.2F;
                                            logger.info("decrease because barding: 0.2");
                                    }

                                }

                            }
                            catch (Exception ex)
                            {
                            }


                        }

                        logger.info("new speed: " + speed);
                        return speed;

                    }
                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.spells.Dominate", "mayDominate", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/creatures/Creature;)Z", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        Creature performer = (Creature) args[0];
                        Creature target = (Creature) args[1];

                        if (target != null && target.isUnique()) {

                            performer.getCommunicator().sendNormalServerMessage("You cannot dominate " + target
                                    .getName() + ", because of its immense power.", (byte)3);
                            return false;

                        }

                        return method.invoke(object, args);

                    }
                };
            }
        });


        String descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[]{});
        HookManager.getInstance().registerHook("com.wurmonline.server.epic.Hota", "poll", descriptor, new InvocationHandlerFactory() {
            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Hodor.getInstance().hodorPoll();
                        return null;
                    }
                };
            }
        });


        HookManager.getInstance().registerHook("com.wurmonline.server.zones.FocusZone", "getHotaZone", "()Lcom/wurmonline/server/zones/FocusZone;", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                        try {
                            if (stackTrace[4].getClassName().equals("com.wurmonline.server.questions.VillageInfo") && stackTrace[4].getMethodName().equals("sendQuestion")) {
                                // If village info asks if there is a hota zone, just serve up any old zone to fool it
                                FocusZone fz = FocusZone.getAllZones()[0];
                                return fz;
                            }
                        } catch (Exception e) {

                        }

                        return method.invoke(object, args);

                    }
                };
            }
        });


        HookManager.getInstance().registerHook("com.wurmonline.server.weather.Weather", "getFog", "()F", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                        try {
                            if (stackTrace[4].getClassName().equals("com.wurmonline.server.zones.Zone") && stackTrace[4].getMethodName().equals("poll")) {
                                // If zone poller asks about fog, pretend there is none so fog spiders do not spawn
                                return 0.0F;
                            }
                        } catch (Exception e) {

                        }

                        return method.invoke(object, args);

                    }
                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.questions.VillageFoundationQuestion", "checkTile", "()Z", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        VillageFoundationQuestion vfq = (VillageFoundationQuestion) object;

                        VolaTile tile = Zones.getOrCreateTile(vfq.tokenx, vfq.tokeny, vfq.surfaced);
                        if (tile != null) {
                            int tx = tile.tilex;
                            int ty = tile.tiley;
                            int tt = Server.surfaceMesh.getTile(tx, ty);
                            if ((Tiles.decodeType(tt) == Tiles.Tile.TILE_LAVA.id) || (Tiles.isMineDoor(Tiles.decodeType(tt)))) {
                                vfq.getResponder().getCommunicator().sendSafeServerMessage("You cannot found a settlement here.");
                                return false;
                            }


                            for (int i=(tx-vfq.selectedWest);i<=(tx+vfq.selectedEast);i++) {
                                for (int j=(ty-vfq.selectedNorth);j<=(ty+vfq.selectedSouth);j++) {
                                    logger.info("checking tile: " + i + ", " + j);
                                    if (FocusZone.isNoBuildZoneAt(i, j)) {
                                        FocusZone fz = FocusZone.getZonesAt(i,j).iterator().next();
                                        vfq.getResponder().getCommunicator().sendSafeServerMessage("You cannot found a settlement here. It is too close to the " + fz.getName());
                                        return false;
                                    }

                                }
                            }


                            for (int x = -1; x <= 1; x++) {
                                for (int y = -1; y <= 1; y++) {
                                    int t = Server.surfaceMesh.getTile(tx + x, ty + y);
                                    if (Tiles.decodeHeight(t) < 0) {
                                        vfq.getResponder().getCommunicator().sendSafeServerMessage("You cannot found a settlement here. Too close to water.");
                                        return false;
                                    }
                                }
                            }
                        }
                        return true;
                    }
                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "mayCreatureInsertItem", "()Z", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        Item item = (Item) object;

                        for (StructureTemplate template : structureTemplates) {
                            if (item.getTemplateId() == template.templateID) {
                                return item.getItemCount() < 1000;
                            }
                        }

                        return method.invoke(object, args);
                    }
                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.CargoTransportationMethods", "unloadCargo", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;F)Z", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        Creature performer = (Creature) args[0];
                        Item item = (Item) args[1];

                        if (performer.getCurrentAction().currentSecond() == 3) {

                            for (FocusZone fz : FocusZone.getZonesAt(performer.currentTile.tilex, performer.currentTile.tiley)) {
                                if (fz.getName().equals("Northport") && item.getData() == 801L) {
                                    Items.destroyItem(item.getWurmId());
                                    performer.addMoney(2000L);
                                    performer.getCommunicator().sendNormalServerMessage("You receive a payment for delivering the trade goods.");

                                } else if (fz.getName().equals("Southport") && item.getData() == 802L) {
                                    Items.destroyItem(item.getWurmId());
                                    performer.addMoney(2000L);
                                    performer.getCommunicator().sendNormalServerMessage("You receive a payment for delivering the trade goods.");
                                }
                            }
                        }
                    return method.invoke(object, args);
                    }
                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Creature", "doNew", "(IZFFFILjava/lang/String;BBBZB)Lcom/wurmonline/server/creatures/Creature;", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        int id = (int) args[0];

                        // Spawn Fog Goblins instead of Fox Spiders
                        if (id == 105) {
                            args[0] = fogGoblinID;
                            id = fogGoblinID;
                        }

                        if (id == fogGoblinID) {
                            args[1] = true;
                        }

                        return method.invoke(object, args);

                    }

                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.questions.KarmaQuestion", "answer", "(Ljava/util/Properties;)V", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        Properties answers = (Properties) args[0];
                        String val = answers.getProperty("karma");
                        KarmaQuestion q = (KarmaQuestion) object;


                        if (val.equals("townportal")) {
                            q.getResponder().getCommunicator().sendNormalServerMessage("The spirits of courage prevent you from teleporting.");
                            return null;

                        } else {
                            return method.invoke(object, args);
                        }

                    }
                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Creature", "createBasicItems", "(Lcom/wurmonline/server/creatures/Creature;)V", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {
                        Creature creature = (Creature) args[0];
                        int id = creature.getTemplate().getTemplateId();
                        Item inventory = creature.getInventory();

                        // Fog goblins spawn with potions in inventory
                        if (id == fogGoblinID) {
                            Item potion = createItem(5, 100.0F);
                            inventory.insertItem(potion);

                            int reward = Server.rand.nextInt(100);

                            if (reward >= 20) {
                                Item secondPotion = createItem(5, 100.0F);
                                inventory.insertItem(secondPotion);
                            }

                            if (reward >= 60) {
                                Item thirdPotion = createItem(5, 100.0F);
                                inventory.insertItem(thirdPotion);
                            }

                            if (reward >= 90) {
                                Item yellowPotion = createItem(834, 100.0F);
                                inventory.insertItem(yellowPotion);
                            }

                            if (reward >= 97) {
                                int potionId = 871 + Server.rand.nextInt(18);
                                Item specialPotion = createItem(potionId, 100.0F);
                                inventory.insertItem(specialPotion);
                            }

                        }

                        return method.invoke(object, args);

                    }

                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.items.ItemTemplateCreator", "createItemTemplate", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[SSSIJIIII[BLjava/lang/String;FIBIZ)Lcom/wurmonline/server/items/ItemTemplate;", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        int id = (int) args[0];

                        // Replaces key to heavens with an egg
                        if (id == 794) {
                            args = new Object[] { 794, "huge egg", "huge eggs", "excellent", "good", "ok", "poor", "A huge yellow egg.", new short[] { 112, 5, 48, 96, 59 }, (short)522, (short)1, 0, 172800L, 3, 4, 5, -10, MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY, "model.food.egg.large.", 200.0F, 100, (byte)28, 10000, false};
                        }

                        return method.invoke(object, args);

                    }

                };
            }
        });


        HookManager.getInstance().registerHook("com.wurmonline.server.Players", "getChampionsFromKingdom", "(BI)I", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                       return 0;
                    }

                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.deities.Deities", "getEntityNumber", "(Ljava/lang/String;)I", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        String name = (String) args[0];

                        switch(name) {
                            case "Fo":
                                return 1;
                            case "Magranon":
                                return 2;
                            case "Libila":
                                return 4;
                            default:
                                return -1;
                        }
                    }

                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.epic.EpicMissionEnum", "getRandomMission", "(IZZZ)Lcom/wurmonline/server/epic/EpicMissionEnum;", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        int difficulty = (int) args[0];
                        boolean battlegroundServer = (boolean) args[1];
                        boolean homeServer = (boolean) args[2];
                        boolean enemyHomeServer = (boolean) args[3];

                        int totalChance = 0;
                        for (EpicMissionEnum f :  EpicMissionEnum.values()) {
                            if ((f.getMinDifficulty() <= difficulty) && (f.getMaxDifficulty() >= difficulty)) {
                                    totalChance += f.getMissionChance();
                            }
                        }
                        if (totalChance == 0) {
                            return null;
                        }
                        int winningVal = Server.rand.nextInt(totalChance);
                        int thisVal = 0;
                        EpicMissionEnum[] arrayOfEpicMissionEnum2 = EpicMissionEnum.values();
                        int f = arrayOfEpicMissionEnum2.length;

                        for (int localEpicMissionEnum1 = 0; localEpicMissionEnum1 < f; localEpicMissionEnum1++)
                        {
                            EpicMissionEnum f1 = arrayOfEpicMissionEnum2[localEpicMissionEnum1];
                            if ((f1.getMinDifficulty() <= difficulty) && (f1.getMaxDifficulty() >= difficulty)) {

                                if (thisVal + f1.getMissionChance() > winningVal && (

                                        f1.getMissionType() == (byte) 101 ||
                                                f1.getMissionType() == (byte) 102 ||
                                                f1.getMissionType() == (byte) 103 ||
                                                f1.getMissionType() == (byte) 104 ||
                                                f1.getMissionType() == (byte) 105 ||
                                                f1.getMissionType() == (byte) 106 ||
                                                f1.getMissionType() == (byte) 107 ||
                                                f1.getMissionType() == (byte) 112 ||
                                                f1.getMissionType() == (byte) 113 ||
                                                f1.getMissionType() == (byte) 114 ||
                                                f1.getMissionType() == (byte) 115 ||
                                                f1.getMissionType() == (byte) 116 ||
                                                f1.getMissionType() == (byte) 117 ||
                                                f1.getMissionType() == (byte) 118 ||
                                                f1.getMissionType() == (byte) 119 ||
                                                f1.getMissionType() == (byte) 120 ||
                                                f1.getMissionType() == (byte) 124

                                        )) {
                                    return f1;
                                }
                                thisVal += f1.getMissionChance();

                            }
                        }
                        return null;
                    }

                };
            }
        });


        HookManager.getInstance().registerHook("com.wurmonline.server.Players", "sendAltarsToPlayer", "(Lcom/wurmonline/server/players/Player;)V", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {

                        Player player = (Player) args[0];

                        SupplyDepots.sendDepotEffectsToPlayer(player);

                        return method.invoke(object, args);
                    }

                };
            }
        });

        HookManager.getInstance().registerHook("com.wurmonline.server.Server", "run", "()V", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object object, Method method, Object[] args) throws Throwable {
                        long now = System.currentTimeMillis();
                        if (now - lastPolledDepots > 300000) {
                            logger.log(Level.INFO, "last polled at " + lastPolledDepots + ", greater than : " + (now - lastPolledDepots));
                            lastPolledDepots = now;
                            SupplyDepots.pollDepotSpawn();
                        }

                        if (now - lastPolledTradeTents > delayTradeTents) {
                            logger.log(Level.INFO, "Polling trade tents at " + lastPolledTradeTents + ", time since last poll : " + (now - lastPolledTradeTents));
                            lastPolledTradeTents = now;
                            pollTradeTents();
                        }

                        if (now - lastPolledResourcePoints > delayResourcePoints) {
                            logger.log(Level.INFO, "Polling resource points at " + lastPolledResourcePoints + ", time since last poll : " + (now - lastPolledResourcePoints));
                            lastPolledResourcePoints = now;
                            pollResourcePoints();
                        }

                        if (now - lastPolledLootCarpets > delayLootCarpets) {
                            logger.log(Level.INFO, "Polling loot carpets at " + lastPolledLootCarpets + ", time since last poll : " + (now - lastPolledLootCarpets));
                            lastPolledLootCarpets = now;
                            pollLootCarpets();
                        }

                        if (now - lastPolledMobSpawners > delayMobSpawners) {
                            logger.log(Level.INFO, "Polling  mob spawners at " + lastPolledMobSpawners + ", time since last poll : " + (now - lastPolledMobSpawners));
                            lastPolledMobSpawners = now;
                            pollMobSpawners();
                        }

                        if (now - lastPolledAthanorMechanism > delayAthanorMechanism) {
                            logger.log(Level.INFO, "Polling athanor mechanism at " + lastPolledAthanorMechanism + ", time since last poll : " + (now - lastPolledAthanorMechanism));
                            lastPolledAthanorMechanism = now;
                            phaseShiftAthanorMechanism();
                        }

                        if (now - lastPolledFogGoblins > delayFogGoblins) {
                            logger.log(Level.INFO, "Polling fog goblins at " + lastPolledFogGoblins + ", time since last poll : " + (now - lastPolledFogGoblins) + ", fog is: " + Server.getWeather().getFog());
                            lastPolledFogGoblins = now;
                            pollFogGoblins();
                        }

                        return method.invoke(object, args);
                    }

                };
            }
        });

    }

    @Override
    public void preInit() {
        ModActions.init();
    }

    private void pollFogGoblins() {

        if (Server.getWeather().getFog() > 0.5F) {

            if (fogGoblins.size() < maxFogGoblins) {

                int spawnAttempts = 15;

                for (int i=0;i<spawnAttempts;i++) {

                    float worldSizeX = Zones.worldTileSizeX;
                    float worldSizeY = Zones.worldTileSizeY;
                    float minX = worldSizeX*0.25f;
                    float minY = worldSizeY*0.25f;
                    int tilex = (int) (minX+(minX*2*Server.rand.nextFloat()));
                    int tiley = (int) (minY+(minY*2*Server.rand.nextFloat()));

                    int tile = Server.surfaceMesh.getTile(tilex, tiley);

                    if(Tiles.decodeHeight(tile) > 0) {
                        Creature fg = spawnCreature(fogGoblinID, tilex * 4, tiley * 4, true, (byte)0, false);
                        fogGoblins.add(fg);
                    }
                }

                logger.info("Added some fog goblins, there are now: " + fogGoblins.size());

            }


        } else {

            if (fogGoblins.size() > 0) {

                Creature fg = fogGoblins.iterator().next();
                fogGoblins.remove(fg);
                fg.destroy();
                logger.info("Removed a fog goblin from the world");

            }

        }

    }

    private void pollMobSpawners() {

        // If no mob spawners found, search for them
        if (mobSpawners.size() == 0) {
            for(Item item : Items.getAllItems()){
                for (SpawnerTemplate template : spawnerTemplates) {
                    if (item.getTemplateId() == template.templateID) {
                        mobSpawners.add(item);
                        logger.info("Mob Spawner located and remembered, with name: " + item.getName() + ", and wurmid: " + item.getWurmId());
                    }
                }
            }
        }

        // Loop through known Mob Spawners and spawn dungeon mobs

        for (Item mobSpawner : mobSpawners) {
            for (SpawnerTemplate template : spawnerTemplates) {
                if (mobSpawner.getTemplateId() == template.templateID) {

                    int tileX = mobSpawner.getTileX();
                    int tileY = mobSpawner.getTileY();
                    int mobCount = 0;

                    for (int i=tileX-3; i<tileX+3;i++) {
                        for (int j=tileY-3; j<tileY+3;j++) {
                            try {
                                mobCount = mobCount + Zones.getOrCreateTile(i, j, mobSpawner.isOnSurface()).getCreatures().length;
                            } catch (Exception e) {

                            }
                        }
                    }

                    if (mobCount == 0) {
                        spawnCreature(template.mobType, mobSpawner, (byte) 1);
                    } else if (mobCount < template.maxNum) {
                        spawnCreature(template.mobType, mobSpawner, (byte) 0);
                    }

                }
            }
        }

    }


    private void pollLootCarpets() {

        // If no loot carpets found, search for them
        if (lootCarpets.size() == 0) {
            for(Item item : Items.getAllItems()){

                if (item.getTemplateId() == lootFlagID || item.getTemplateId() == smallLootFlagID) {
                    lootCarpets.add(item);
                    logger.info("Loot Carpet located and remembered, with name: " + item.getName() + ", and wurmid: " + item.getWurmId());
                }

            }
        }


        // Loop through known loot carpets and spawn loot boxes

        for (Item lootCarpet : lootCarpets) {

            int tileX = lootCarpet.getTileX();
            int tileY = lootCarpet.getTileY();
            int boxID = (lootCarpet.getTemplateId() == smallLootFlagID) ? smallLootBoxID : lootBoxID;

            int boxCount = 0;

            for (int i=tileX-1; i<tileX+1;i++) {
                for (int j=tileY-1; j<tileY+1;j++) {
                    VolaTile t = Zones.getTileOrNull(i, j, lootCarpet.isOnSurface());

                    if ((t != null)) {
                        for (Item possibleBox : t.getItems()) {
                            if (possibleBox.getTemplateId() == boxID) {
                                boxCount++;
                            }
                        }
                    }
                }
            }

            if (boxCount < 2) {
                spawnLootBox(lootCarpet, boxID);
            }
        }

    }

    private void pollResourcePoints() {

        // If no resource points found, search for them
        if (resourcePoints.size() == 0) {
            for(Item item : Items.getAllItems()){
                for (StructureTemplate template : structureTemplates) {
                    if (item.getTemplateId() == template.templateID) {
                        resourcePoints.add(item);
                        logger.info("Resource Point located and remembered, with name: " + item.getName() + ", and wurmid: " + item.getWurmId());
                    }
                }
            }
        }

        // Loop through known resource points and spawn items
        for(Item resourcePoint : resourcePoints) {

            for (StructureTemplate template : structureTemplates) {
                if (resourcePoint.getTemplateId() == template.templateID) {
                    spawnItemSpawn(resourcePoint, template.templateProduce, template.templateConsume, template.templateSecondaryConsume, 50.0F, 30.0F, template.maxNum, template.maxitems);
                    SoundPlayer.playSound(template.sound, resourcePoint, 0);
                }
            }
        }

    }

    private void pollTradeTents() {

        // If no Trade Tents found, search for them
        if (tradeTents.size() == 0) {
            for(Item item : Items.getAllItems()){
                if (item.getTemplateId() == tradeTentID) {
                    tradeTents.add(item);
                    logger.info("Trade Tents located and remembered, with name: " + item.getName() + ", and wurmid: " + item.getWurmId());
                }
            }
        }

        // Loop through known Trade Tents and spawn crates

        for(Item tradeTent : tradeTents) {

            int tileX = tradeTent.getTileX();
            int tileY = tradeTent.getTileY();
            int crateCount = 0;

            for (int i=tileX-5; i<tileX+5;i++) {
                for (int j=tileY-5; j<tileY+5;j++) {
                    VolaTile tile = Zones.getTileOrNull(i, j, tradeTent.isOnSurface());

                    if ((tile != null)) {
                        for (Item possibleCrate : tile.getItems()) {
                            if (possibleCrate.getTemplateId() == tradeGoodsID) {
                                crateCount++;
                            }
                        }
                    }
                }
            }

            if (crateCount < 20) {
                for (FocusZone fz : FocusZone.getZonesAt(tileX, tileY)) {
                    if (fz.getName().equals("Southport")) {
                        spawnTradeCrate(tradeTent, 801L);
                    } else if (fz.getName().equals("Northport")) {
                        spawnTradeCrate(tradeTent, 802L);
                    }
                }
            }
        }

    }

    private void phaseShiftAthanorMechanism() {

        if (athanorMechanism == null) {
            for(Item item : Items.getAllItems()){
                if(item.getTemplateId() == mechanismID){
                    logger.info("Athanor Mechanism located and remembered, with name: " + item.getName() + ", and wurmid: " + item.getWurmId());
                    athanorMechanism = item;
                    break;
                }
            }
        }
        Location targetLocation = mechanismLocations[Server.rand.nextInt(mechanismLocations.length)];
        try {
            Server.getInstance().broadCastAlert("The Athanor Mechanism has phase shifted to a new location!");
            athanorMechanism.setPosXY(targetLocation.x * 4.0F, targetLocation.y * 4.0F);
            Zone zone = Zones.getZone((int)targetLocation.x, (int)targetLocation.y, true);
            zone.addItem(athanorMechanism);
            Players.getInstance().weatherFlash(athanorMechanism.getTileX(), athanorMechanism.getTileY(), athanorMechanism.getPosZ());
            logger.info("Athanor Mechanism shifted to x: " + targetLocation.x + ", y: " + targetLocation.y);
        } catch (Exception e) {

        }

    }


    private void spawnItemSpawn(Item item, int templateProduce, int templateConsume, int templateSecondaryConsume, float startQl, float qlValRange, int maxNums, int maxItems)
    {
        Byte material = null;
        if(templateProduce == 274) {
            templateProduce = 274 + Server.rand.nextInt(6);
            material = Materials.MATERIAL_IRON;
        } else if (templateProduce == 280) {
            templateProduce = 280 + Server.rand.nextInt(8);
            material = Materials.MATERIAL_STEEL;
        } else if (templateProduce == 2) {
            int[] weaponIDs = {ItemList.shieldSmallMetal, ItemList.shieldMedium, ItemList.maulLarge, ItemList.maulSmall, ItemList.axeMedium, ItemList.axeSmall, ItemList.swordShort, ItemList.swordTwoHander, ItemList.swordLong, ItemList.halberd};
            templateProduce = weaponIDs[Server.rand.nextInt(weaponIDs.length)];
            material = Materials.MATERIAL_IRON;
        } else if (templateProduce == ItemList.log) {
            material = Materials.MATERIAL_WOOD_BIRCH;
        } else if (templateProduce == ItemList.stoneBrick) {
            material = Materials.MATERIAL_STONE;
        } else if (templateProduce == ItemList.ball) {
            material = Materials.MATERIAL_IRON;
        }

        Item[] currentItems = item.getAllItems(true);
        int produceTally = 0;
        int consumeTally = 0;
        int secondaryConsumeTally = 0;

        float[] consumeQLs = new float[maxNums];
        float[] secondaryConsumeQLs = new float[maxNums];

        for (Item i : currentItems) {
            if (templateProduce == i.getTemplateId() || templateProduce == 50)
            {
                produceTally++;
            } else if (templateConsume == i.getTemplateId()) {
                if(consumeTally < consumeQLs.length) {
                    consumeQLs[consumeTally] = i.getQualityLevel();
                }
                consumeTally++;
            } else if (templateSecondaryConsume == i.getTemplateId()) {
                if(secondaryConsumeTally < secondaryConsumeQLs.length) {
                    secondaryConsumeQLs[secondaryConsumeTally] = i.getQualityLevel();
                }
                secondaryConsumeTally++;
            }
        }

        if (templateConsume != 0) {
            maxNums = Math.min(maxNums, consumeTally);
        }

        if (templateSecondaryConsume != 0) {
            maxNums = Math.min(maxNums, secondaryConsumeTally);
        }

        if (produceTally + maxNums > maxItems) {
            return;
        }

        if (templateConsume != 0) {

            consumeTally = Math.min(maxNums, consumeTally);

            if (templateSecondaryConsume != 0) {
                secondaryConsumeTally = Math.min(maxNums, secondaryConsumeTally);
            }

            for (Item i : currentItems) {
                if (consumeTally > 0 && i.getTemplateId() == templateConsume) {
                    Items.destroyItem(i.getWurmId());
                    consumeTally--;
                }
            }

            for (Item i : currentItems) {

                if (secondaryConsumeTally > 0 && i.getTemplateId() == templateSecondaryConsume) {
                    Items.destroyItem(i.getWurmId());
                    secondaryConsumeTally--;
                }
            }
        }


        for (int nums = 0; nums < maxNums; nums++) {
            try
            {
                byte rrarity = 0;
                float newql = startQl + Server.rand.nextFloat() * qlValRange;
                if (templateConsume != 0) {
                    newql = Math.min(newql, consumeQLs[nums]);
                }

                if (templateSecondaryConsume != 0) {
                    newql = Math.min(newql, secondaryConsumeQLs[nums]);
                }

                Item toInsert;

                if (material == null) {
                    toInsert = ItemFactory.createItem(templateProduce, newql, rrarity, "");
                } else {
                    toInsert = ItemFactory.createItem(templateProduce, newql, material, rrarity, "");
                }

                item.insertItem(toInsert, true);

            } catch (FailedException |NoSuchTemplateException e) {

            }

        }
    }

    private final Creature spawnCreature(int templateId, Item item, byte ctype)
    {
        return spawnCreature(templateId, (int) item.getPosX(), (int)item.getPosY(),  item.isOnSurface(), ctype, false);
    }

    private final Creature spawnCreature(int templateId, int x, int y, boolean isOnSurface, byte ctype, boolean isZombie) {

        try
        {

            float xMod = (3.0F * Server.rand.nextFloat()) - 1;
            float yMod = (3.0F * Server.rand.nextFloat()) - 1;

            CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            byte sex = 0;
            if (Server.rand.nextInt(2) == 0) {
                sex = 1;
            }

            Creature c = Creature.doNew(templateId, true, x + xMod , y + yMod, Server.rand
                    .nextFloat() * 360.0F, isOnSurface ? 0 : -1, ct.getName(), sex, (byte) 0, ctype, isZombie);
            return c;
        }
        catch (NoSuchCreatureTemplateException nst)
        {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
        return null;

    }

    private void spawnTradeCrate(Item item, long id) {
        try {
            Item crate = ItemFactory.createItem(tradeGoodsID, 50.0F + Server.rand.nextFloat() * 50.0F,
                item.getPosX() - 5.0F + (Server.rand.nextFloat() * 10.0F), item.getPosY() - 5.0F + (Server.rand.nextFloat() * 10.0F), 65.0F,
                item.isOnSurface(), (byte) 0, -10L, "");
            crate.setData(id);
        } catch (Exception ex) {
        }
    }

    private void spawnLootBox(Item item, int id) {
        try {
            Item box = ItemFactory.createItem(id, 50.0F + Server.rand.nextFloat() * 50.0F,
                    item.getPosX() - 3.0F + (Server.rand.nextFloat() * 3.0F), item.getPosY() - 1.0F + (Server.rand.nextFloat() * 1.0F), 65.0F,
                    item.isOnSurface(), (byte) 0, -10L, "");
            if (box.isOnSurface()) {
                Items.destroyItem(box.getWurmId());
            }
        } catch (Exception ex) {
        }
    }


    public static final Item createItem(int templateId, float qualityLevel)
            throws Exception
    {
        Item item = ItemFactory.createItem(templateId, qualityLevel, (byte)0, (byte)0, null);
        return item;
    }
}
