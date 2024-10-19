package commoble.exmachina;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public record ServerConfig(DoubleValue maxWirePostConnectionRange, DoubleValue maxBatteryBoxEnergy)
{
	public static ServerConfig create(ForgeConfigSpec.Builder builder)
	{
		return new ServerConfig(
			builder
				.comment("Maximum Wire Post Connection Range")
				.defineInRange("max_wire_post_connection_range", 32D, 0D, Double.MAX_VALUE),
			builder
				.comment("Maximum energy storable by battery boxes (in Joules or Watt-hours")
				.defineInRange("max_battery_box_energy",  1000D, 1, Double.MAX_VALUE));
	}
}
