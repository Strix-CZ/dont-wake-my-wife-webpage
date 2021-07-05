package online.temer.alarm.admin;

import java.util.Arrays;
import java.util.List;

public class Output
{
	public final int exitCode;
	public final List<String> lines;

	public Output(int exitCode, String... lines)
	{
		this.exitCode = exitCode;
		this.lines = Arrays.asList(lines);
	}
}
