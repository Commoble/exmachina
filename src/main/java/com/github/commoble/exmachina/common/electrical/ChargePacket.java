package com.github.commoble.exmachina.common.electrical;

import java.util.HashSet;

import net.minecraft.util.math.BlockPos;

public class ChargePacket
{
	public double charge = 0D;
	public double voltage = 0D;
	public boolean is_potential = false; // if true, used for testing electrical connection only
	public HashSet<BlockPos> traversed;	// positions this charge has traversed
	
	public ChargePacket(double charge, double voltage, boolean is_potential, BlockPos initial_pos)
	{
		this.charge = charge;
		this.voltage = voltage;
		this.is_potential = is_potential;
		this.traversed = new HashSet<BlockPos>();
		this.traversed.add(initial_pos);
	}
	
	public ChargePacket(double charge, double voltage, boolean is_potential, BlockPos initial_pos, HashSet<BlockPos> existing_traversal)
	{
		this.charge = charge;
		this.voltage = voltage;
		this.is_potential = is_potential;
		this.traversed = existing_traversal;
		this.traversed.add(initial_pos);
	}
}
