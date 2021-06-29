package online.temer.alarm.test.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HttpUtil
{
	public static HttpResponse<String> makeGetRequest(URI uri)
	{
		var request = HttpRequest.newBuilder(uri).build();

		return makeRequest(request);
	}

	public static HttpResponse<String> makePostResquest(URI uri, String body)
	{
		var request = HttpRequest.newBuilder(uri)
				.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
				.build();

		return makeRequest(request);
	}

	private static HttpResponse<String> makeRequest(HttpRequest request)
	{
		try
		{
			return HttpClient.newHttpClient()
					.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		}
		catch (InterruptedException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
