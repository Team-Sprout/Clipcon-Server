package sprout.clipcon.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class User {
	private String email;
	private String name;
	private String status;
}
