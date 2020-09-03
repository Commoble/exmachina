package commoble.exmachina.content;


import commoble.exmachina.api.AutoPlugin;
import commoble.exmachina.api.Plugin;
import commoble.exmachina.api.PluginRegistrator;
import net.minecraft.util.ResourceLocation;

@AutoPlugin
public class BuiltinPlugin implements Plugin
{
	@Override
	public void register(PluginRegistrator registry)
	{
		registry.registerConnectionType(new ResourceLocation("exmachina:all_directions"), json -> block -> BuiltinFunctions::getAllDirectionsConnectionSet);
		registry.registerConnectionType(new ResourceLocation("exmachina:directions"), BuiltinFunctions::readRotatableDirections);
		
		registry.registerStaticCircuitElementProperty(new ResourceLocation("exmachina:constant"), BuiltinFunctions::getConstantPropertyReader);
		registry.registerStaticCircuitElementProperty(new ResourceLocation("exmachina:blockstate"), BuiltinFunctions::getStateTablePropertyReader);
	}

}
