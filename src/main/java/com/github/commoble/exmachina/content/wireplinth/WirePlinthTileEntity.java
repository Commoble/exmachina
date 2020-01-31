package com.github.commoble.exmachina.content.wireplinth;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.api.circuit.WorldCircuitManager;
import com.github.commoble.exmachina.content.registry.TileEntityRegistrar;
import com.github.commoble.exmachina.content.util.NBTListHelper;
import com.github.commoble.exmachina.content.util.WorldHelper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class WirePlinthTileEntity extends TileEntity
{
	public static final String CONNECTIONS = "connections";
	
	private Set<BlockPos> connectionSet = new HashSet<>();
	
	public static NBTListHelper<BlockPos> BLOCKPOS_LISTER = new NBTListHelper<>(
		CONNECTIONS,
		pos -> NBTUtil.writeBlockPos(pos),
		nbt -> NBTUtil.readBlockPos(nbt)
		);
	
	public WirePlinthTileEntity()
	{
		super(TileEntityRegistrar.wire_plinth);
	}
	
	public static Optional<WirePlinthTileEntity> getPlinth(IWorld world, BlockPos pos)
	{
		return WorldHelper.getTileEntityAt(WirePlinthTileEntity.class, world, pos);
	}
	
	// connects two WirePlinthTileEntities
	// returns whether the attempt to add a connection was successful
	public static boolean addConnection(IWorld world, BlockPos posA, BlockPos posB)
	{
		// if two plinth TEs exist at the given locations, connect them and return true
		// otherwise return false
		return getPlinth(world, posA)
			.flatMap(plinthA -> getPlinth(world, posB)
				.map(plinthB -> addConnection(world, plinthA, plinthB)))
			.orElse(false);
	}
	
	// returns true if attempt to add a connection was successful
	public static boolean addConnection(IWorld world, @Nonnull WirePlinthTileEntity plinthA, @Nonnull WirePlinthTileEntity plinthB)
	{
		plinthA.addConnection(plinthB.pos);
		plinthB.addConnection(plinthA.pos);
		return true;
	}
	
	public Set<BlockPos> getConnections()
	{
		Set<BlockPos> totalSet = new HashSet<>();
		totalSet.addAll(this.connectionSet);
		BlockState state = this.getBlockState();
		if (state.has(WirePlinthBlock.DIRECTION_OF_ATTACHMENT))
		{
			totalSet.add(this.pos.offset(state.get(WirePlinthBlock.DIRECTION_OF_ATTACHMENT)));
		}
		return ImmutableSet.copyOf(totalSet);
	}
	
	public boolean hasConnection(BlockPos otherPos)
	{
		return this.connectionSet.contains(otherPos);
	}
	
	@Override
	public void remove()
	{
		this.clearConnections();
		super.remove();
	}

	// returns true if plinth TEs exist at the given locations and both have a connection to the other
	public static boolean arePlinthsConnected(IWorld world, BlockPos posA, BlockPos posB)
	{
		return getPlinth(world, posA)
			.flatMap(plinthA -> getPlinth(world, posB)
				.map(plinthB -> plinthA.hasConnection(posB) && plinthB.hasConnection(posA)))
			.orElse(false);
	}
	
	public void clearConnections()
	{
		this.connectionSet.forEach(
			otherPos -> getPlinth(this.world, otherPos)
				.ifPresent(otherPlinth -> otherPlinth.removeConnection(this.pos)));
		this.connectionSet = new HashSet<>();
		this.onDataUpdated();
	}
	
	// removes any connection between two plinths to each other
	// if only one plinth exists for some reason, or only one plinth has a connection to the other,
	// it will still attempt to remove its connection
	public static void removeConnection(IWorld world, BlockPos posA, BlockPos posB)
	{
		getPlinth(world, posA).ifPresent(plinth -> plinth.removeConnection(posB));
		getPlinth(world, posB).ifPresent(plinth -> plinth.removeConnection(posA));
	}
	
	private void addConnection(BlockPos otherPos)
	{
		this.connectionSet.add(otherPos);
		this.onDataUpdated();
	}
	
	private void removeConnection(BlockPos otherPos)
	{
		this.connectionSet.remove(otherPos);
		this.onDataUpdated();
	}
	
	public void onDataUpdated()
	{
		WorldCircuitManager.invalidateCircuitAt(this.world, this.pos);
		this.markDirty();
		this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		if (compound.contains(CONNECTIONS))
		{
			this.connectionSet = Sets.newHashSet(BLOCKPOS_LISTER.read(compound));
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		BLOCKPOS_LISTER.write(Lists.newArrayList(this.connectionSet), compound);
		return compound;
	}

	@Override
	// called on server when client loads chunk with TE in it
	public CompoundNBT getUpdateTag()
	{
		return this.write(new CompoundNBT());	// supermethods of write() and getUpdateTag() both call writeInternal
	}

	@Override
	// generate packet on server to send to client
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(this.pos, 1, this.write(new CompoundNBT()));
	}

	@Override
	// read packet on client
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		super.onDataPacket(net, pkt);
		this.read(pkt.getNbtCompound());
	}
}
