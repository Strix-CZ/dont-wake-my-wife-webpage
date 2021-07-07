package online.temer.alarm.server;

public class TestExceptionLogger implements ExceptionLogger
{
	@Override
	synchronized public void log(Throwable t)
	{
		System.err.println();
		System.err.println("Exception was thrown during handling the request");
		t.printStackTrace(System.err);
	}
}
