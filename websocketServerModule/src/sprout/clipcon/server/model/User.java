package sprout.clipcon.server.model;

import javax.websocket.Session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
	private String email;
	private String name;
	private Group group;
	private String status;
	private AddressBook addressBook = new AddressBook();
	private Session session;

	public User(String email, String name) {
		this.email = email;
		this.name = name;
	}
}