package com.github.commoble.exmachina.api.circuit;

import java.util.Set;

import net.minecraft.util.math.BlockPos;

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
	public boolean canThisConnectTo(BlockContext context)
	{
		return this.getPotentialConnections().contains(context.pos);
	}

	@Override
	public Set<BlockPos> getPotentialConnections()
	{
		if (this.allowedConnections == null)
		{
			this.allowedConnections = this.properties.getAllowedConnections(this.context);
		}
		return this.allowedConnections;
	}
}
