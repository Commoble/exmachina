package com.github.commoble.exmachina.common.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * Abstract base template for TileEntities requiring the persistance and synchronization methods
 */
public abstract class PersistantSynchronousTileEntity extends TileEntity
{
	
	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		this.readDataFromNBTIntoTE(compound);
		super.readFromNBT(compound);
	}
	
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		this.writeDataFromTEIntoNBT(compound);
		return super.writeToNBT(compound);
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
		return writeToNBT(new NBTTagCompound());
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
		this.writeToNBT(nbt);
		return new SPacketUpdateTileEntity(getPos(), 1, nbt);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
	{
		this.readFromNBT(packet.getNbtCompound());
	}
}
