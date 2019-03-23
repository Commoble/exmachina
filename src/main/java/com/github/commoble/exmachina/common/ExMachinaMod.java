package com.github.commoble.exmachina.common;

import com.github.commoble.exmachina.client.CombinedClientProxy;
import com.github.commoble.exmachina.client.gui.GuiHandler;
//import com.github.commoble.exmachina.common.world.WorldGenManager;
import com.github.commoble.exmachina.server.DedicatedServerProxy;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(ExMachinaMod.MODID)
public class ExMachinaMod
{
	public static ExMachinaMod instance;
	
    public static final String MODID = "exmachina";
    public static final String VERSION = "1.0.0.0";
    public static final String NAME="Ex-Machina";
    
    /*@SidedProxy(clientSide="com.github.commoble.exmachina.client.CombinedClientProxy",
    		serverSide = "com.github.commoble.exmachina.server.DedicatedServerProxy")
    public static CommonProxy proxy;*/
    public static final IProxy PROXY = DistExecutor.runForDist( () -> () -> new CombinedClientProxy(), () -> () -> new DedicatedServerProxy() );

	//public static WorldGenManager worldGenManager = new WorldGenManager();

	public static int modEntityID = 0;
	
	public ExMachinaMod()
	{
		instance = this;	// no @Instance anymore
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> GuiHandler::getClientGuiElement);
	}
}