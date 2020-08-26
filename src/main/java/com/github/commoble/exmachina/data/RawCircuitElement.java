package com.github.commoble.exmachina.data;

import com.github.commoble.exmachina.api.DynamicPropertyFactory;
import com.github.commoble.exmachina.api.StaticPropertyFactory;

/** To be deserialized from json via GSON **/
public class RawCircuitElement
{
	public String connector = null;
	public StaticPropertyFactory static_source = null;
	public StaticPropertyFactory static_load = null;
	public DynamicPropertyFactory dynamic_source = null;
	public DynamicPropertyFactory dynamic_load = null;
}
