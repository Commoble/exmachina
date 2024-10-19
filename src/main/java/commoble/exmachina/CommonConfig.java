package commoble.exmachina;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public record CommonConfig(IntValue solarPanelUpdateInterval)
{
	public static CommonConfig create(ForgeConfigSpec.Builder builder)
	{
		return new CommonConfig(builder
			.comment("Interval between solar panel updates (in ticks)")
			.defineInRange("solar_panel_update_interval", 200, 1, Integer.MAX_VALUE));
	}
}
