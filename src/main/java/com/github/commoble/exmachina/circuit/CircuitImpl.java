package com.github.commoble.exmachina.circuit;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;

import org.apache.commons.lang3.tuple.Pair;

import com.github.commoble.exmachina.api.Circuit;
import com.github.commoble.exmachina.api.CircuitComponent;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class CircuitImpl implements Circuit
{	
	private final IWorld world;
	private final double staticLoad;
	private final double staticSource;
	private final List<DoubleSupplier> dynamicLoads;
	private final List<DoubleSupplier> dynamicSources;
	private final Map<BlockPos, ? extends Pair<BlockState, ? extends CircuitComponent>> components;

//	private boolean isValid = true;
	private boolean needsDynamicUpdate = true;
	private double current = 0D;
	
	public CircuitImpl(IWorld world, double staticLoad, double staticSource, Map<BlockPos, ? extends Pair<BlockState, ? extends CircuitComponent>> components, List<DoubleSupplier> dynamicLoads, List<DoubleSupplier> dynamicSources)
	{
		this.world = world;
		this.staticLoad = staticLoad;
		this.staticSource = staticSource;
		this.dynamicLoads = dynamicLoads;
		this.dynamicSources = dynamicSources;
		this.components = components;
	}
	
	
	@Override
	public double getPowerSuppliedTo(BlockPos pos)
	{
		Pair<BlockState, ? extends CircuitComponent> pair = this.components.get(pos);
		
		if (pair != null)
		{
			double current = this.getCurrent();
			
			BlockState state = pair.getLeft();
			CircuitComponent element = pair.getRight();
			
			double load = element.getLoad(this.world, state, pos);
			double source = element.getSource(this.world, state, pos);
			
			double loadPower = current*current*load; // power supplied to position
			double sourcePower = current*source; // power drawn from position
			
			return loadPower - sourcePower;
		}
		else // if circuit is invalid or position is not in circuit
		{
			return 0D;
		}
	}


//	@Override
//	public boolean isValid()
//	{
//		return this.isValid && this.world != null;
//	}
//
//
//	@Override
//	public Circuit invalidate()
//	{
//		this.isValid = false;
//		return this;
//	}

	@Override
	public double getCurrent()
	{
		if (this.needsDynamicUpdate)
		{
			double totalLoad = this.staticLoad;
			double totalSource = this.staticSource;
			for (DoubleSupplier load : this.dynamicLoads)
			{
				totalLoad += load.getAsDouble();
			}
			for (DoubleSupplier source : this.dynamicSources)
			{
				totalSource += source.getAsDouble();
			}
			
			this.current = totalSource / totalLoad;
			this.needsDynamicUpdate = false;
		}
		
		return this.current;
	}


	@Override
	public void markNeedingDynamicUpdate()
	{
		this.needsDynamicUpdate = true;
	}


	@Override
	public Map<BlockPos, ? extends Pair<BlockState, ? extends CircuitComponent>> getComponentCache()
	{
		return this.components;
	}
}
