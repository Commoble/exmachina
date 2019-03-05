package com.github.commoble.exmachina.common.tileentity;

import com.github.commoble.exmachina.common.electrical.Circuit;
import com.github.commoble.exmachina.common.electrical.CircuitElement;
import com.github.commoble.exmachina.common.electrical.Node;

/**
 * This interface implies that the implementing class holds a CircuitElement field 
 *
 */
public interface ICircuitElementHolderTE
{
	/** Intended usage: Create the circuit element, set its nodes to the given nodes, and apply it
	 * to the implementing class's CircuitElement field,
	 * and then return the CircuitElement
	 */
	public CircuitElement createCircuitElement(Node nodeA, Node nodeB);
	public CircuitElement getCircuitElement();
	
	/**
	 * Called when the TE's block receives a state or neighbor change,
	 * or another block (like a wire) notifies this of its state or neighbor changing 
	 */
	public void onPossibleCircuitUpdate();
	
	/** Invalidates the TE's circuit and tries to rebuild circuit(s) if possible **/
	public void invalidateCircuit();
	public Circuit getCircuit();
	public void setCircuit(Circuit circuit);
}
