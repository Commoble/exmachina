package net.commoble.exmachina.internal.mechanical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.ApiStatus;

import net.commoble.exmachina.api.MechanicalConnection;
import net.commoble.exmachina.api.MechanicalGraphKey;
import net.commoble.exmachina.api.MechanicalNode;
import net.commoble.exmachina.api.MechanicalState;
import net.commoble.exmachina.api.MechanicalStateComponent;
import net.commoble.exmachina.api.Parity;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Represents a mechanical graph of connected nodes
 * @param groupedNodes The graph's nodes grouped by parity and gear ratio and level
 * @param nodesInGraph all the nodes in the graph
 * @param baseVelocity double value of the angular velocity of the origin node of the graph (in signed radians per second, positive value indicates thumb points to positive end of the axis of rotation)
 */
@ApiStatus.Internal
public record MechanicalGraph(Map<GearGroup,Map<LevelAccessor,List<KeyedNode>>> groupedNodes, Map<MechanicalGraphKey,KeyedNode> nodesInGraph, double baseVelocity)
{
	/**
	 * Constructs a MechanicalGraph from a given origin
	 * @param level LevelAccessor of the origin node
	 * @param originKey MechanicalGraphKey describing the origin node's position
	 * @param originNode MechanicalNode describing the properties of the origin node
	 * @param levelLookup Function to determine the level of a node based on the node key's level key
	 * @param componentLookup Function to perform cache lookups for mechanical components by position
	 * @param gearCache Map of cached gear values
	 * @return MechanicalGraph constructed from the given origin
	 */
	@ApiStatus.Internal
	public static MechanicalGraph fromOriginNode(LevelAccessor level, MechanicalGraphKey originKey, MechanicalNode originNode, Function<ResourceKey<Level>,LevelAccessor> levelLookup, Function<BlockGetter, Function<BlockPos, MechanicalBlockState>> componentLookup, Map<Fraction,GearRatio> gearCache)
	{
		Map<GearGroup,Map<LevelAccessor,List<KeyedNode>>> groupedNodes = new HashMap<>();
		Map<MechanicalGraphKey, KeyedNode> nodesInGraph = new HashMap<>();
		Queue<KeyedNode> uncheckedNodesInGraph = new LinkedList<>();
		KeyedNode keyedOriginNode = KeyedNode.of(originKey, originNode, Parity.POSITIVE, Fraction.ONE, gearCache);
		nodesInGraph.put(originKey, keyedOriginNode);
		groupedNodes.computeIfAbsent(new GearGroup(Fraction.ONE, Parity.POSITIVE), group -> new IdentityHashMap<>())
			.computeIfAbsent(level, $ -> new ArrayList<>())
			.add(keyedOriginNode);
		uncheckedNodesInGraph.add(keyedOriginNode);
		
		int maxSize = ExMachina.COMMON_CONFIG.maxMechanicalGraphSize().getAsInt();
		boolean zeroed = false; // parity or gear ratio violation
		whileLoop:
		while (uncheckedNodesInGraph.poll() instanceof KeyedNode(
			MechanicalGraphKey nextKey,
			MechanicalNode nextNode,
			Parity trackedParity,
			GearRatio trackedGearRatio))
		{
			ResourceKey<Level> nextLevelKey = nextKey.levelKey();
			LevelAccessor nextLevel = levelLookup.apply(nextLevelKey);
			if (nextLevel == null)
				continue;
			
			for (var entry : nextNode.connectableNodes().entrySet())
			{
				MechanicalGraphKey preferredTargetKey = entry.getKey();
				ResourceKey<Level> targetLevelKey = preferredTargetKey.levelKey();
				LevelAccessor targetLevel = targetLevelKey == nextLevelKey
					? nextLevel
					: levelLookup.apply(targetLevelKey);
				if (targetLevel == null)
					continue;
				
				MechanicalConnection outgoingConnection = entry.getValue();
				Parity outgoingConnectionParity = outgoingConnection.parity();
				int outgoingConnectionTeeth = outgoingConnection.teeth();
				var localComponentLookup = componentLookup.apply(targetLevel);
				BlockPos targetPos = preferredTargetKey.pos();
				MechanicalBlockState targetStateComponent = localComponentLookup.apply(targetPos);
				MechanicalStateComponent targetComponent = targetStateComponent.component();
				for (MechanicalNode targetNode : targetComponent.getNodes(targetLevelKey, targetLevel, targetPos))
				{
					MechanicalGraphKey targetKey = new MechanicalGraphKey(targetLevelKey, targetPos, targetNode.shape());
					KeyedNode existingNode = nodesInGraph.get(targetKey);
					// the only reason we check existing node further is to check zeroing
					// if we're already zeroed, we don't care about it
					if (existingNode != null && zeroed)
						continue;
					
					if (targetKey.isValidFor(preferredTargetKey))
					{
						for (var nodePreferredByTargetEntry : targetNode.connectableNodes().entrySet())
						{
							MechanicalGraphKey keyPreferredByTarget = nodePreferredByTargetEntry.getKey();
							MechanicalConnection incomingConnection = nodePreferredByTargetEntry.getValue();
							Parity incomingConnectionParity = incomingConnection.parity();
							int incomingConnectionTeeth = incomingConnection.teeth();
							Parity connectionParity = outgoingConnectionParity == incomingConnectionParity ? outgoingConnectionParity : Parity.ZERO;
							Parity newTrackedParity = connectionParity.multiply(trackedParity);
							// how to calculate gear ratio
							// if either next or target node has nonpositive teeth, do not change tracked gear ratio
							// if both nodes have positive teeth, new Fraction is uhh
							// well, what do we want
							// if go from small gear to big gear with 2x teeth, we double the torque, halve the speed
							// it's conceptually easy to think of the gear ratio as "number of teeth relative to the origin node"
							GearRatio targetGearRatio = incomingConnectionTeeth < 1 || outgoingConnectionTeeth < 1
								? trackedGearRatio
								: gearCache.computeIfAbsent(Fraction.getFraction(incomingConnectionTeeth, outgoingConnectionTeeth).multiplyBy(trackedGearRatio.ratio), GearRatio::of);
							if (nextKey.isValidFor(keyPreferredByTarget))
							{
								// skip target if it's already in graph
								if (existingNode != null)
								{
									// but, if parity or gear ratio is violated, zero the graph too
									// how do we know if parity is violated?
									// nextNode parity * connection parity != existingNode parity
									// how do we know if gear ratio is violated?
									
									if (newTrackedParity != existingNode.trackedParity
										|| !existingNode.gearRatio.ratio.equals(targetGearRatio.ratio))
									{
										zeroed = true;
									}
									continue;
								}
								KeyedNode keyedTargetNode = new KeyedNode(targetKey, targetNode, newTrackedParity, targetGearRatio);
								nodesInGraph.put(targetKey, keyedTargetNode);
								groupedNodes.computeIfAbsent(new GearGroup(targetGearRatio.ratio, newTrackedParity), group -> new IdentityHashMap<>())
									.computeIfAbsent(targetLevel, $ -> new ArrayList<>())
									.add(keyedTargetNode);
								uncheckedNodesInGraph.add(keyedTargetNode);
								if (nodesInGraph.size() >= maxSize)
								{
									zeroed = true;
									break whileLoop;
								}
							}
						}
					}
				}
			}
		}
		
		double totalGearshiftedTorque = 0;
		double totalGearshiftedInertia = 0;
		if (!zeroed)
		{
			for (KeyedNode keyedNode : nodesInGraph.values())
			{
				// let g1 be origin gear ratio
				// get g2 be some other gear ratio
				// Ttotal as seen by g1 (Ttotal,g1) = sum(Tn/gn)
				// Ttotal,g2 = Ttotal,g1 * g2
				// Itotal,g1 = sum(In/gn^2)
				// Itotal,g2 = Itotal,g1 * g2^2
				MechanicalNode node = keyedNode.node;
				GearRatio gearRatio = keyedNode.gearRatio;
				double adjustedTorque = node.torque() * gearRatio.gearDivisor * keyedNode.trackedParity.value();
				totalGearshiftedTorque += adjustedTorque;
				double adjustedInertia = node.inertia() * gearRatio.squareDivisor;
				totalGearshiftedInertia += adjustedInertia;
			}
			// zero the graph if we have no inertia, can't divide by 0
			if (totalGearshiftedInertia <= 0D || totalGearshiftedTorque == 0D)
			{
				zeroed=true;
			}
			else
			{
				// apply countertorques
				// these only apply in specific directions and can't subtract the active torques beyond 0
				// this simulates e.g. rocks pushing back on a grinder
				for (KeyedNode keyedNode : nodesInGraph.values())
				{
					if (zeroed)
						break;
					MechanicalNode node = keyedNode.node;
					Parity relativeParity = keyedNode.trackedParity;
					// how does this work...
					// we know the parity relative to the origin
					// and we know the value of the active torque (also relative to the origin)
					// consider a grinder oriented such that applying a positive torque at positive parity rotates it "forward"
					// it has a positive countertorque of 10 and a negative countertorque of 0
					// if active torque and parity are both positive, subtract the positive countertorque
					// if relative parity is negative and active torque is positive, we are rotating it backward
					// subtract the negative countertorque
					// if relative parity is positive and active torque is negative, we are rotating it backward
					// add the negative countertorque
					// if active torque and parity are both negative, add the positive countertorque
					boolean positiveTorque = totalGearshiftedTorque > 0;
					boolean positiveParity = relativeParity == Parity.POSITIVE;
					double counterTorque = (positiveTorque == positiveParity ? node.positiveCounterTorque() : node.negativeCounterTorque()) * keyedNode.gearRatio.gearDivisor;
					totalGearshiftedTorque = positiveTorque
						? Math.max(0, totalGearshiftedTorque - counterTorque)
						: Math.min(0, totalGearshiftedTorque + counterTorque);
					if (totalGearshiftedTorque == 0)
					{
						zeroed = true;	
					}
				}
			}
		}
		if (zeroed)
		{
			totalGearshiftedTorque = 0D;
			totalGearshiftedInertia = 1D; // can't divide by 0
		}
		double baseVelocity = totalGearshiftedTorque / totalGearshiftedInertia;
		// the graph should have
		// * base velocity
		// * set of node keys in graph
		// * graph nodes grouped by gear x parity (because we can calculate the adjusted velocity once per such group,
		// ensuring all such nodes are granted exactly the same velocity
		
		return new MechanicalGraph(groupedNodes, nodesInGraph, baseVelocity);
		
		
	}
	
