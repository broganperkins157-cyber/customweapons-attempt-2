package com.example.customweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class KatanaListener implements Listener {
    private final CustomWeaponsPlugin plugin;
    private final Map<UUID, Long> cooldownUntil = new HashMap<>();

    public KatanaListener(CustomWeaponsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        if (!plugin.getKatana().isKatana(p.getInventory().getItemInMainHand())) return;
        if (a == Action.RIGHT_CLICK_BLOCK && !p.isSneaking()
                && e.getClickedBlock() != null && e.getClickedBlock().getType().isInteractable()) return;

        long now = System.currentTimeMillis();
        Long until = cooldownUntil.get(p.getUniqueId());
        if (until != null && until > now) {
            p.sendActionBar(Component.text(
                    String.format("Iaido recharging: %.1fs", (until - now) / 1000.0), NamedTextColor.RED));
            return;
        }
        if (dash(p)) {
            long cd = (long) (plugin.getConfig().getDouble("katana.cooldown-seconds", 8) * 1000);
            cooldownUntil.put(p.getUniqueId(), now + cd);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldownUntil.remove(e.getPlayer().getUniqueId());
    }

    private boolean dash(Player p) {
        Location start = p.getLocation();
        Vector dir = p.getEyeLocation().getDirection().setY(0);
        if (dir.lengthSquared() < 1.0E-4) return false;
        dir.normalize();

        double maxDist = plugin.getConfig().getDouble("katana.dash-distance", 8.0);
        double damage = plugin.getConfig().getDouble("katana.dash-damage", 8.0);
        World w = p.getWorld();

        double travel = Math.min(
                clip(w, p.getEyeLocation(), dir, maxDist),
                clip(w, start.clone().add(0, 0.3, 0), dir, maxDist));
        if (travel < 0.5) {
            p.sendActionBar(Component.text("No room to dash!", NamedTextColor.RED));
            return false;
        }

        Set<LivingEntity> hits = new HashSet<>();
        for (double t = 0; t <= travel; t += 0.5) {
            Location pt = start.clone().add(dir.clone().multiply(t)).add(0, 1, 0);
            w.spawnParticle(Particle.SWEEP_ATTACK, pt, 1, 0, 0, 0, 0);
            w.spawnParticle(Particle.CRIT, pt, 4, 0.2, 0.3, 0.2, 0.05);
            for (LivingEntity le : w.getNearbyLivingEntities(pt, 1.3)) {
                if (le == p || le instanceof ArmorStand) continue;
                if (le instanceof Player o && o.getGameMode() == GameMode.SPECTATOR) continue;
                hits.add(le);
            }
        }

        p.teleport(start.clone().add(dir.clone().multiply(travel)));
        w.playSound(start, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.3f);
        for (LivingEntity le : hits) {
            le.damage(damage, p);
            le.setVelocity(le.getVelocity().add(dir.clone().multiply(0.4).setY(0.25)));
        }
        return true;
    }

    private double clip(World w, Location origin, Vector dir, double maxDist) {
        RayTraceResult r = w.rayTraceBlocks(origin, dir, maxDist + 0.6, FluidCollisionMode.NEVER, true);
        if (r == null) return maxDist;
        double d = r.getHitPosition().distance(origin.toVector()) - 0.6;
        return Math.max(0, Math.min(maxDist, d));
    }
}
