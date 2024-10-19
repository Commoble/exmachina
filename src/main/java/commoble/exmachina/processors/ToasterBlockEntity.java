package commoble.exmachina.processors;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import commoble.exmachina.ExMachina;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ToasterBlockEntity extends BlockEntity
{
	public final IItemHandler inventory = new ItemStackHandler(27) {
		@Override
		protected void onContentsChanged(int slot)
		{
			super.onContentsChanged(slot);
			ToasterBlockEntity.this.setChanged();
		}
	};
	public final LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> inventory);

	public ToasterBlockEntity(BlockEntityType<? extends ToasterBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public ToasterBlockEntity(BlockPos pos, BlockState state)
	{
		this(ExMachina.TOASTER_BLOCKENTITY.get(), pos, state);
	}
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, this.inventoryHolder);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ToasterBlockEntity be)
	{
		
	}
	
	// from furnace
	public void dropContents(ServerLevel serverLevel)
	{
		
	}

	public void awardUsedRecipesAndPopExperience(ServerPlayer p_155004_)
	{
//		List<Recipe<?>> list = this.getRecipesToAwardAndPopExperience(p_155004_.serverLevel(), p_155004_.position());
//		p_155004_.awardRecipes(list);
//
//		for (Recipe<?> recipe : list)
//		{
//			if (recipe != null)
//			{
//				p_155004_.triggerRecipeCrafted(recipe, this.items);
//			}
//		}
//
//		this.recipesUsed.clear();
	}

	public List<Recipe<?>> getRecipesToAwardAndPopExperience(ServerLevel p_154996_, Vec3 p_154997_)
	{
		List<Recipe<?>> list = Lists.newArrayList();

//		for (Object2IntMap.Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet())
//		{
//			p_154996_.getRecipeManager().byKey(entry.getKey()).ifPresent((p_155023_) -> {
//				list.add(p_155023_);
//				createExperience(p_154996_, p_154997_, entry.getIntValue(), ((AbstractCookingRecipe) p_155023_).getExperience());
//			});
//		}

		return list;
	}
}
