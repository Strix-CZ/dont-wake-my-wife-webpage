package online.temer.alarm.dto;

public class UserDto
{
	public final Long id;
	public final String email;

	public UserDto(String email)
	{
		this.id = null;
		this.email = email;
	}

	public UserDto(long id, String email)
	{
		this.id = id;
		this.email = email;
	}
}
