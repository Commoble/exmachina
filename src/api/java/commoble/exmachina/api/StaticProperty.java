package commoble.exmachina.api;

import java.util.function.ToDoubleFunction;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface StaticProperty extends ToDoubleFunction<BlockState>
{

}
