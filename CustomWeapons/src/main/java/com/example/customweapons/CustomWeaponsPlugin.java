package com.example.customweapons;

import org.bukkit.plugin.java.JavaPlugin;

public class CustomWeaponsPlugin extends JavaPlugin {
    private KatanaItem katana;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        katana = new KatanaItem(this);
        getServer().getPluginManager().registerEvents(new KatanaListener(this), this);
        WeaponCommand cmd = new WeaponCommand(this);
        getCommand("weapon").setExecutor(cmd);
        getCommand("weapon").setTabCompleter(cmd);
    }

    public KatanaItem getKatana() {
        return katana;
    }
}
