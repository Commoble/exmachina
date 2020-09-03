package commoble.exmachina.api;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

@FunctionalInterface
/** This is just Function<JsonObject, T> but with non-null arg**/
public interface JsonObjectReader<T>
{
	public T deserialize(@Nonnull JsonObject jsonObject);
}
