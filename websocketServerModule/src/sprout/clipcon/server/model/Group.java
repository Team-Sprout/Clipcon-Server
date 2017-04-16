package sprout.clipcon.server.model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jws.soap.SOAPBinding.Use;
import javax.websocket.EncodeException;
import javax.websocket.Session;

import lombok.Getter;
import lombok.Setter;
import sprout.clipcon.server.controller.Server;
import sprout.clipcon.server.controller.UserController;
import sprout.clipcon.server.model.message.Message;

@Getter
@Setter
public class Group {
	private String primaryKey;
	private String name;
	private Server server = Server.getInstance();
	public Map<User, UserController> users = Collections.synchronizedMap(new HashMap<User, UserController>());

	public Group(String primaryKey, String name) {
		this.primaryKey = primaryKey;
		this.name = name;
	}

	public void send(Message message) throws IOException, EncodeException {
		for (User key : users.keySet()) {
			System.out.println(key);
			users.get(key).getSession().getBasicRemote().sendObject(message);
		}
	}
	
	public boolean addUser(User user, UserController session) {
		users.put(user, session);
		return true;
	}
}
