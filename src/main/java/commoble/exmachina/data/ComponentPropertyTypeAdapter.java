package com.github.commoble.exmachina.data;

import java.lang.reflect.Type;
import java.util.function.DoubleFunction;
import java.util.function.Function;

import com.github.commoble.exmachina.api.JsonObjectReader;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import net.minecraft.util.ResourceLocation;

public class ComponentPropertyTypeAdapter<T> implements JsonDeserializer<T>
{
	private final String mapName;
	private final Function<ResourceLocation, JsonObjectReader<T>> map;
	private final DoubleFunction<T> fromConstant;
	
	public ComponentPropertyTypeAdapter(String mapName, Function<ResourceLocation, JsonObjectReader<T>> map, DoubleFunction<T> fromConstant)
	{
		this.mapName = mapName;
		this.map = map;
		this.fromConstant = fromConstant;
	}

	@Override
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		if (json == null || json.isJsonNull())
		{
			return null;
		}
		else if (json.isJsonPrimitive()) // allow raws to define e.g. "static_load": 5
		{
			JsonPrimitive primitive = json.getAsJsonPrimitive();
			if (primitive.isNumber())
			{
				double value = primitive.getAsDouble();
				return this.fromConstant.apply(value);
			}
			else
			{
				throw new JsonParseException("Failed to parse component property field: Component property fields must be a number or json object");
			}
		}
		else // otherwise treat as a json object; only numbers and objects are valid
		{
			if (!json.isJsonObject())
			{
				throw new JsonParseException("Failed to parse component property field: Component property fields must be a number or json object");
			}
			
			JsonObject object = json.getAsJsonObject();
			JsonElement typeField = object.get("type");
			if (typeField == null)
			{
				throw new JsonParseException("Failed to parse component property field: Component property objects must declare a type field");
			}

			String typeName = typeField.getAsString();
			JsonObjectReader<T> deserializer = this.map.apply(new ResourceLocation(typeName));
			if (deserializer == null)
			{
				throw new JsonParseException(String.format("Failed to parse component property object: No {} property deserializer registered for: {}", this.mapName, typeName));
			}
			return deserializer.deserialize(object);
		}
	}

}
