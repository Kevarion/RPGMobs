package net.kevarion.rpgmobs.components;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

import static net.kevarion.rpgmobs.Utils.*;


public enum CustomMob {

    DESERT_RISEN("&6Desert Risen", 15, 60, EntityType.HUSK, null, null, new LootItem(createItem(Material.ROTTEN_FLESH, 1, false, false, false, "&fPreserved Flesh", "&7A preserved flesh from a rotting corpse", "not sure what you'd want this for, though", "&7", "&9Foodstuff"), 1, 3, 100)),

    SKELETAL_MAGE("&dSkeletal Mage", 20, 15, EntityType.SKELETON, createItem(Material.BONE, 1, true, false, false, null), makeArmorSet(new ItemStack(Material.IRON_HELMET), null, new ItemStack(Material.IRON_LEGGINGS), null), new LootItem(createItem(Material.BONE, 1, true, false, false, "&dBone Wand", "&7A wand made from skeletal mage", "&7", "&9Skeletal Bones"), 30), new LootItem(new ItemStack(Material.BONE), 1, 3, 100))
    ;

    private String name;
    private double maxHealth, spawnChance;
    private EntityType type;
    private ItemStack mainItem;
    private ItemStack[] armor;
    private List<LootItem> lootTable;

    CustomMob(String name, double maxHealth, double spawnChance, EntityType type, ItemStack mainItem, ItemStack[] armor, LootItem... lootItems) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.spawnChance = spawnChance;
        this.type = type;
        this.mainItem = mainItem;
        this.armor = armor;
        lootTable = Arrays.asList(lootItems);
    }

    public LivingEntity spawn(Location location) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type);
        entity.setCustomNameVisible(true);
        entity.setCustomName(color(name + " &r&c" + (int) maxHealth + "/" + (int) maxHealth + "♥"));
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        entity.setHealth(maxHealth);
        EntityEquipment inv = entity.getEquipment();
        if (armor != null) inv.setArmorContents(armor);
        inv.setHelmetDropChance(0f);
        inv.setChestplateDropChance(0f);
        inv.setLeggingsDropChance(0f);
        inv.setBootsDropChance(0f);
        inv.setItemInMainHand(mainItem);
        inv.setItemInMainHandDropChance(0f);
        return entity;
    }

    public void tryDropLoot(Location location) {
        for (LootItem item : lootTable) {
            item.tryDropItem(location);
        }
    }

    public String getName() {
        return name;
    }

    public double getSpawnChance() {
        return spawnChance;
    }

    public double getMaxHealth() {
        return maxHealth;
    }



}
