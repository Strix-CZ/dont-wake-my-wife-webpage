package online.temer.alarm.dto;

public class UserDto
{
	public final Long id;
	public final String email;
	public final String hash;
	public final String salt;

	public UserDto(String email, String hash, String salt)
	{
		this.id = null;
		this.email = email;
		this.hash = hash;
		this.salt = salt;
	}

	public UserDto(long id, String email, String hash, String salt)
	{
		this.id = id;
		this.email = email;
		this.hash = hash;
		this.salt = salt;
	}
}
