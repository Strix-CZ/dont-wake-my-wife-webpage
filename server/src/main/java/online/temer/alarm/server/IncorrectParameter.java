package online.temer.alarm.server;

public class IncorrectParameter extends IncorrectRequest
{
	public IncorrectParameter(String parameterName)
	{
		super(400, "Incorrect parameter " + parameterName);
	}
}
