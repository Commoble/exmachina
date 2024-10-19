package commoble.exmachina.processors;

import java.util.function.Predicate;

import com.google.common.base.Predicates;

import commoble.exmachina.ExMachina;
import commoble.exmachina.SlotGroupMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ToasterMenu extends SlotGroupMenu
{
	public static ToasterMenu clientMenu(int id, Inventory playerInventory)
	{
		return new ToasterMenu(id, playerInventory, Predicates.alwaysTrue(), new ItemStackHandler(27));
	}

	public static MenuConstructor serverMenu(ToasterBlockEntity be)
	{
		return (id, playerInventory, player) -> new ToasterMenu(id, playerInventory, p -> Container.stillValidBlockEntity(be, p), be.inventory);
	}

	protected ToasterMenu(int id, Inventory playerInventory, Predicate<Player> validator, IItemHandler inventory)
	{
		super(ExMachina.TOASTER_MENU.get(), id, playerInventory, validator, 8, 84);
		SlotGroup inventoryGroup = this.addSlots(9, 3, 8, 18, inventory, SlotItemHandler::new);
	}

}
