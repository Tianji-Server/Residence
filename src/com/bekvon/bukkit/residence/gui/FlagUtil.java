package com.bekvon.bukkit.residence.gui;

import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;

import cmiLib.ConfigReader;
import cmiLib.ItemManager.CMIMaterial;

public class FlagUtil {

    private FlagData flagData = new FlagData();
    private Residence plugin;

    public FlagUtil(Residence plugin) {
	this.plugin = plugin;
    }


    public void load() {
	ConfigReader c = null;
	try {
	    c = new ConfigReader("flags.yml");
	} catch (Exception e1) {
	    e1.printStackTrace();
	}

	if (c != null) {
	    if (!c.isConfigurationSection("Global.FlagPermission"))
		return;

	    Set<String> allFlags = c.getConfigurationSection("Global.FlagPermission").getKeys(false);

	    for (String oneFlag : allFlags) {
		if (!c.getC().contains("Global.FlagGui." + oneFlag))
		    continue;

		int id = 35;
		int data = 0;

		String value = c.get("Global.FlagGui." + oneFlag, "35-0");

		try {
		    if (value.contains("-")) {
			id = Integer.parseInt(value.split("-")[0]);
			data = Integer.parseInt(value.split("-")[1]);
		    } else
			id = Integer.parseInt(value);
		} catch (Exception e) {
		}

		CMIMaterial Mat = CMIMaterial.get(id, data);
		if (Mat == null)
		    Mat = CMIMaterial.STONE;
		ItemStack item = Mat.newItemStack();
		flagData.addFlagButton(oneFlag.toLowerCase(), item);
	    }
	}

    }

    public FlagData getFlagData() {
	return flagData;
    }
}
