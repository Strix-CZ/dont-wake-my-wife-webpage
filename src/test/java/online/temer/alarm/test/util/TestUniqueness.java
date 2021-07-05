package online.temer.alarm.test.util;

import org.assertj.core.api.Assertions;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class TestUniqueness
{
	public static <T> void assertUniqueness(int attempts, Supplier<T> supplier)
	{
		Set<T> values = new HashSet<>(attempts);
		for (int i = 0; i < attempts; i++)
		{
			T newValue = supplier.get();
			if (values.contains(newValue))
			{
				Assertions.fail("A duplicate value was seen. Duplicate:\n" + newValue);
			}
			else
			{
				values.add(supplier.get());
			}
		}
	}
}
