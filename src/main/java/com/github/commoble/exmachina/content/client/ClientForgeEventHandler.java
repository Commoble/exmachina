package com.github.commoble.exmachina.content.client;

import com.github.commoble.exmachina.ExMachinaMod;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid=ExMachinaMod.MODID, value= {Dist.CLIENT}, bus=Bus.FORGE)
public class ClientForgeEventHandler
{
}
