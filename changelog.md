# 1.21.5-0.11.0.1-beta
* Fix incorrect mechanical update behavior where manual updates were invoking updates on neighbors and automatic updates were not. It should be the other way around.
* Added Parity.inversion(Direction, Direction) helper method to create a parity inversion for interlocking gears

# 1.21.5-0.11.0.0-beta
* Updated to 1.21.5
* Very high gear ratios should now cause mechanical graphs to zero out instead of crashing
* Added block tag exmachina:no_automatic_mechanical_updates
* Reworked how mechanical updates work:
  * When a block update occurs, if the new block is in the exmachina:no_automatic_mechanical_updates tag, automatic mechanical updates will not occur on that block or its neighbors
  * When a block update occurs on a block which is not in that tag, mechanical updates on neighbors will only occur on neighbors which are not in that tag
  * Manual updates invoked by firing the exmachina:mechanical_graph_update game event will now only enqueue an update at the game event position (and not the six neighbor positions)

# 1.21.4-0.10.0.0-beta
* Added Mechanical Graph API which works similarly to the signal graph
* Mechanical Components can be assigned via the exmachina:mechanical_component datapack registry
* Mechanical Component Types can be registered to the exmachina:mechanical_component_type registry, which define the nodes provided by a blockstate, their torque/inertia, and what connections they can form
* Mechanical graph updates can be scheduled via ExMachinaGameEvents.scheduleMechanicalGraphUpdate or by triggering the exmachina:mechanical_graph_update game event
* Added max_mechanical_graph_size to common config
* Added builtin mechanical components:
  * exmachina:none
    * Provides no nodes
    * This is used as a fallback when a block has no associated component
  * exmachina:variants
    * Assigns nodes to states using property-value matching using a format similar to variants blockstate files
  * exmachina:multipart
    * Assigns unions of nodes to states using a format similar multipart blockstate files
  * Codecs and datagen builders are provided for variants and multipart components
* Moved the helper method for scheduling signal graph updates to ExMachinaGameEvents

# 1.21.4-0.9.0.0-beta
* Update to 1.21.4

# 1.21.3-0.8.0.2-beta
* Fix origin nodes' source power being ignored in signal graphs

# 1.21.3-0.8.0.1-beta
* Fix default signal component providing the set of nodes that shouldn't connect instead of the set of nodes that should

# 1.21.3-0.8.0.0-beta
* signal_transmitter -> signal_component

# 1.21.3-0.7.0.0-beta
* Combined SignalSource/Transmitter/Receiver into a single SignalComponent class
* SignalComponents now must provide a collection of TransmissionNodes for a given context, which provide
  * A NodeShape, which must be unique for the level+pos+channel in context (multiple TransmissionNodes with different NodeShapes can exist for that context)
  * A function which provides power to the graph on the given channel (can provide 0 if no power provided)
  * A set of directions to read vanilla power from
  * A set of graph keys the node is allowed to connect to
  * A listener callback which can perform internal side-effects of graph updates and provide a list of directions to update neighbors in
* A SignalComponent can also declare whether the associated block can receive neighbor updates from a graph update (defaults to false)

# 1.21.3-0.6.0.1-beta
* Enqueuing a position to the signal graph now automatically enqueues its six neighbors as well (to ensure receivers are shut off correctly)

# 1.21.3-0.6.0.0-beta
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
