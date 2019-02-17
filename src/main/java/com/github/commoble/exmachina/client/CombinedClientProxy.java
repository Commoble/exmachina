package com.github.commoble.exmachina.client;

import com.github.commoble.exmachina.common.IProxy;

/**
* CombinedClient is used to set up the mod and start it running when installed on a normal minecraft client.
* It should not contain any code necessary for proper operation on a DedicatedServer.
* Code required for both normal minecraft client and dedicated server should go into CommonProxy.
* 
* All client-side-specific things (rendering and textures, mostly) goes in here
*/
public class CombinedClientProxy implements IProxy
{

}
