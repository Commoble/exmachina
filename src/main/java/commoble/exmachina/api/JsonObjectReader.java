package com.github.commoble.exmachina.api;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

@FunctionalInterface
/** This is just Function<JsonObject, T> but with non-null arg and throws declaration **/
public interface JsonObjectReader<T>
{
	public T deserialize(@Nonnull JsonObject jsonObject) throws JsonParseException;
}
