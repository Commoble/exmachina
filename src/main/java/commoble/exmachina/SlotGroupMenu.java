package commoble.exmachina;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * Abstract base menu that manages quickMoveStack and slot positioning
 */
public abstract class SlotGroupMenu extends AbstractContainerMenu
{
	private static final int PLAYER_INVENTORY_SIZE = 36;
	
	private final Predicate<Player> validator;
	private final SlotGroup backpackSlotGroup;
	private final SlotGroup hotbarSlotGroup;
	
	private int nextSlot;
	private List<SlotGroup> internalSlotGroups = new ArrayList<>();
	
	protected SlotGroupMenu(MenuType<?> type, int id, Inventory playerInventory, Predicate<Player> validator, int backpackStartX, int backpackStartY)
	{
		super(type, id);
		this.validator = validator;
		this.backpackSlotGroup = new SlotGroup(9, 9, 3, backpackStartX, backpackStartY, (i,x,y) -> new Slot(playerInventory, i, x, y));
		this.hotbarSlotGroup = new SlotGroup(0, 9, 1, backpackStartX, backpackStartY + 58, (i,x,y) -> new Slot(playerInventory, i, x, y));
		
		this.backpackSlotGroup.addSlots(this::addSlot);
		this.hotbarSlotGroup.addSlots(this::addSlot);
		this.nextSlot = PLAYER_INVENTORY_SIZE;
		// can add additional slots in subclass constructors via addSlots
	}
	
	protected SlotGroup addSlots(int columns, int rows, int xStart, int yStart, IItemHandler itemHandler, ItemHandlerSlotFactory ihsf)
	{
		return addSlots(columns, rows, xStart, yStart, SlotFactory.of(itemHandler, ihsf));
	}
	
	protected SlotGroup addSlots(int columns, int rows, int xStart, int yStart, SlotFactory slotFactory)
	{
		if (rows <= 0 || columns <= 0)
			throw new IllegalArgumentException(String.format("Empty slot group: %d x %d", columns, rows));
		SlotGroup slotGroup = new SlotGroup(this.nextSlot, rows, columns, xStart, yStart, slotFactory);
		this.internalSlotGroups.add(slotGroup);
		this.nextSlot += slotGroup.size();
		return slotGroup;
	}
	
	@Override
	public ItemStack quickMoveStack(Player player, int slotClicked)
	{
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotClicked);
		if (slot != null && slot.hasItem())
		{
			// protips from self to self:
			// moveItemStackTo returns true if any slot contents were changed
			// returning ItemStack.EMPTY signals the quickmove code to stop iterating
			// returning a non-empty itemstack results in the quickmove code being called again
			ItemStack stackInSlot = slot.getItem();
			slotStackCopy = stackInSlot.copy();
			if (slotClicked < PLAYER_INVENTORY_SIZE) // client shift-clicking item from player inventory to internal inventory
			{
				List<SlotGroup> slotGroups = this.getSlotGroupsInOrder(stackInSlot);
				int slotGroupCount = slotGroups.size();
				if (slotGroupCount <= 0)
				{
					return ItemStack.EMPTY;
				}
				boolean changed = false;
				for (int slotGroupIndex=0; slotGroupIndex<slotGroupCount; slotGroupIndex++)
				{
					SlotGroup slotGroup = slotGroups.get(slotGroupIndex);
					int start = slotGroup.firstIndex();
					int end = start + slotGroup.size();
					boolean groupChanged = this.moveItemStackTo(stackInSlot, start, end, false);
					if (groupChanged)
					{
						changed = true;
					}
				}
				if (!changed)
				{
					return ItemStack.EMPTY;
				}
			}
			else // moving item from internal inventory to backpack/hotbar
			{
				if (!this.moveItemStackTo(stackInSlot, 0, PLAYER_INVENTORY_SIZE, true))
				{
					return ItemStack.EMPTY;
				}
			}
		}
		
		return slotStackCopy;
	}

	@Override
	public boolean stillValid(Player player)
	{
		return this.validator.test(player);
	}
	
	/**
	 * Get the list of slotgroups to attempt to insert an item into when item is quickmoved from player inventory to internal inventory.
	 * @param quickMovedStack ItemStack in player inventory shift-clicked by player to quickmove it
	 * @return List of SlotGroups to check in the order to check them in for the given itemstack
	 */
	protected List<SlotGroup> getSlotGroupsInOrder(ItemStack quickMovedStack)
	{
		return this.internalSlotGroups;
	}

	public record SlotGroup(int firstIndex, int columns, int rows, int xStart, int yStart, SlotFactory slotFactory)
	{
		public void addSlots(Consumer<Slot> slotConsumer)
		{
			for (int row=0; row<rows; row++)
			{
				for (int column=0; column<columns; column++)
				{
					int index = row*columns + column + firstIndex;
					int x = xStart + column*18;
					int y = yStart + row*18;
					slotConsumer.accept(slotFactory.create(index, x, y));
				}
			}
		}
		
		public int size()
		{
			return rows*columns;
		}
	}
	
	@FunctionalInterface
	public static interface SlotFactory
	{
		public static SlotFactory of(IItemHandler itemHandler, ItemHandlerSlotFactory ihsf)
		{
			return (i, x, y) -> ihsf.create(itemHandler, i, x, y);
		}
		
		public abstract Slot create(int index, int x, int y);
	}

	
	@FunctionalInterface
	public static interface ItemHandlerSlotFactory
	{
		public abstract Slot create(IItemHandler handler, int index, int x, int y);
	}
}
