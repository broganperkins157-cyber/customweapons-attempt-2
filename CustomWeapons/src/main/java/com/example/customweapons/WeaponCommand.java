package com.example.customweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class WeaponCommand implements TabExecutor {
    private static final List<String> WEAPONS = List.of("katana");
    private final CustomWeaponsPlugin plugin;

    public WeaponCommand(CustomWeaponsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("customweapons.give")) {
            sender.sendMessage(Component.text("You don't have permission.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Component.text("Usage: /" + label + " give <katana> [player]", NamedTextColor.YELLOW));
            return true;
        }
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[2], NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(Component.text("Specify a player from the console.", NamedTextColor.RED));
            return true;
        }

        ItemStack item;
        switch (args[1].toLowerCase()) {
            case "katana" -> item = plugin.getKatana().create();
            default -> {
                sender.sendMessage(Component.text("Unknown weapon: " + args[1], NamedTextColor.RED));
                return true;
            }
        }
        target.getInventory().addItem(item).values()
                .forEach(left -> target.getWorld().dropItemNaturally(target.getLocation(), left));
        sender.sendMessage(Component.text("Gave " + args[1] + " to " + target.getName() + ".", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) out.add("give");
        else if (args.length == 2) out.addAll(WEAPONS);
        else if (args.length == 3) Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        String last = args[args.length - 1].toLowerCase();
        out.removeIf(s -> !s.toLowerCase().startsWith(last));
        return out;
    }
}
