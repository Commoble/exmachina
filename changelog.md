# 1.21.3-0.8.0.1
* Fix default signal component providing the set of nodes that shouldn't connect instead of the set of nodes that should

# 1.21.3-0.8.0.0
* signal_transmitter -> signal_component

# 1.21.3-0.7.0.0
* Combined SignalSource/Transmitter/Receiver into a single SignalComponent class
* SignalComponents now must provide a collection of TransmissionNodes for a given context, which provide
  * A NodeShape, which must be unique for the level+pos+channel in context (multiple TransmissionNodes with different NodeShapes can exist for that context)
  * A function which provides power to the graph on the given channel (can provide 0 if no power provided)
  * A set of directions to read vanilla power from
  * A set of graph keys the node is allowed to connect to
  * A listener callback which can perform internal side-effects of graph updates and provide a list of directions to update neighbors in
* A SignalComponent can also declare whether the associated block can receive neighbor updates from a graph update (defaults to false)

# 1.21.3-0.6.0.1
* Enqueuing a position to the signal graph now automatically enqueues its six neighbors as well (to ensure receivers are shut off correctly)

# 1.21.3-0.6.0.0
* Signal graph APIs now use a "NodeShape" to determine whether a transmission node can connect to another node
* Three categories of nodeshape:
	* "Cube" shapes represent nodes which fill the entire block
	* "Side" shapes represent a node attached to a face of a block
	* "SideSide" shapes represent a node attached to a face of a block, which can only connect in a single orthagonal direction
* A transmission node's requested nodeshape and the target node's actual nodeshape must be compatible in order to connect to the target node
	* Cubes can connect with any node
	* Sides and SideSides can connect to nodes of the same type of they have the same direction
	* Sides can connect to SideSides if they share the same face
* Fixed a bug where signal sources couldn't connect directly to receivers

# 1.21.3-0.5.0.0
* Signal graphs can now cross dimensions.
* API change:
  * Face now requires a level key on construction, denoting that the Face exists in that level
  * Signal transmitters can now connect to nodes in other dimensions by providing connectable nodes with faces in other dimensions

# 1.21.3-0.4.0.1
* Fix warning in logs re: missing accesstransformer

# 1.21.3-0.4.0.0
* Port to 1.21.3

# 1.21.1-0.3.0.1
* Fixed typo in default signal_source datamap

# 1.21.1-0.3.0.0
* Added the new signal graph APIs. Pulled in the power graph APIs from exmachina_engine which isn't a thing anymore. Content has been set aside for now to be added to More Red later (hopefully).
