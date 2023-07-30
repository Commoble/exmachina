package commoble.exmachina;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public record CommonConfig(IntValue maxCircuitSize)
{	
	public static CommonConfig create(ForgeConfigSpec.Builder builder)
	{
		IntValue maxCircuitSize = builder
			.comment("Maximum size of circuits in blocks")
			.defineInRange("maxCircuitSize", 10000, 1, Integer.MAX_VALUE);
		
		return new CommonConfig(maxCircuitSize);
	}
}
