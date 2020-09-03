package commoble.exmachina.api;

import java.util.function.ToDoubleFunction;

import net.minecraft.block.BlockState;

/**
 * Static Properties are assigned to blocks as part of their circuit element definition.
 * They can only vary by blockstate, but are more efficient to calculate for large circuits.
 */
@FunctionalInterface
public interface StaticProperty extends ToDoubleFunction<BlockState>
{

}
