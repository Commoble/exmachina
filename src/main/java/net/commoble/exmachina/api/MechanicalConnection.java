package net.commoble.exmachina.api;

/**
 * Represents properties of some connection from one mechanical node to another.
 * @param parity Parity transformation of the connection.
 * If POSITIVE, no parity transformation.
 * If NEGATIVE, the connection inverts parity, causing the nodes to rotate in opposite directions relative to their axes.
 * If ZERO, the existing of connection zeroes the mechanical graph, causing no nodes to rotate.
 * Mutual connections must have the same parity or will else zero themselves.
 * @param teeth How many "gear teeth" the node forming the outgoing connection has.
 * If both nodes of a mutual connection specify teeth > 0, a gearshift occurs,
 * causing the node with fewer teeth to rotate faster with a speed ratio equal to the teeth ratio.
 * If either half of the connection specify non-positive teeth, no gearshift occurs.
 */
public record MechanicalConnection(Parity parity, int teeth)
{

}
