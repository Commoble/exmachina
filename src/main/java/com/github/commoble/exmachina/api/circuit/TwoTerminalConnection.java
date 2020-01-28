package com.github.commoble.exmachina.api.circuit;

import java.util.Collections;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.Sets;

import net.minecraft.util.math.BlockPos;

@Immutable
public class TwoTerminalConnection
{
	public final BlockPos positiveEnd;
	public final BlockPos negativeEnd;
	public final Set<BlockPos> set;
	
	public TwoTerminalConnection(final BlockPos positiveEnd, final BlockPos negativeEnd)
	{
		this.positiveEnd = positiveEnd;
		this.negativeEnd = negativeEnd;
		this.set = Collections.unmodifiableSet(Sets.newHashSet(this.positiveEnd, this.negativeEnd));
	}
}
