package com.github.commoble.exmachina.api.circuit;

/**
 * A representation of a component in the circuit
 * indicating the component's position, as well as its adjacent
 * nodes and its electrical characteristics
 */
public class CircuitElement
{
	public final ElementContext context;
	public Node nodeA;
	public Node nodeB;
		// they may be the same node
	
	public double power;	// this is set whenever the circuit is built or rebuilt

	public int identifier; // identifier of the component as seen by the circuit it is contained in
		// (determined when solving the circuit)
		// resistors will be 0,1,2, etc
		// sources will be 0,1,2, etc, independant from resistors

	public CircuitElement(ElementContext context, Node nodeA, Node nodeB)
	{
		this.context = context;
		this.nodeA = nodeA;
		this.nodeB = nodeB;
	}
	
	@Override
	public int hashCode()
	{
		return this.context.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (o instanceof CircuitElement && ((CircuitElement)o).context.equals(this.context));
	}
	
	public ElectricalValues getElectricalValues()
	{
		return this.context.getElectricalValues(this);
	}
}
