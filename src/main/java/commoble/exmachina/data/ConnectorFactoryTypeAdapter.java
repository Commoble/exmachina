package commoble.exmachina.data;

import java.lang.reflect.Type;

import javax.annotation.Nonnull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import commoble.exmachina.ExMachina;
import commoble.exmachina.api.ConnectorFactory;
import commoble.exmachina.api.JsonObjectReader;
import net.minecraft.util.ResourceLocation;

public class ConnectorFactoryTypeAdapter implements JsonDeserializer<ConnectorFactory>
{
	public static final ConnectorFactoryTypeAdapter INSTANCE = new ConnectorFactoryTypeAdapter();
	
	@Override
	public ConnectorFactory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		// "connector" field in jsons can either be a string or json object or null
		// if it's null, return an empty set generator
		if (json == null || json.isJsonNull())
		{
			return null;
		}
		else
		{
			JsonObject object = getJsonObject(json);
			JsonElement typeElement = object.get("type");
			if (!typeElement.isJsonPrimitive())
			{
				throw new JsonParseException("Failed to parse component connector field: Connector field objects must contain a string field named `type`");
			}
			String typeName = typeElement.getAsString();
			JsonObjectReader<ConnectorFactory> deserializer = ExMachina.INSTANCE.circuitBehaviourRegistry.connectionTypes.get(new ResourceLocation(typeName));
			if (deserializer == null)
			{
				throw new JsonParseException(String.format("Failed to parse component property object: no connector property deserializer registered for: %s", typeName));
			}
			return deserializer.deserialize(object);
		}
	}
	
	private static JsonObject getJsonObject(@Nonnull JsonElement json) throws JsonParseException
	{
		if (json.isJsonObject())
		{
			return json.getAsJsonObject();
		}
		// if it's a string, generate an object with just the type field
		else if (json.isJsonPrimitive())
		{
			JsonPrimitive primitive = json.getAsJsonPrimitive();
			if (!primitive.isString())
			{
				throw new JsonParseException("Failed to parse component connector field: Connector fields must contain null, a string, or an object");
			}
			String typeString = primitive.getAsString();
			ResourceLocation identifier = new ResourceLocation(typeString);
			JsonObjectReader<ConnectorFactory> reader = ExMachina.INSTANCE.circuitBehaviourRegistry.connectionTypes.get(identifier);
			if (reader == null)
			{
				// if no simple factory for the given name exists, throw a parse error
				throw new JsonParseException(String.format("Failed to parse component connector field: No connection type registered to identifier %s", typeString));
			}
			JsonObject object = new JsonObject();
			object.addProperty("type", typeString);
			return object;
		}
		else
		{
			throw new JsonParseException("Failed to parse component connector field: Connector fields must contain null, a string, or an object");
		}
	}
}