	/**
	 * Informs all mechanical update listeners of their new power and velocity values
	 * @param gearCache Map of cached gear values
	 */
	public void updateListeners(Map<Fraction,GearRatio> gearCache)
	{
		// finally, calculate speed+power at each node and invoke listeners
		for (var entry : this.groupedNodes.entrySet())
		{
			GearGroup group = entry.getKey();
			GearRatio gearRatio = gearCache.computeIfAbsent(group.gearFraction, GearRatio::of);
			double localVelocity = baseVelocity * gearRatio.gearDivisor * group.parity.value();
			Map<LevelAccessor,List<KeyedNode>> levelNodes = entry.getValue();
			for (var levelNodeEntry : levelNodes.entrySet())
			{
				LevelAccessor level = levelNodeEntry.getKey();
				for (KeyedNode keyedNode : levelNodeEntry.getValue())
				{
					MechanicalNode node = keyedNode.node;
					double localPower = localVelocity * localVelocity * node.inertia();
					node.graphListener().accept(level, new MechanicalState(localPower,localVelocity));
				}
			}
		}
	}
	
	private static record KeyedNode(
		MechanicalGraphKey key,
		MechanicalNode node,
		Parity trackedParity,
		GearRatio gearRatio)
	{
		public static KeyedNode of(MechanicalGraphKey key,
			MechanicalNode node,
			Parity trackedParity,
			Fraction gearFraction,
			Map<Fraction,GearRatio> gearCache)
		{
			return new KeyedNode(
				key,
				node,
				trackedParity,
				gearCache.computeIfAbsent(gearFraction, GearRatio::of));
		}
		
	}
	
