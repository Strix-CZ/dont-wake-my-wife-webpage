package online.temer.alarm.server;

public class IncorrectRequest extends RuntimeException {
	public final Handler.Response response;

	public IncorrectRequest(int code) {
		this.response = new Handler.Response(code);
	}

	public IncorrectRequest(int code, String message) {
		this.response = new Handler.Response(code, message);
	}
}
