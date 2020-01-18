package com.github.commoble.exmachina.common.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Abstract base template for TileEntities requiring the persistance and synchronization methods
 */
public abstract class PersistantSynchronousTileEntity extends TileEntity
{
	
	public PersistantSynchronousTileEntity(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void read(NBTTagCompound compound)
	{
		this.readDataFromNBTIntoTE(compound);
		super.read(compound);
	}
	
	
	@Override
	public NBTTagCompound write(NBTTagCompound compound)
	{
		this.writeDataFromTEIntoNBT(compound);
		return super.write(compound);
	}

	protected abstract void readDataFromNBTIntoTE(NBTTagCompound compound);
	protected abstract NBTTagCompound writeDataFromTEIntoNBT(NBTTagCompound compound);
	
	/**
	 * Called whenever chunkdata is sent to client
	 * Override to send smaller packets if necessary
	 */
	@Override
	public NBTTagCompound getUpdateTag()
	{
		return write(new NBTTagCompound());
	}
	
	/**
	 * Prepare a packet to sync TE to client
	 * This method as-is sends the entire NBT data in the packet
	 * Consider overriding to whittle the packet down if TE data is large
	 */
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.write(nbt);
		return new SPacketUpdateTileEntity(getPos(), 1, nbt);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
	{
		this.read(packet.getNbtCompound());
	}
}
