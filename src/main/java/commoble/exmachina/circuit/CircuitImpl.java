package commoble.exmachina.circuit;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;

import org.apache.commons.lang3.tuple.Pair;

import commoble.exmachina.api.Circuit;
import commoble.exmachina.api.StateComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class CircuitImpl implements Circuit
{	
	private final LevelAccessor level; // why do we need a writable level to read gametime??
	private final double staticLoad;
	private final double staticSource;
	private final List<DoubleSupplier> dynamicLoads;
	private final List<DoubleSupplier> dynamicSources;
	private final Map<BlockPos, Pair<BlockState, StateComponent>> components;

	private long lastDynamicUpdateTime = -1L;
	private boolean needsDynamicUpdate = true;
	private double current = 0D;
	
	public CircuitImpl(LevelAccessor level, double staticLoad, double staticSource, Map<BlockPos, Pair<BlockState, StateComponent>> components, List<DoubleSupplier> dynamicLoads, List<DoubleSupplier> dynamicSources)
	{
		this.level = level;
		this.staticLoad = staticLoad;
		this.staticSource = staticSource;
		this.dynamicLoads = dynamicLoads;
		this.dynamicSources = dynamicSources;
		this.components = components;
	}
	
	
	@Override
	public double getPowerSuppliedTo(BlockPos pos)
	{
		var pair = this.components.get(pos);
		
		if (pair != null)
		{
			double current = this.getCurrent();
			
			BlockState state = pair.getLeft();
			StateComponent element = pair.getRight();
			
			double load = element.staticLoad() + element.dynamicLoad().getValue(this.level, pos, state);
			double source = element.staticSource() + element.dynamicSource().getValue(this.level, pos, state);
			
			double loadPower = current*current*load; // power supplied to position
			double sourcePower = current*source; // power drawn from position
			
			return loadPower - sourcePower;
		}
		else // if circuit is invalid or position is not in circuit
		{
			return 0D;
		}
	}

	@Override
	public double getCurrent()
	{
		long time = this.level.getLevelData().getGameTime();
		if (this.needsDynamicUpdate && time > this.lastDynamicUpdateTime)
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
			this.lastDynamicUpdateTime = time;
		}
		
		return this.current;
	}

	@Override
	public void markNeedingDynamicUpdate()
	{
		this.needsDynamicUpdate = true;
	}


	@Override
	public Map<BlockPos, Pair<BlockState, StateComponent>> components()
	{
		return this.components;
	}


	@Override
	public boolean isPresent()
	{
		return true;
	}
}
