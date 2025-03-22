package net.commoble.exmachina.api;

import java.util.Map;

import com.mojang.serialization.Codec;

import net.commoble.exmachina.internal.ExMachina;
import net.commoble.exmachina.internal.util.CodecHelper;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data attachment intended to be used for blockentities which use the exmachina:blockstate mechanical component or similar components
 * which automatically save mechanical state into arbitrary blockentities instead of having the blockentity implement saving manually.
 */
public final class MechanicalNodeStates
{
	private MechanicalNodeStates(){}
	
	/** neoforge:attachment_type / exmachina:mechanical_node_states **/
	public static final ResourceKey<AttachmentType<?>> KEY = ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ExMachina.id("mechanical_node_states"));
	/** Holder for mechanical node states **/
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<Map<NodeShape,MechanicalState>>> HOLDER = DeferredHolder.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, KEY.location());
	/** Codec for the attachment map. Deserializes mutable maps. **/
	public static final Codec<Map<NodeShape,MechanicalState>> CODEC = CodecHelper.pairListMap(NodeShape.CODEC, MechanicalState.CODEC);
		
}
