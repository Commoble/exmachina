package net.commoble.exmachina.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * A Face represents something attached to the inside of a block.
 * e.g. for the face {pos, DOWN}, this represents something which is inside that pos and at the bottom of that pos:
<pre>
/---\
|   |
|   |
| x |
\---/
</pre>
 * @param pos BlockPos where the Face is
 * @param attachmentSide Internal side of that BlockPos relative to the center
 */
public record Face(BlockPos pos, Direction attachmentSide, ResourceKey<Level> levelKey)
{

}
