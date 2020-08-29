package commoble.exmachina;

import commoble.exmachina.util.ConfigHelper;
import commoble.exmachina.util.ConfigHelper.ConfigValueListener;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{
	public final ConfigValueListener<Integer> max_circuit_size_in_blocks;
	
	public ServerConfig(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("General Settings");
		
		this.max_circuit_size_in_blocks = subscriber.subscribe(builder
			.comment("Maximum size of circuits in blocks")
			.translation("exmachina.config.max_circuit_size_in_blocks")
			.defineInRange("max_circuit_size_in_blocks", 2000, 1, Integer.MAX_VALUE));
		
		builder.pop();
	}
}
