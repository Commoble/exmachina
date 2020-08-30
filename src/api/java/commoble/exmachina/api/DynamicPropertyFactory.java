package commoble.exmachina.api;

import java.util.function.Function;

import net.minecraft.block.Block;

@FunctionalInterface
public interface DynamicPropertyFactory extends Function<Block, DynamicProperty>
{

}
