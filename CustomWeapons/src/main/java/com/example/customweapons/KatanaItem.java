package com.example.customweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;

public class KatanaItem {
    private final CustomWeaponsPlugin plugin;
    private final NamespacedKey idKey;

    public KatanaItem(CustomWeaponsPlugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "weapon");
    }

    public ItemStack create() {
        FileConfiguration c = plugin.getConfig();
        MiniMessage mm = MiniMessage.miniMessage();
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(mm.deserialize("<!italic><gradient:#f5f5ff:#6f9bff><bold>Katana</bold></gradient>"));
        meta.lore(List.of(
                mm.deserialize("<!italic><gray>A blade folded a thousand times."),
                Component.empty(),
                mm.deserialize("<!italic><gold>Right-click: <white>Iaido Dash")));

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                new NamespacedKey(plugin, "katana_damage"), c.getDouble("katana.attack-damage", 6.0),
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(
                new NamespacedKey(plugin, "katana_speed"), c.getDouble("katana.attack-speed", -2.0),
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setFloats(List.of((float) c.getInt("katana.custom-model-data", 1)));
        cmd.setStrings(List.of("katana"));
        meta.setCustomModelDataComponent(cmd);

        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, "katana");
        item.setItemMeta(meta);
        return item;
    }

    public boolean isKatana(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return "katana".equals(item.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING));
    }
}
