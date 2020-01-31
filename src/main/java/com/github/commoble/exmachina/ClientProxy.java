package com.github.commoble.exmachina;

import com.github.commoble.exmachina.content.client.WirePlinthRenderer;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ClientProxy
{
	public static void renderPlinthWire(World world, Vec3d start, Vec3d end)
	{
		WirePlinthRenderer.renderFromTo(world, start, end);
	}
}
