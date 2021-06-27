package online.temer.alarm.server.ui;

import online.temer.alarm.server.ServerTestExtension;
import online.temer.alarm.test.util.HttpUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.URISyntaxException;

@ExtendWith(ServerTestExtension.class)
class AlarmHandlerTest
{
	@Test
	void whenQueried_returns200() throws URISyntaxException
	{
		var response = HttpUtil.makeGetRequest(new URI("http://localhost:8765/alarm"));
		Assertions.assertThat(response.statusCode())
				.as("status code")
				.isEqualTo(200);
	}
}