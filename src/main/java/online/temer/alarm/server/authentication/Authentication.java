package online.temer.alarm.server.authentication;

import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.util.Optional;

public interface Authentication<T>
{
	Result<T> authenticate(Connection connection, QueryParameterReader parameterReader, Request request, Response response);

	class Result<T>
	{
		public final Optional<T> entity;
		public final Handler.Response errorResponse;

		public Result(T entity)
		{
			this.entity = Optional.of(entity);
			this.errorResponse = null;
		}

		public Result(Handler.Response errorResponse)
		{
			this.entity = Optional.empty();
			this.errorResponse = errorResponse;
		}

		public Optional<T> getEntity()
		{
			return entity;
		}

		public Handler.Response getErrorResponse()
		{
			return errorResponse;
		}
	}
}
