package online.temer.alarm.test.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtil
{
	public static HttpResponse<String> makeGetRequest(URI uri)
	{
		try
		{
			var request = HttpRequest.newBuilder(uri).build();

			return HttpClient.newHttpClient()
					.send(request, HttpResponse.BodyHandlers.ofString());
		}
		catch (InterruptedException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
