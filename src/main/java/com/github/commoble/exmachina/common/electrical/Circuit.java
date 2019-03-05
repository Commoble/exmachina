package com.github.commoble.exmachina.common.electrical;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.exmachina.common.block.CategoriesOfBlocks;
import com.github.commoble.exmachina.common.block.IElectricalBlock;
import com.github.commoble.exmachina.common.block.ITwoTerminalComponent;
import com.github.commoble.exmachina.common.tileentity.ICircuitElementHolderTE;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import repackage.org.apache.commons.math3.linear.Array2DRowRealMatrix;
import repackage.org.apache.commons.math3.linear.ArrayRealVector;
import repackage.org.apache.commons.math3.linear.DecompositionSolver;
import repackage.org.apache.commons.math3.linear.LUDecomposition;
import repackage.org.apache.commons.math3.linear.RealMatrix;
import repackage.org.apache.commons.math3.linear.RealVector;
import repackage.org.apache.commons.math3.linear.SingularMatrixException;

public class Circuit
{
	public HashSet<Node> nodes = new HashSet<Node>();
	public HashMap<BlockPos, CircuitElement> components = new HashMap<BlockPos, CircuitElement>();
	
	// use instead of null circuit
	public static final Circuit INVALID_CIRCUIT = new Circuit().invalidate(null);
	
	/** If this is true, the circuit needs an update and must be rebuilt // use isValid to get **/
	private boolean invalidated = false;
	
