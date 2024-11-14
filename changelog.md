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
