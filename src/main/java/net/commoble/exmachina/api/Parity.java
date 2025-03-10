package net.commoble.exmachina.api;

public enum Parity
{
	ZERO(0),
	POSITIVE(1),
	NEGATIVE(-1);
	
	private int value;
	
	Parity(int value)
	{
		this.value = value;
	}
	
	public int value()
	{
		return this.value;
	}
	
	public Parity multiply(Parity that)
	{
		int newValue = this.value * that.value;
		return newValue == 1 ? POSITIVE
			: newValue == -1 ? NEGATIVE
			: ZERO;
	}
}
