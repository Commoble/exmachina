package com.github.commoble.exmachina.content.util;

import java.util.Optional;
import java.util.function.BinaryOperator;

public class OptionalHelper
{
	/** Returns A + B if both options are present, A or B if only one is present, empty if none are present **/
	public static <T> Optional<T> mergeOptionals(Optional<T> A, Optional<T> B, BinaryOperator<T> operator)
	{
		return A.map(a->
			B.map(b-> Optional.ofNullable(operator.apply(a, b)))
			.orElse(A))
			.orElse(B);
	}
}
