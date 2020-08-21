package com.github.commoble.exmachina.data;

/** To be deserialized from json via GSON **/
public class RawCircuitElement
{
	public String block;
	public String connection;
	public double wire_resistance;
	public RawCircuitProperty production;
	public RawCircuitProperty load;
	public String consumption;
}
