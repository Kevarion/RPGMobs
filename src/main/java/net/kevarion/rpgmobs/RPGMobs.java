package net.kevarion.rpgmobs;

import net.kevarion.rpgmobs.components.CustomMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.*;

import static me.clip.placeholderapi.util.Msg.color;

public final class RPGMobs extends JavaPlugin implements Listener {

    private World world;
    private BukkitTask task;
    private Map<Entity, CustomMob> entities = new HashMap<>();
    private Map<Entity, Integer> indicators = new HashMap<>();
    private DecimalFormat formatter = new DecimalFormat("#.##");

    @Override
    public void onEnable() {

        getLogger().info("Enabled");

        world = Bukkit.getWorld("world");
        getServer().getPluginManager().registerEvents(this, this);

        spawnMobs(9, 10, 5 * 20);
        new BukkitRunnable() {
            Set<Entity> stands = indicators.keySet();
            List<Entity> removal = new ArrayList<>();
            @Override
            public void run() {
                for (Entity stand : stands) {
                    int ticksLeft = indicators.get(stand);
                    if (ticksLeft == 0) {
                        stand.remove();
                        removal.add(stand);
                        continue;
                    }
                    ticksLeft--;
                    indicators.put(stand, ticksLeft);
                }
                stands.removeAll(removal);
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    public void spawnMobs(int size, int mobCap, int spawnTime) {
        CustomMob[] mobTypes = CustomMob.values();
        task = new BukkitRunnable() {
            Set<Entity> spawned = entities.keySet();
            List<Entity> removal = new ArrayList<>();
            @Override
            public void run() {
                for (Entity entity : spawned) {
                    if (!entity.isValid() || entity.isDead()) removal.add(entity);
                }
                spawned.removeAll(removal);

                // Spawning algorithm
                int diff = mobCap - entities.size();
                if (diff <= 0) return;
                int spawnAmount = (int) (Math.random() * (diff + 1)), count = 0;
                while (count <= spawnAmount) {
                    count++;
                    int ranX = getRandomWithNegative(size), ranZ = getRandomWithNegative(size);
                    Block block = world.getHighestBlockAt(ranX, ranZ);
                    double xOffset = getRandomOffset(), zOffset = getRandomOffset();
                    Location loc = block.getLocation().clone().add(xOffset, 1, zOffset);
                    if (!isSpawnable(loc)) continue;
                    double random = Math.random() * 101, previous = 0;
                    CustomMob typeToSpawn = mobTypes[0];
                    for (CustomMob type : mobTypes) {
                        previous += type.getSpawnChance();
                        if (random <= previous) {
                            typeToSpawn = type;
                            break;
                        }
                    }
                    entities.put(typeToSpawn.spawn(loc), typeToSpawn);
                }

            }
        }.runTaskTimer(this, 0L, spawnTime);
    }

    private boolean isSpawnable(Location loc) {
        Block feetBlock = loc.getBlock(), headBlock = loc.clone().add(0, 1,0).getBlock(), upperBlock = loc.clone().add(0, 2, 0).getBlock();
        return feetBlock.isPassable() && !feetBlock.isLiquid() && headBlock.isPassable() && !headBlock.isLiquid() && upperBlock.isPassable() && !upperBlock.isLiquid();
    }

    private double getRandomOffset() {
        double random = Math.random();
        if (Math.random() > 0.5) random *= -1;
        return random;
    }

    private int getRandomWithNegative(int size) {
        int random = (int) (Math.random() * (size + 1));
        if (Math.random() > 0.5) random *= -1;
        return random;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity rawEntity = event.getEntity();
        if (!entities.containsKey(rawEntity)) return;
        CustomMob mob = entities.get(rawEntity);
        LivingEntity entity = (LivingEntity) rawEntity;
        double damage = event.getFinalDamage(), health = entity.getHealth() + entity.getAbsorptionAmount();
        if (health > damage) {
            // If entity survived the hit
            health -= damage;
            entity.setCustomName(Utils.color(mob.getName() + " &r&c" + (int) health + "/" + (int) mob.getMaxHealth() + "♥"));
        }
        Location loc = entity.getLocation().clone().add(getRandomOffset(), 1, getRandomOffset());
        world.spawn(loc, ArmorStand.class, armorStand -> {
           armorStand.setMarker(true);
           armorStand.setVisible(false);
           armorStand.setGravity(false);
           armorStand.setSmall(true);
           armorStand.setCustomNameVisible(true);
           armorStand.setCustomName(color("&c" + formatter.format(damage)));
           indicators.put(armorStand, 30);
        });
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!entities.containsKey(event.getEntity())) return;
        event.setDroppedExp(0);
        event.getDrops().clear();
        entities.remove(event.getEntity()).tryDropLoot(event.getEntity().getLocation());
    }

}
