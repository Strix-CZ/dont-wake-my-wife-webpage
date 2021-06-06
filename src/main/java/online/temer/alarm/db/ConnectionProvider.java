package online.temer.alarm.db;

import java.sql.Connection;

public interface ConnectionProvider
{
	Connection get();
}
