package commoble.exmachina.generators;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;


public class SolarPanelBlock extends Block
{
	public static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public SolarPanelBlock(Properties props)
	{
		super(props);
		this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(POWERED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state)
	{
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(POWERED, hasLight(context.getLevel(), context.getClickedPos()));
	}
	
	/**
	 * Returns the integer daylight level of the position, same math as daylight detector
	 * @param world world
	 * @param pos pos
	 * @return A value in the range [0,15]
	 */
	public static int getLightLevel(Level world, BlockPos pos)
	{
		if (world.dimensionType().hasSkyLight())
		{
			int lightLevel = world.getBrightness(LightLayer.SKY, pos) - world.getSkyDarken();
			float sunAngle = world.getSunAngle(1.0F);
			if (lightLevel > 0)
			{
				float angleOffset = sunAngle < (float) Math.PI ? 0.0F : ((float) Math.PI * 2F);
				sunAngle += (angleOffset - sunAngle) * 0.2F;
				lightLevel = Math.round((float) lightLevel * Mth.cos(sunAngle));
			}

			lightLevel = Mth.clamp(lightLevel, 0, 15);
			return lightLevel;
		}
		else
		{
			return 0;
		}
	}
	
	public static boolean hasLight(Level level, BlockPos pos)
	{
		return getLightLevel(level, pos) > 0;
	}
	
	public static void tickSolarPanel(ServerLevel level, BlockPos pos)
	{
		BlockState state = level.getBlockState(pos);
		if (state.hasProperty(SolarPanelBlock.POWERED))
		{
			boolean oldPower = state.getValue(SolarPanelBlock.POWERED);
			boolean newPower = hasLight(level, pos);
			if (oldPower != newPower)
			{
				level.setBlockAndUpdate(pos, state.setValue(SolarPanelBlock.POWERED, SolarPanelBlock.hasLight(level, pos)));				
			}
		}
	}
}
