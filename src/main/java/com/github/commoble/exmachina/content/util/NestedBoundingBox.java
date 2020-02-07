package com.github.commoble.exmachina.content.util;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.util.math.AxisAlignedBB;

public class NestedBoundingBox
{
	private final Optional<NestedBoundingBox> boxA;
	private final Optional<NestedBoundingBox> boxB;
	private final @Nonnull AxisAlignedBB superBox;	// if boxA and boxB are both present, this is the union of their superboxes
	
	public NestedBoundingBox(@Nonnull NestedBoundingBox boxA, @Nonnull NestedBoundingBox boxB)
	{
		this.boxA = Optional.of(boxA);
		this.boxB = Optional.of(boxB);
		this.superBox = boxA.superBox.union(boxB.superBox);
	}
	
	public NestedBoundingBox(@Nonnull AxisAlignedBB box)
	{
		this.boxA = Optional.empty();
		this.boxB = Optional.empty();
		this.superBox = box;
	}
	
	public NestedBoundingBox combine(Optional<NestedBoundingBox> other)
	{
		return other.map(nbbB -> new NestedBoundingBox(this, nbbB)).orElse(this);
	}
	
	public NestedBoundingBox combine(@Nonnull NestedBoundingBox other)
	{
		return new NestedBoundingBox(this, other);
	}
	
	public boolean contains(AxisAlignedBB target)
	{
		return this.contains(target, box -> box.contains(target));
	}
	
	private boolean contains(AxisAlignedBB target, Function<NestedBoundingBox, Boolean> boxContainsTarget)
	{
		if (!this.superBox.intersects(target))
		{
			return false;
		}
		else
		{
			return this.boxA.map(boxContainsTarget).orElseGet(() -> this.boxB.map(boxContainsTarget).orElse(false));
		}
	}
}
