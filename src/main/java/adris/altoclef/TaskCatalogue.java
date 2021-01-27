package adris.altoclef;

import adris.altoclef.tasks.*;
import adris.altoclef.tasks.resources.CollectCobblestoneTask;
import adris.altoclef.tasks.resources.CollectPlanksTask;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.SmeltTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TaskCatalogue {

    private static HashMap<String, Item[]> _nameToItemMatches = new HashMap<>();
    private static HashMap<String, TaskFactory> _nameToResourceTask = new HashMap<>();
    static {
        /// DEFINE RESOURCE TASKS HERE
        {
            String p = "planks";
            String s = "stick";
            String o = null;

            simple("planks", ItemTarget.PLANKS, CollectPlanksTask.class);
            shapedRecipe2x2("stick", Items.STICK, p, o, p, o);
            mine("log", MiningRequirement.HAND, ItemTarget.LOG, ItemTarget.LOG);
            mine("dirt", MiningRequirement.HAND, new Block[]{Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.GRASS_PATH}, Items.DIRT);
            shapedRecipe2x2("crafting_table", Items.CRAFTING_TABLE, p, p, p, p);
            shapedRecipe2x2("wooden_pressure_plate", ItemTarget.WOOD_PRESSURE_PLATE, o, o, p, p);
            shapedRecipe2x2("wooden_button", ItemTarget.WOOD_BUTTON, p, o, o, o);

            shapedRecipe3x3("sign", ItemTarget.WOOD_SIGN, p, p, p, p, p, p, o, s, o);

            tools("wooden", "planks", Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_HOE);
            simple("cobblestone", Items.COBBLESTONE, CollectCobblestoneTask.class);
            {
                String c = "cobblestone";
                shapedRecipe3x3("stone_pickaxe", Items.STONE_PICKAXE, c, c, c, o, s, o, o, s, o);
                shapedRecipe3x3("furnace", Items.FURNACE, c, c, c, c, o, c, c, c, c);
            }
            tools("stone", "cobblestone", Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_SWORD, Items.STONE_AXE, Items.STONE_HOE);

            mine("coal",  MiningRequirement.WOOD, Blocks.COAL_ORE, Items.COAL);
            mine("iron_ore", MiningRequirement.STONE, Blocks.IRON_ORE, Items.IRON_ORE);
            mine("gold_ore", MiningRequirement.IRON, Blocks.GOLD_ORE, Items.GOLD_ORE);
            smelt("iron_ingot", Items.IRON_INGOT, "iron_ore");
            smelt("gold_ingot", Items.GOLD_INGOT, "gold_ore");

            tools("iron", "iron_ingot", Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_HOE);
            tools("golden", "gold_ingot", Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_HOE);
            armor("iron", "iron_ingot", Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
            armor("golden", "gold_ingot", Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);

            mine("diamond", MiningRequirement.IRON, Blocks.DIAMOND_ORE, Items.DIAMOND);

            tools("diamond", "diamond", Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_HOE);
            armor("diamond", "diamond", Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);

            shapedRecipe2x2("torch", Items.TORCH, "coal", o, s, o);

            alias("wooden_pick", "wooden_pickaxe");
            alias("stone_pick", "stone_pickaxe");
            alias("iron_pick", "iron_pickaxe");
            alias("gold_pick", "gold_pickaxe");
            alias("diamond_pick", "diamond_pickaxe");

        }
    }

    private static void put(String name, Item[] matches, TaskFactory factory) {
        _nameToResourceTask.put(name, factory);
        _nameToItemMatches.put(name, matches);
    }
    /*private static void put(String name, Item match, TaskFactory factory) {
        put(name, new Item[]{match}, factory);
    }*/

    /*
    static ResourceTask getItemTask(Item item, int count) {
        return getItemTask(ItemTarget.trimItemName(item.getTranslationKey()), count);
    }
    */

    // This is here so that we can use strings for item targets (optionally) and stuff like that.
    public static Item[] getItemMatches(String name) {
        if (!_nameToItemMatches.containsKey(name)) {
            return null;
        }
        return _nameToItemMatches.get(name);
    }

    public static ItemTarget getItemTarget(String name, int count) {
        return new ItemTarget(name, count);
    }

    public static ResourceTask getItemTask(String name, int count) {

        if (!taskExists(name)) {
            Debug.logWarning("Task " + name + " does not exist. Error possibly.");
            return null;
        }

        TaskFactory creator = _nameToResourceTask.get(name);
        return creator.createResourceTask(name, count);
    }

    public static ResourceTask getItemTask(ItemTarget target) {
        return getItemTask(target.getCatalogueName(), target.targetCount);
    }

    public static boolean taskExists(String name) {
        return _nameToResourceTask.containsKey(name);
    }

    public static Collection<String> resourceNames() {
        return _nameToResourceTask.keySet();
    }

    private static <T> void simple(String name, Item[] matches, Class<T> type) {
        put(name, matches, new SimpleTaskFactory(type));
    }
    private static <T> void simple(String name, Item matches, Class<T> type) {
        simple(name, new Item[] {matches}, type);
    }
    private static void mine(String name, MiningRequirement requirement, Item[] toMine, Item ...targets) {
        Block[] toMineBlocks = new Block[toMine.length];
        for (int i = 0; i < toMine.length; ++i) toMineBlocks[i] = Block.getBlockFromItem(toMine[i]);
        mine(name, requirement, toMineBlocks, targets);
    }
    private static void mine(String name, MiningRequirement requirement, Block[] toMine, Item ...targets) {
        put(name, targets, new MineTaskFactory(MineAndCollectTask.class, targets, toMine, requirement));
    }
    private static void mine(String name, MiningRequirement requirement, Block toMine, Item target) {
        mine(name, requirement, new Block[]{toMine}, target);
    }

    private static void shapedRecipe2x2(String name, Item[] matches, String s0, String s1, String s2, String s3) {
        CraftingRecipe recipe = CraftingRecipe.newShapedRecipe(name, new ItemTarget[] {t(s0), t(s1), t(s2), t(s3)});
        put(name, matches, new CraftTaskFactory(CraftInInventoryTask.class, name, recipe));
    }
    private static void shapedRecipe3x3(String name, Item[] matches, String s0, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8) {
        CraftingRecipe recipe = CraftingRecipe.newShapedRecipe(name, new ItemTarget[] {t(s0), t(s1), t(s2), t(s3), t(s4), t(s5), t(s6), t(s7), t(s8)});
        put(name, matches, new CraftTaskFactory(CraftInTableTask.class, name, recipe));
    }
    private static void shapedRecipe2x2(String name, Item match, String s0, String s1, String s2, String s3) {
        shapedRecipe2x2(name, new Item[]{match}, s0, s1, s2, s3);
    }
    private static void shapedRecipe3x3(String name, Item match, String s0, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8) {
        shapedRecipe3x3(name, new Item[]{match}, s0, s1, s2, s3, s4, s5, s6, s7, s8);
    }

    private static void smelt(String name, Item[] matches, String materials) {
        put(name, matches, new SmeltTaskFactory(SmeltInFurnaceTask.class, name, materials));
    }
    private static void smelt(String name, Item match, String materials) {
        smelt(name, new Item[]{match}, materials);
    }

    private static void tools(String toolMaterialName, String material, Item pickaxeItem, Item shovelItem, Item swordItem, Item axeItem, Item hoeItem) {
        String s = "stick";
        String o = null;
        //noinspection UnnecessaryLocalVariable
        String m = material;
        shapedRecipe3x3(toolMaterialName + "_pickaxe", pickaxeItem, m, m, m, o, s, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_shovel", shovelItem, o, m, o, o, s, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_sword", swordItem, o, m, o, o, m, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_axe", axeItem, m, m, o, m, s, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_hoe", hoeItem, m, m, o, o, s, o, o, s, o);
    }

    private static void armor(String armorMaterialName, String material, Item helmetItem, Item chestplateItem, Item leggingsItem, Item bootsItem) {
        String o = null;
        //noinspection UnnecessaryLocalVariable
        String m = material;
        shapedRecipe3x3(armorMaterialName + "_helmet", helmetItem, m, m, m, m, o, m, o, o, o);
        shapedRecipe3x3(armorMaterialName + "_chestplate", chestplateItem, m, o, m, m, m, m, m, m, m);
        shapedRecipe3x3(armorMaterialName + "_leggings", leggingsItem, m, m, m, m, o, m, m, o, m);
        shapedRecipe3x3(armorMaterialName + "_boots", bootsItem, o, o, o, m, o, m, m, o, m);
    }

    private static void alias(String newName, String original) {
        _nameToResourceTask.put(newName, _nameToResourceTask.get(original));
        _nameToItemMatches.put(newName, _nameToItemMatches.get(original));

    }





    private static ItemTarget t(String cataloguedName) {
        return new ItemTarget(cataloguedName);
    }


    /// TASK FACTORIES (I think I'm using the term "factory" wrong here but screw OOP I'll call it whatever I want)

    // Basically the issue is that tasks usually accept ItemTargets, which are pairs of items and their COUNTS.
    // These factories let you create these tasks with a count passed in LATER

    static class SimpleTaskFactory extends TaskFactory {
        public SimpleTaskFactory(Class type) {
            super(type);
        }

        @Override
        protected ResourceTask createResourceTaskInternal(String name, int count) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            Constructor constructor = _class.getConstructor(int.class);
            return (ResourceTask) constructor.newInstance(count);
        }
    }

    static class SmeltTaskFactory extends TaskFactory {

        private String _targetName;
        private String _materials;

        public SmeltTaskFactory(Class type, String targetName, String materials) {
            super(type);
            _targetName = targetName;
            _materials = materials;
        }

        @Override
        protected ResourceTask createResourceTaskInternal(String name, int count) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            Constructor constructor = _class.getConstructor(SmeltTarget.class);
            return (ResourceTask) constructor.newInstance(new SmeltTarget(new ItemTarget(_targetName, count), new ItemTarget(_materials, count)));
        }
    }

    static class CraftTaskFactory extends TaskFactory {
        // Generic Resource Task
        private String _targetName;

        // Craft task
        private CraftingRecipe _recipe;

        public CraftTaskFactory(Class type, String targetName, CraftingRecipe recipe) {
            super(type);
            _targetName = targetName;
            _recipe = recipe;

        }

        @Override
        protected ResourceTask createResourceTaskInternal(String name, int count) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            Constructor constructor = _class.getConstructor(ItemTarget.class, CraftingRecipe.class);
            return (ResourceTask) constructor.newInstance(new ItemTarget(_targetName, count), _recipe);
        }
    }

    static class MineTaskFactory extends TaskFactory {

        // Mine task
        private Block[] _toMine;
        private MiningRequirement _requirement;
        private Item[] _target;


        public MineTaskFactory(Class type, Item[] target, Block[] toMine, MiningRequirement requirement) {
            super(type);
            _target = target;
            _toMine = toMine;
            _requirement = requirement;
        }

        @Override
        protected ResourceTask createResourceTaskInternal(String name, int count) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            Constructor constructor = _class.getConstructor(ItemTarget.class, Block[].class, MiningRequirement.class);
            return (ResourceTask) constructor.newInstance(new ItemTarget(_target,  count), _toMine, _requirement);
        }
    }

    static abstract class TaskFactory {
        protected final Class _class;

        public TaskFactory(Class type) {
            _class = type;
        }
        protected abstract ResourceTask createResourceTaskInternal(String name, int count) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;

        public ResourceTask createResourceTask(String name, int count) {
            try {
                return createResourceTaskInternal(name, count);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                Debug.logWarning(e.getMessage() + "Couldn't find standard resource constructor for task for \"" + name + "\".");
                return null;
            }
        }
    }

}
