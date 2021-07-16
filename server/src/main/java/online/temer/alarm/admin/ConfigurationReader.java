package online.temer.alarm.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigurationReader
{
	private final File configurationFile;

	public ConfigurationReader(File configurationFile)
	{
		this.configurationFile = configurationFile;
	}

	public String readPassword()
	{
		try
		{
			return new Scanner(configurationFile).useDelimiter("\n").next();
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
}
