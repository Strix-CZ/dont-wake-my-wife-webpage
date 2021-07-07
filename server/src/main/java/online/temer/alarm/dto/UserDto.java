package online.temer.alarm.dto;

import java.util.Objects;

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

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserDto userDto = (UserDto) o;
		return Objects.equals(id, userDto.id) && email.equals(userDto.email) && hash.equals(userDto.hash) && salt.equals(userDto.salt);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, email, hash, salt);
	}
}