	/**
	 * Cached values for a gear fraction
	 * @param ratio Fraction (simplified) of a gearshift representing relative teeth counts (e.g. 2/1)
	 * @param inverseRatio Fraction of the teeth counts, inverted (e.g. 1/2)
	 * @param gearMultiplier double value of the ratio
	 * @param squareMultiplier double value of ratio*ratio
	 * @param gearDivisor double value of inverseRatio
	 * @param squareDivisor double value of inverseRatio * inverseRatio
	 */
	public static record GearRatio(
		Fraction ratio,
		Fraction inverseRatio,
		double gearMultiplier,
		double squareMultiplier,
		double gearDivisor,
		double squareDivisor)
	{
		/**
		 * {@return GearRatio using the given fraction}
		 * @param ratio Fraction of teeth counts
		 */
		public static GearRatio of(Fraction ratio)
		{
			double gearMultiplier = ratio.doubleValue();
			Fraction inverseRatio = ratio.invert();
			double gearDivisor = inverseRatio.doubleValue();
			Fraction gearSquared = ratio.multiplyBy(ratio);
			double squareMultiplier = gearSquared.doubleValue();
			double squareDivisor = gearSquared.invert().doubleValue();
			return new GearRatio(ratio, inverseRatio, gearMultiplier, squareMultiplier, gearDivisor, squareDivisor);
		}
	}
	
	private static record GearGroup(Fraction gearFraction, Parity parity)
	{
	}
}
