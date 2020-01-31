package com.github.commoble.exmachina.api.circuit;

import java.util.Set;

import com.github.commoble.exmachina.api.util.BlockContext;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class WireContext implements IConnectionProvider
{
	public final BlockContext context;
	public final WireProperties properties;

	private Set<BlockPos> allowedConnections = null;

	public WireContext(final BlockContext context, final WireProperties properties)
	{
		this.context = context;
		this.properties = properties;
	}

	@Override
	public boolean canThisConnectTo(IWorld world, BlockContext context)
	{
		return this.getPotentialConnections(world).contains(context.pos);
	}

	@Override
	public Set<BlockPos> getPotentialConnections(IWorld world)
	{
		if (this.allowedConnections == null)
		{
			this.allowedConnections = this.properties.getAllowedConnections(world, this.context);
		}
		return this.allowedConnections;
	}
}
