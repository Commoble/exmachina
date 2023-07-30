package commoble.exmachina.gametest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import commoble.exmachina.ExMachina;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

public class GameTestBuilder
{
	private String batchName = "defaultBatch";
	private Rotation rotation = Rotation.NONE;
	private int maxTicks = 100;
	private long setupTicks = 0;
	private boolean required = true;
	private int requiredSuccesses = 1;
	private int maxAttempts = 1;

	public GameTestBuilder batchName(String batchName) { this.batchName = batchName; return this; }
	public GameTestBuilder rotation(Rotation rotation) { this.rotation = rotation; return this; }
	public GameTestBuilder maxTicks(int maxTicks) { this.maxTicks = maxTicks; return this; }
	public GameTestBuilder setupTicks(int setupTicks) { this.setupTicks = setupTicks; return this; }
	public GameTestBuilder required(boolean required) { this.required = required; return this; }
	public GameTestBuilder requiredSuccesses(int requiredSuccesses) { this.requiredSuccesses = requiredSuccesses; return this; }
	public GameTestBuilder maxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; return this; }
	
	public static Collection<TestFunction> spin(String testName, String structureName, Consumer<GameTestHelper> function)
	{
		return spin(testName, structureName, b -> {}, function);
	}
	
	public static Collection<TestFunction> spin(String testName, String structureName, Consumer<GameTestBuilder> builder, Consumer<GameTestHelper> function)
	{
		List<TestFunction> list = new ArrayList<>();
		for (Rotation r : Rotation.values())
		{
			GameTestBuilder b = new GameTestBuilder();
			builder.accept(b);
			list.add(b.build(testName + r.ordinal(), structureName, function));
		}
		return list;
	}
	
	public TestFunction build(String testName, String structureName, Consumer<GameTestHelper> function)
	{
		return new TestFunction(
			this.batchName,
			testName,
			new ResourceLocation(ExMachina.MODID, structureName).toString(),
			this.rotation,
			this.maxTicks,
			this.setupTicks,
			this.required,
			this.requiredSuccesses,
			this.maxAttempts,
			function);
	}
}
