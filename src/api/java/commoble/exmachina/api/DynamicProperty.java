package commoble.exmachina.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * Dynamic properties are assigned to blocks as part of their circuit element definition.
 * They allow the existence of blocks whose power characteristics change somewhat frequently
 * and in a manner unrelated to their blockstates (e.g. a block entity whose power output
 * depends on its inventory).
 * 
 * If the value of a dynamic property of a block in the world changes, it must notify the
 * circuit manager capability of a dynamic update.
 */
@FunctionalInterface
public interface DynamicProperty
{
	public double getValue(IWorld world, BlockPos pos, BlockState state);
}
