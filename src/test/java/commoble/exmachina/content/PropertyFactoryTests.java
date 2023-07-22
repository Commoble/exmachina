package commoble.exmachina.content;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Direction;

public class PropertyFactoryTests
{
	
	@Test
	void testRotatableConnectorCodec()
	{
		String json = "{\r\n" + 
			"	\"type\": \"exmachina:directions\",\r\n" + 
			"	\"values\": [\"up\", \"down\"],\r\n" + 
			"	\"direction_property\": \"facing\",\r\n" + 
			"	\"unrotated_direction\": \"south\"\r\n" + 
			"}";
		Direction[] expectedDirections = {Direction.UP, Direction.DOWN};
		String expectedProperty = "facing";
		Direction unrotatedDirection = Direction.SOUTH;
		
		// when we use the codec to convert json -> java -> nbt -> java -> json -> java
		RotatableConnectorFactory finalFactory = runConversionsAndTestTypeName(RotatableConnectorFactory.CODEC, "exmachina:directions", json);
		// then it should retain the initial values
		Assertions.assertArrayEquals(expectedDirections, finalFactory.getNominalDirections());
		Assertions.assertEquals(expectedProperty, finalFactory.getDirectionProperty().orElse("wrong"));
		Assertions.assertEquals(unrotatedDirection, finalFactory.getUnrotatedDirection());
	}
	@Test
	void testMinimalRotatableConnectorCodec()
	{
		
		// also test another json without the optional fields
		String minimalJson = "{\r\n" + 
			"	\"type\": \"exmachina:directions\"\r\n" + 
			"}";

		RotatableConnectorFactory minimalFactory = runConversionsAndTestTypeName(RotatableConnectorFactory.CODEC, "exmachina:directions", minimalJson);
		// then it should retain the initial values
		Assertions.assertArrayEquals(new Direction[0], minimalFactory.getNominalDirections());
		Assertions.assertFalse(minimalFactory.getDirectionProperty().isPresent());
		Assertions.assertEquals(Direction.NORTH, minimalFactory.getUnrotatedDirection());
	}
	
	@Test
	void testScaledPropertyCodec()
	{
		// given a scaled property factory json
		String json = "{\r\n" + 
			"	\"type\": \"exmachina:property\",\r\n" + 
			"	\"property\": \"level\",\r\n" + 
			"	\"scale\": 5.0\r\n" + 
			"}";
		// when we use the codec to convert json -> java -> nbt -> java -> json -> java
		ScaledPropertyFactory finalFactory = runConversionsAndTestTypeName(ScaledPropertyFactory.CODEC, "exmachina:property", json);
		// then it should retain the initial values
		Assertions.assertEquals("level", finalFactory.getPropertyName());
		Assertions.assertEquals(5.0D, finalFactory.getScale());
	}
	
	@Test
	void testConstantPropertyFactoryCodec()
	{
		String json = "{\r\n" + 
			"	\"type\": \"exmachina:constant\",\r\n" + 
			"	\"value\": 4\r\n" + 
			"}";

		// when we use the codec to convert json -> java -> nbt -> java -> json -> java
		ConstantPropertyFactory finalFactory = runConversionsAndTestTypeName(ConstantPropertyFactory.CODEC, "exmachina:constant", json);
		// then it should retain the initial values
		Assertions.assertEquals(4.0D, finalFactory.getValue());
	}
	
	@Test
	void testStateTableCodec()
	{
		String json = "{\r\n" + 
			"	\"type\": \"exmachina:blockstate\",\r\n" + 
			"	\"variants\":\r\n" + 
			"	{\r\n" + 
			"		\"level=0\": 1,\r\n" + 
			"		\"level=1\": 10\r\n" + 
			"	}\r\n" + 
			"}";
		
		Map<String, Double> expectedMap = new HashMap<>();
		expectedMap.put("level=0", 1D);
		expectedMap.put("level=1", 10D);
		
		// when we use the codec to convert json -> java -> nbt -> java -> json -> java
		StateTablePropertyFactory finalFactory = runConversionsAndTestTypeName(StateTablePropertyFactory.CODEC, "exmachina:blockstate", json);
		// then it should retain the initial values
		Assertions.assertEquals(expectedMap, finalFactory.getVariants());
	}
	
	private static <T> T runConversionsAndTestTypeName(Codec<T> codec, String typeName, String json)
	{
		// rationale: we want to test all four of these:
			// java -> json
			// json -> java
			// java -> nbt
			// nbt -> java
		// but we also want to start with json because that will be the starting point in practice
		T factoryAfterRead = codec.decode(JsonOps.INSTANCE, new JsonParser().parse(json)).result().get().getFirst();
		INBT nbt = codec.encodeStart(NBTDynamicOps.INSTANCE, factoryAfterRead).result().get();
		T factoryAfterNBT = codec.decode(NBTDynamicOps.INSTANCE, nbt).result().get().getFirst();
		JsonElement jsonAgain = codec.encodeStart(JsonOps.INSTANCE, factoryAfterNBT).result().get();
		T finalFactory = codec.decode(JsonOps.INSTANCE, jsonAgain).result().get().getFirst();

		// make sure it saves the type name too
		Assertions.assertEquals(typeName, jsonAgain.getAsJsonObject().get("type").getAsString());
		
		return finalFactory;
	}
}
