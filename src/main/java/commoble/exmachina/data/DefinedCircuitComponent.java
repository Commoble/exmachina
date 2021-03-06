package commoble.exmachina.data;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import commoble.exmachina.api.CircuitComponent;
import commoble.exmachina.api.Connector;
import commoble.exmachina.api.ConnectorFactory;
import commoble.exmachina.api.DynamicProperty;
import commoble.exmachina.api.StaticProperty;
import commoble.exmachina.plugins.CircuitBehaviourRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class DefinedCircuitComponent implements CircuitComponent
{
	public static final Set<BlockPos> NO_CONNECTIONS = ImmutableSet.of();
	public static final Connector NO_CONNECTOR = (world,pos,state) -> NO_CONNECTIONS;
	public static final ConnectorFactory NO_CONNECTOR_FACTORY = block -> NO_CONNECTOR;
	public static final StaticProperty STATIC_NOOP = state -> 0D;
	
	public final @Nonnull Connector connector;
	public final @Nonnull StaticProperty staticSource;
	public final @Nonnull Optional<DynamicProperty> dynamicSource;
	public final @Nonnull StaticProperty staticLoad;
	public final @Nonnull Optional<DynamicProperty> dynamicLoad;
	
	public DefinedCircuitComponent(@Nonnull RawCircuitElement raw, @Nonnull Block block, @Nonnull CircuitBehaviourRegistry registry)
	{
		this.connector = getProperty(raw.connector, block).orElse(NO_CONNECTOR);
		this.staticSource = getProperty(raw.static_source, block).orElse(STATIC_NOOP);
		this.dynamicSource = getProperty(raw.dynamic_source, block);
		this.staticLoad = getProperty(raw.static_load, block).orElse(STATIC_NOOP);
		this.dynamicLoad = getProperty(raw.dynamic_load, block);
	}
	
	private static <T> Optional<T> getProperty(@Nullable Function<Block, T> factory, @Nonnull Block block)
	{
		return factory == null ? Optional.empty() : Optional.ofNullable(factory.apply(block));
	}
	
	@Override
	public double getLoad(IWorld world, BlockState state, BlockPos pos)
	{
		return this.staticLoad.applyAsDouble(state) + this.dynamicLoad.map(f -> f.getValue(world, pos, state)).orElse(0D);
	}
	
	@Override
	public double getSource(IWorld world, BlockState state, BlockPos pos)
	{
		return this.staticSource.applyAsDouble(state) + this.dynamicSource.map(f -> f.getValue(world, pos, state)).orElse(0D);
	}
	
	@Override
	@Nonnull
	public Connector getConnector()
	{
		return this.connector;
	}
}
