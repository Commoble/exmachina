package com.github.commoble.exmachina.data;

/** To be deserialized from json via GSON **/
public class RawCircuitElement
{
	public double constant_load;
	public double constant_source;
	public String connector;
	public RawCircuitProperty static_source;
	public RawCircuitProperty static_load;
	public RawCircuitProperty dynamic_source;
	public RawCircuitProperty dynamic_load;
}
