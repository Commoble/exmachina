package com.github.commoble.exmachina;

import com.github.commoble.exmachina.content.Config;
import com.github.commoble.exmachina.content.util.ConfigHelper;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ExMachinaMod.MODID)
public class ExMachinaMod
{
    public static final String MODID = "exmachina";

    public static Config config;
    
	public ExMachinaMod()
	{
		config = ConfigHelper.register(ModConfig.Type.SERVER, Config::new);
	}
}