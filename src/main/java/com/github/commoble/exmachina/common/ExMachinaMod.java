package com.github.commoble.exmachina.common;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ExMachinaMod.MODID, version = ExMachinaMod.VERSION, name=ExMachinaMod.NAME)
public class ExMachinaMod
{
	@Instance("sandbox")	// the static instance of the mod class
	public static ExMachinaMod instance = new ExMachinaMod();
	
    public static final String MODID = "exmachina";
    public static final String VERSION = "1.0.0.0";
    public static final String NAME="Ex-Machina";
    
    @SidedProxy(clientSide="com.github.commoble.ex-machina.client.CombinedClientProxy",
    		serverSide = "com.github.commoble.ex-machina.server.DedicatedServerProxy")
    public static CommonProxy proxy;
    
    /**
     * Run before anything else; read the config, create blocks, items, etc, register w/ GameRegistry
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	proxy.preInit(event);
    }
    
    /**
     * Setup anything that doesn't go in pre- or post-init. Build data structures, register recipes,
     * send FMLInterModComms messages to other mods
     */
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.load(event);
    }
    
    /**
     * Handle interaction with other mods, complete setup base on this
     */
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	proxy.postInit(event);
    }
    
    /**
     * Generates a string with the dungeonfist prefix from a base string to get the full string ID
     * e.g. "models/banana" -> "dungeonfist:models/banana"
     */
    public static String appendPrefix(String unprefixedString)
    {
		return ExMachinaMod.MODID + ":" + unprefixedString;
    }
}