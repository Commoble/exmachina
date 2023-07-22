package commoble.exmachina.api;

import java.util.function.Function;

import net.minecraft.block.Block;

@FunctionalInterface
public interface StaticPropertyFactory extends Function<Block, StaticProperty>
{
	@Override
	public StaticProperty apply(Block block);
}