	public boolean isPositionInAnyKnownNode(BlockPos pos)
	{
		for (Node node : this.nodes)
		{
			if (node.contains(pos))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Mark this circuit as invalidated and possibly ask each former component within it to run an update check
	 * If forceUpdate is false, updates will only be checked if the circuit was not already invalidated
	 * before calling the method
	 * If forceUpdate is true, the update checks will be carried out in any case
	 * 
	 * When would I want the circuit to do update checks even if it was already invalidated?
		-In case the invalidated circuit becomes valid (blocks added or replaced)
		-When neighbor is updated (context is ambiguous)

		When would I not want the circuit to do update checks if it was already invalidated?
		-when an invalidated circuit will not become valid (blocks removed)
		-problem: removal of block is handled by onBlockReplaced
		// could check for air but that's probably not necessary
		 * don't worry about forcing updates for now, just do it every time
	 * @return
	 */
	@Nonnull
	public Circuit invalidate(World world)
	{
		
		this.invalidated = true;
		/*if (world != null)
		{
			for (CircuitElement element : this.components.values())
			{
				BlockPos pos = element.componentPos;
				IBlockState state = world.getBlockState(pos);
				if (state.hasTileEntity())
				{
					TileEntity te = world.getTileEntity(pos);
					if (te instanceof ICircuitElementHolderTE)
					{
						((ICircuitElementHolderTE)te).onPossibleCircuitUpdate();
					}
				}
			}
		}*/
		return this;
	}
	
	public boolean isValid()
	{
		return !this.invalidated;
	}
	
	public void addCircuitComponent(World world, BlockPos pos, Node nodeA, Node nodeB)
	{
		// consolidate node instances
		for (Node node : this.nodes)
		{
			if (nodeA.equals(node))
			{
				nodeA = node;
			}
			if (nodeB.equals(node))
			{
				nodeB = node;
			}
		}
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof ICircuitElementHolderTE)
		{
			((ICircuitElementHolderTE)te).setCircuit(this);
			CircuitElement element;
			if (block instanceof ITwoTerminalComponent)
			{
				BlockPos positiveEnd = pos.offset(((ITwoTerminalComponent)block).getPositiveFace(world, pos));
				if (nodeA.contains(positiveEnd))
				{
					element = ((ICircuitElementHolderTE)te).createCircuitElement(nodeA, nodeB);
				}
				else
				{
					element = ((ICircuitElementHolderTE)te).createCircuitElement(nodeB, nodeA);
				}
			}
			else
			{
				element = ((ICircuitElementHolderTE)te).createCircuitElement(nodeA, nodeB);
			}
			this.components.put(pos, element);
		}
		else
		{
			System.out.println("Something borked!");
		}
	}
	
	
	/**
	 * Given a starting ground node, build the rest of the circuit and return that circuit
	 */
	@Nonnull
	public static Circuit buildCircuitFromGround(World world, Node groundNode)
	{
		Circuit circuit = new Circuit();
		
		circuit = expandCircuitFromNode(world, circuit, groundNode);		
		
		circuit.invalidated = false;
		
		circuit.doAnalysis(world, groundNode);
		
		return circuit;
	}
	
	private void doAnalysis(World world, Node groundNode)
	{
		// voltage across each resistor can be found by the formula AX=Z
		// where A, X, and Z are matrices
		// and X contains the voltage at each node and the current through each source (i.e. the unknowns)
		// and A and Z comtain values based on the values of resistors and independant sources (i.e. known quantities)
		// the unknowns can therefore be found as: X = A^-1 * Z
		// more math at https://www.swarthmore.edu/NatSci/echeeve1/Ref/mna/MNA1.html
		
		// this is also where the "identifier" of each node is set
		groundNode.identifier = -1;
		
		// build the A matrix
		// dimensions of (n+m) x (n+m), where n is number of NON-GROUND nodes and m is number of independant sources
		
		// start by getting nodes, sources, and resistors at least sort of organized
		int nodeCount = this.nodes.size();
		int nonGroundNodeCount = nodeCount - 1;
		Node nonGroundNodes[] = nonGroundNodeCount > 0 ? new Node[nonGroundNodeCount] : null;
		int nodeCounter = 0;
		for (Node node : this.nodes)
		{
			if (!node.equals(groundNode))
			{
				nonGroundNodes[nodeCounter] = node;
				node.identifier = nodeCounter;
				nodeCounter++;
			}
			else
			{
				node.identifier = -1;
			}
		}
		HashMap<BlockPos, VoltageSourceElement> vSourceMap = new HashMap<BlockPos, VoltageSourceElement>();
		HashMap<BlockPos, ResistorElement> resistorMap = new HashMap<BlockPos, ResistorElement>();
		int vCounter = 0;
		int rCounter = 0;
		for (CircuitElement element : this.components.values())
		{
			if (element instanceof VoltageSourceElement)
			{
				vSourceMap.put(element.componentPos, (VoltageSourceElement)element);
				element.identifier = vCounter;
				vCounter++;
			}
			else if (element instanceof ResistorElement)
			{
				resistorMap.put(element.componentPos, (ResistorElement)element);
				element.identifier = rCounter;
				rCounter++;
			}
		}
		// matrix solver requires at least two nodes OR at least once source, it will crash if one of these requirements is not met
		if (vCounter == 0) // no sources -- matrix solver may crash, but we don't need to solve because all power will be 0, so just do that
		{
			
			for (ResistorElement resistor : resistorMap.values())
			{
				resistor.power = 0;
			}
			return;
		}
		
		//int independantSourceCount = vSourceMap.size();
		//int resistorCount = resistorMap.size();
		VoltageSourceElement[] vSourceArray = new VoltageSourceElement[vCounter];
		ResistorElement[] resistorArray = new ResistorElement[rCounter];
		for (VoltageSourceElement source : vSourceMap.values())
		{
			vSourceArray[source.identifier] = source;
		}
		for (ResistorElement resistor : resistorMap.values())
		{
			resistorArray[resistor.identifier] = resistor;
		}
		
		// everything's organized now! /s
		
		
		double[][] matrixDataA = new double[nonGroundNodeCount + vCounter][nonGroundNodeCount + vCounter];
		double[] matrixDataZ = new double[nonGroundNodeCount + vCounter];
		
		// Matrix A contains known quantities relating to resistance, conductance, and the direction of current
		// it can be broken up into four sub-matrices:
		//	G	B
		//	C	D
		// G = n*n and is based on the resistors that touch the non-ground nodes of the circuit
		for (int i=0; i<nonGroundNodeCount; i++)	// if there are no non-ground nodes, then G, B, and C will be skipped. Only D will be built
		{
			for (int j=0; j<nonGroundNodeCount; j++)
			{
				if (i==j)	// same node, diagonal across matrix A (top left to bottom right)
				{	// diagonals across G where i=j=n are equal to sum(1/R) of all R that touch node n
					Node checkNode = nonGroundNodes[i];
					for (BlockPos pos : checkNode.connectedComponents)
					{
						if (resistorMap.containsKey(pos))
						{
							ResistorElement r = resistorMap.get(pos);
							if (!r.nodeA.equals(r.nodeB))	// don't add shorted resistors here
							{	// sum of conducances (1/R) of all resistors touching this node
								matrixDataA[i][j] += 1/resistorMap.get(pos).getNominalResistance();
							}
						}
					}
				}
				else // not on diagonal, not the same node
				{	// These positions in matrix are equal to sum(-1/R) or all R between node i and node j
					for (BlockPos posi : nonGroundNodes[i].connectedComponents)
					{
						if (resistorMap.containsKey(posi))
						{
							for (BlockPos posj : nonGroundNodes[j].connectedComponents)
							{
								if (posi.equals(posj))
								{	// sum of (-1/R) of all resistors between these nodes
									matrixDataA[i][j] -= 1/resistorMap.get(posi).getNominalResistance();
								}
							}
						}
					}
				}
			}

			// Matrix B is N-vertical, M-horizontal matrix (N*M in matrix notation, M*N in array) where
			// n represents non-ground node
			// m represents voltage source
			// [m,n] is 1 if positive end of source touches node, -1 if negative touches node, 0 if source does not touch node // EDIT: OR SOURCE IS SHORTED
			// Matrix C is the same, but transposed horizontally/vertically
			for (int j=0; j<vCounter; j++)
			{
				matrixDataA[i][nonGroundNodeCount+j] = 0D;
				matrixDataA[nonGroundNodeCount+j][i] = 0D;
				if (nonGroundNodes[i].equals(vSourceArray[j].getPositiveNode()))
				{
					matrixDataA[i][nonGroundNodeCount+j] += 1D;
					matrixDataA[nonGroundNodeCount+j][i] += 1D;
				}
				if (nonGroundNodes[i].equals(vSourceArray[j].getNegativeNode()))
				{
					matrixDataA[i][nonGroundNodeCount+j] -= 1D;
					matrixDataA[nonGroundNodeCount+j][i] -= 1D;
				}
			}
		}
		
		// Matrix D is typically all zeroes (M*M matrix)
		// Using a very small number close to zero is done to handle the case where a voltage source is shorted
		// (otherwise divide by zero can occur)
		// The values in this submatrix are negative resistance values approximately proportional to
		// the resistance of the wires through the source (i.e. very small resistance)
		for (int j=0; j < vCounter; j++)
		{
			matrixDataA[nonGroundNodeCount + j][nonGroundNodeCount + j] = -0.000001D; // replace later
		}
		
		// Matrix Z is a 1-wide, (N+M) tall matrix; it contains the known values of the sources
		// the first N spaces include the sum of the independant current sources giving current to the node (0 if none)
		// the next M spaces are simply the values of the independant voltage sources in the circuit
		
		// ignoring current sources for now
		// voltage sources:
		for (int j=0; j < vCounter; j++)
		{
			matrixDataZ[nonGroundNodeCount + j] = vSourceArray[j].getNominalVoltage();
		}
		
		// Finally, Matrix X has our unknowns: The voltage at each node and the current through each voltage source
		// We need to invert A and multiply A^-1 by Z to get X
		// code thanks to "duffymo"'s reply to this stackoverflow question:
		//		https://stackoverflow.com/questions/1992638/java-inverse-matrix-calculation
		RealMatrix realMatrixA = new Array2DRowRealMatrix(matrixDataA);
		System.out.println("Matrix A: " + realMatrixA);
		DecompositionSolver solver = new LUDecomposition(realMatrixA).getSolver();
		RealVector vectorZ = new ArrayRealVector(matrixDataZ);
		System.out.println("Vector Z: " + vectorZ);
		RealVector vectorX = solver.solve(vectorZ);
		System.out.println("Solution X: " + vectorX);
		// To reiterate: The first N values in vectorX are the voltage at each node, where
		// vectorX[n] is the voltage at the node with identifier n
		// (ground node has voltage 0 and identifier -1)
		// the next M values are the current through each voltage source, from positive terminal through negative terminal
		// (this current will generally have a negative value)
		
		// we have all the data we need, now notify each element of its power consumption
		for (ResistorElement resistor : resistorArray)
		{
			// determine the difference between voltage of the two adjacent nodes
			// abs(voltage differential) * resistance = power consumption
			
			// TODO this is a hack; sometimes nodes that are equal() to ground nodes pop up and their identifier isn't getting properly set, fix later
//			if (resistor.nodeA.equals(groundNode))
//			{
//				resistor.nodeA = groundNode;
//			}
//			if (resistor.nodeB.equals(groundNode))
//			{
//				resistor.nodeB = groundNode;
//			}
			
			int nodeIDA = resistor.nodeA.identifier;
			int nodeIDB = resistor.nodeB.identifier;
			double vA = (nodeIDA == -1 ? 0D : vectorX.getEntry(nodeIDA));
			double vB = (nodeIDB == -1 ? 0D : vectorX.getEntry(nodeIDB));
			double voltageDiff = Math.abs(vA - vB);
			double power = voltageDiff * voltageDiff / resistor.getNominalResistance();	// P = V^2 / R
			resistor.power = power;
			System.out.println("Resistor R" + resistor.identifier + " between nodes N" + (nodeIDA + 1) + " and N" + (nodeIDB + 1) + " has power of " + EngineeringNotation.toSIUnit(power, "W"));
		}
		for (VoltageSourceElement source : vSourceArray)
		{
			
			// TODO this is a hack; sometimes nodes that are equal() to ground nodes pop up and their identifier isn't getting properly set, fix later
			if (source.nodeA.equals(groundNode))
			{
				source.nodeA = groundNode;
			}
			if (source.nodeB.equals(groundNode))
			{
				source.nodeB = groundNode;
			}
			
			int nodeIDA = source.nodeA.identifier;
			int nodeIDB = source.nodeB.identifier;
			double power = source.getNominalVoltage() * vectorX.getEntry(nonGroundNodeCount + source.identifier);	// P = V * I
			source.power = power;
			System.out.println("Source V" + source.identifier + " between nodes N" + (nodeIDA + 1) + " and N" + (nodeIDB + 1) + " has power of " + EngineeringNotation.toSIUnit(power, "W"));
		}
	}
	
	@Nonnull
	private static Circuit expandCircuitFromNode(World world, Circuit circuit, Node baseNode)
	{
		if (circuit.nodes.contains(baseNode))
		{
			return circuit;
		}
		circuit.nodes.add(baseNode);
		
		// for each component the node touches
		// reminder: components can only have two electrical connections
		for (BlockPos componentPos : baseNode.connectedComponents)
		{
			if (circuit.components.containsKey(componentPos))	// we already did this one
			{
				continue;
			}
			IBlockState componentState = world.getBlockState(componentPos);
			Block componentBlock = componentState.getBlock();
			if (componentBlock instanceof IElectricalBlock)
			{
				Set<EnumFacing> checkFaces = ((IElectricalBlock)componentBlock).getConnectingFaces(world, componentState, componentPos);
				int faceCount = 0; // number of connected faces for short-finding purposes
				for (EnumFacing face : checkFaces)
				{
					// ASSUMPTION: components only have two connections
					// one of these will connect to the current node
					// the other connection is to one of the following things:
					// 1) a wire that connects back
					// 2) another component that connects back
					// 3) a non-electrical block, or an electrical block that does not connect back
					
					BlockPos checkPos = componentPos.offset(face);
					if (baseNode.contains(checkPos))
					{
						faceCount++;
						// we also need to check if the node is shorted
						// if both blocks adjacent to the component are part of the same node
						// then the component is shorted and we must handle it here
						if (faceCount >= 2 && !circuit.components.containsKey(componentPos))
						{
							circuit.addCircuitComponent(world, componentPos, baseNode, baseNode);
						}
						continue;
					}
					IBlockState checkState = world.getBlockState(checkPos);
					Block checkBlock = checkState.getBlock();
					if (checkBlock instanceof IElectricalBlock && CircuitHelper.doTwoBlocksConnect(world, componentPos, checkPos))
					{
						if (CategoriesOfBlocks.wireBlocks.contains(checkBlock))	// case 1) normal node
						{
							Node newNode = Node.buildNodeFrom(world, checkPos);
							circuit.addCircuitComponent(world, componentPos, baseNode, newNode);
							circuit = Circuit.expandCircuitFromNode(world, circuit, newNode);
							break;
						}
						else if (CategoriesOfBlocks.isAnyComponentBlock(checkBlock))	// case 2) virtual node
						{
							Node newNode = Node.createVirtualNode(componentPos, checkPos);
							circuit.addCircuitComponent(world, componentPos, baseNode, newNode);
							circuit = Circuit.expandCircuitFromNode(world, circuit, newNode);
							break;
						}
						else	// shouldn't happen, but just fall back to case 3)
						{
							Node newNode = Node.createDeadNode(componentPos);
							circuit.addCircuitComponent(world, componentPos, baseNode, newNode);
							circuit = Circuit.expandCircuitFromNode(world, circuit, newNode);
							break;
						}
					}
					else	// case 3) dead node
					{
						Node newNode = Node.createDeadNode(componentPos);
						circuit.addCircuitComponent(world, componentPos, baseNode, newNode);
						circuit = Circuit.expandCircuitFromNode(world, circuit, newNode);
						break;
					}
				}
			}
		}
		return circuit;
	}
	
//	@Nonnull
//	private static Circuit expandCircuitFromNode(World world, Circuit circuit, Node baseNode)
//	{
//		// for each component the node touches,
//		// ASSUMPTION: "components" only have two connecting sides
//		if (circuit.nodes.contains(baseNode))
//		{
//			return circuit;
//		}
//		System.out.println("Adding node to circuit");
//		circuit.nodes.add(baseNode);
//		
//		// the node builder won't add a component if there's no nodes attached to at least two sides of the component
//		
//		for (BlockPos componentPos : baseNode.connectedComponents)
//		{
//			Set<EnumFacing> checkFaces;
//			IBlockState componentState = world.getBlockState(componentPos);
//			
//			// check if component already has a valid circuit
//			// if it does, return that node's circuit and use that circuit
//			/*if (componentState.hasTileEntity())
//			{
//				TileEntity te = world.getTileEntity(componentPos);
//				if (te instanceof ICircuitElementHolderTE)
//				{
//					Circuit teCircuit = ((ICircuitElementHolderTE)te).getCircuit();
//					if (teCircuit.isValid())
//					{
//						return teCircuit;
//					}
//				}
//			}*/
//			
//
//			Block componentBlock = componentState.getBlock();
//			
//			if (componentBlock instanceof IElectricalBlock)
//			{
//				checkFaces = ((IElectricalBlock)componentBlock).getConnectingFaces(world, componentState, componentPos);
//			}
//			else
//			{	
//				//System.out.println("Electrical component at " + componentPos.toString() + " not marked as electrical block! This is an error");
//				return INVALID_CIRCUIT;
//			}
//			
//			BlockPos prevPos = null;	// wire block on old node
//			BlockPos nextPos = null;	// wire block on new node
//
//			// components should have exactly 2 connections
//			// less than 2 -- remove it from node
//			// more than 2 -- invalid component
//			
//			// HACK until lightbulb connections are done correctly
//			for (EnumFacing face : checkFaces)
//			{
//				BlockPos checkPos = componentPos.offset(face);
//				IBlockState checkState = world.getBlockState(checkPos);
//				Block checkBlock = checkState.getBlock();
//				if ((world.getBlockState(checkPos).getBlock() instanceof IElectricalBlock))
//				{
//					if (prevPos == null && baseNode.contains(checkPos))
//					{
//						prevPos = checkPos;
//					}
//					else if (nextPos == null)
//					{
//						nextPos = checkPos;
//					}
//					else
//					{
//						System.out.println("Component found at " + componentPos.toString() + " has more than two connections! This is not supported yet");
//						return INVALID_CIRCUIT;
//					}
//				}
//			}
//			
//			if (prevPos == null || nextPos == null)
//			{	// not enough connections
//				if (prevPos != null)
//				{
//					System.out.println("Adding dead node");
//					Node newNode = Node.createDeadNode(componentPos);
//					circuit.nodes.add(newNode);
//					circuit.addCircuitComponent(world, componentPos, baseNode, newNode);
//					continue;
//				}
//				else if (nextPos != null)
//				{
//					System.out.println("Adding dead node");
//					Node newNode = Node.createDeadNode(nextPos);
//					circuit.nodes.add(newNode);
//					circuit.addCircuitComponent(world, componentPos, baseNode, newNode);
//					continue;
//				}
//				else
//				{
//					// component has no connections
//					Node newNode1 = Node.createDeadNode(componentPos);
//					Node newNode2 = Node.createDeadNode(componentPos);
//					circuit.nodes.add(newNode1);
//					circuit.nodes.add(newNode2);
//					circuit.addCircuitComponent(world,  componentPos, newNode1, newNode2);
//					continue;
//					//System.out.println("Component at position " + componentPos.toString() + " has no nodes attached! How did this even happen");
//				}
//			}
//			// end HACK
//			
//			Block prevBlock = world.getBlockState(prevPos).getBlock();
//			Block nextBlock = world.getBlockState(nextPos).getBlock();
//			
//			// if next node is a virtual node
//			if (CategoriesOfBlocks.isAnyComponentBlock(nextBlock))
//			{
//				// check if the virtual node already exists
//				if (circuit.components.containsKey(nextPos))
//				{
//					CircuitElement nextComponent = circuit.components.get(nextPos);
//					if (nextComponent.nodeA.contains(componentPos))
//					{
//						circuit.addCircuitComponent(world, componentPos, baseNode, nextComponent.nodeA);
//					}
//					else if (nextComponent.nodeB.contains(componentPos))
//					{
//						circuit.addCircuitComponent(world, componentPos, baseNode, nextComponent.nodeB);
//					}
//					else	// add dead node so we don't have to deal with this
//					{
//						Node newNode = Node.createDeadNode(componentPos);
//						circuit.nodes.add(newNode);
//						circuit.addCircuitComponent(world, componentPos, baseNode, newNode);
//					}
//				}
//				else
//				{
//					Node newNode = Node.createVirtualNode(componentPos, nextPos);
//					circuit.addCircuitComponent(world, componentPos, baseNode, newNode);
//					circuit = Circuit.expandCircuitFromNode(world, circuit, newNode);
//					continue;
//				}
//			}
//			else	// next node starts with a wire block
//			{
//				if (circuit.isPositionInAnyKnownNode(nextPos)) // if position is already in circuit, ignore
//				{
//					// fix for circuit element finder
//					Node nextNode = circuit.getNodeAtWireLocation(nextPos);
//					circuit.addCircuitComponent(world, componentPos, baseNode, nextNode);
//					continue;
//				}
//				else // new node
//				{
//					Node newNode = Node.buildNodeFrom(world, componentPos, nextPos);
//					circuit.addCircuitComponent(world, componentPos, baseNode, newNode);
//					circuit = Circuit.expandCircuitFromNode(world, circuit, newNode);
//				}
//			}
//		}
//		
//		return circuit;
//	}
	
	/**
	 * Returns a Node in this circuit that contains the specified block position.
	 * If no Nodes in this circuit contains the specified position, Null is returned.
	 * Component positions are ignored.
	 */
	@Nullable
	public Node getNodeAtWireLocation(BlockPos pos)
	{
		for (Node node : this.nodes)
		{
			if (node.wireBlocks.contains(pos))
			{
				return node;
			}
		}
		return null;
	}
	
	public void printToConsole(World world)
	{
		/*HashMap<BlockPos, String> vComponents = new HashMap<BlockPos, String>();
		HashMap<BlockPos, String> rComponents = new HashMap<BlockPos, String>();
		
		int nodeID = 0;
		int vID = 1;
		int rID = 1;
		
		HashMap<Node, String> nodes = new HashMap<Node, String>();
		for (CircuitElement component : this.components.values())
		{
			String componentLine = "Component ";
			if (CategoriesOfBlocks.activeComponentBlocks.contains(component.componentState.getBlock()))
			{
				componentLine = componentLine + "V" + vID + ": ";
				vID++;
			}
			else if (CategoriesOfBlocks.passiveComponentBlocks.contains(component.componentState.getBlock()))
			{
				componentLine = componentLine + "R" + rID + ": ";
				rID++;
			}
			Node nodeA = component.nodeA;
			Node nodeB = component.nodeB;
			if (!nodes.containsKey(nodeA))
			{
				String nodeName = "N" + Integer.toString(nodeID);
				nodes.put(nodeA, nodeName);
				nodeID++;
			}
			componentLine = componentLine + nodes.get(nodeA) + ' ';
			if (!nodes.containsKey(nodeB))
			{
				String nodeName = "N" + Integer.toString(nodeID);
				nodes.put(nodeB, nodeName);
				nodeID++;
			}
			componentLine = componentLine + nodes.get(nodeB) + ' ';
			System.out.println(componentLine);
		}*/
		/*for (Node node : this.nodes)
		{
			String nodeLine = "Node " + nodeID + ":";
			LinkedList<String> vList = new LinkedList<String>();
			LinkedList<String> rList = new LinkedList<String>();
			
			for (BlockPos pos : node.connectedComponents)
			{
				Block componentBlock = world.getBlockState(pos).getBlock();
				if (CategoriesOfBlocks.activeComponentBlocks.contains(componentBlock))
				{
					if (vComponents.containsKey(pos))
					{
						vList.add(vComponents.get(pos));
					}
					else
					{
						String vName = "V" + vID;
						vID++;
						vComponents.put(pos, vName);
						vList.add(vName);
					}
				}
				if (CategoriesOfBlocks.passiveComponentBlocks.contains(componentBlock))
				{
					if (rComponents.containsKey(pos))
					{
						rList.add(rComponents.get(pos));
					}
					else
					{
						String rName = "R" + rID;
						rID++;
						rComponents.put(pos, rName);
						rList.add(rName);
					}
				}
			}

			vList.sort(null);
			rList.sort(null);
			
			for (String s : vList)
			{
				nodeLine = nodeLine + " " + s;
			}
			for (String s : rList)
			{
				nodeLine = nodeLine + " " + s;
			}
			
			System.out.println(nodeLine);
			
			nodeID++;
		}*/
	}
}
