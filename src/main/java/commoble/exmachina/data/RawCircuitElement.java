package commoble.exmachina.data;

import commoble.exmachina.api.DynamicPropertyFactory;
import commoble.exmachina.api.StaticPropertyFactory;

/** To be deserialized from json via GSON **/
public class RawCircuitElement
{
	public String connector = null;
	public StaticPropertyFactory static_source = null;
	public StaticPropertyFactory static_load = null;
	public DynamicPropertyFactory dynamic_source = null;
	public DynamicPropertyFactory dynamic_load = null;
}
