package online.temer.alarm.server;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

class QueryParameterReaderTest
{

	private QueryParameterReader reader;

	@Test
	void readsEmptyString()
	{
		setParameters("s", "");
		Assertions.assertThat(reader.readString("s")).isEqualTo("");
	}

	@Test
	void readsString()
	{
		setParameters("s", "foo");
		Assertions.assertThat(reader.readString("s")).isEqualTo("foo");
	}

	@Test
	void incorrectInt_throws()
	{
		setParameters("i", "");
		Assertions.assertThatThrownBy(() -> reader.readInt("i")).isInstanceOf(IncorrectParameter.class);

		setParameters("i", "-1000000000000");
		Assertions.assertThatThrownBy(() -> reader.readInt("i")).isInstanceOf(IncorrectParameter.class);

		setParameters("i", "0.1");
		Assertions.assertThatThrownBy(() -> reader.readInt("i")).isInstanceOf(IncorrectParameter.class);

		setParameters("i", "-");
		Assertions.assertThatThrownBy(() -> reader.readInt("i")).isInstanceOf(IncorrectParameter.class);

		setParameters("i", "a");
		Assertions.assertThatThrownBy(() -> reader.readInt("i")).isInstanceOf(IncorrectParameter.class);
	}

	@Test
	void correctInt_reads()
	{
		setParameters("i", "10");
		Assertions.assertThat(reader.readInt("i")).isEqualTo(10);

		setParameters("i", "020");
		Assertions.assertThat(reader.readInt("i")).isEqualTo(20);
	}

	@Test
	void incorrectLong_throws()
	{
		setParameters("i", "");
		Assertions.assertThatThrownBy(() -> reader.readLong("i")).isInstanceOf(IncorrectParameter.class);

		setParameters("i", "0.1");
		Assertions.assertThatThrownBy(() -> reader.readLong("i")).isInstanceOf(IncorrectParameter.class);
	}

	@Test
	void correcLong_reads()
	{
		setParameters("i", "-1000000000000");
		Assertions.assertThat(reader.readLong("i")).isEqualTo(-1000000000000L);
	}

	@Test
	void correctTime_reads()
	{
		setParameters("t", "1900-02-27T20:04:06");
		Assertions.assertThat(reader.readTime("t", TimeZone.getDefault()))
				.isEqualTo(ZonedDateTime.of(1900, 2, 27, 20, 4, 6, 0, ZoneId.systemDefault()));
	}

	@Test
	void incorrectTime_throws()
	{
		setParameters("t", "1900-02-29T20:04:06");
		Assertions.assertThatThrownBy(() -> reader.readTime("t", TimeZone.getDefault())).isInstanceOf(IncorrectParameter.class);

		setParameters("t", "0");
		Assertions.assertThatThrownBy(() -> reader.readTime("t", TimeZone.getDefault())).isInstanceOf(IncorrectParameter.class);

		setParameters("t", "");
		Assertions.assertThatThrownBy(() -> reader.readTime("t", TimeZone.getDefault())).isInstanceOf(IncorrectParameter.class);
	}

	private void setParameters(String... keysAndValues)
	{
		reader = new QueryParameterReader(keysAndValues);
	}
}