package sprout.clipcon.server.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.websocket.EncodeException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sprout.clipcon.server.controller.UserController;
import sprout.clipcon.server.model.message.Message;

@Getter
@Setter
@NoArgsConstructor
public class Group {
	private String primaryKey;
	private String name;
	public Map<String, UserController> users = Collections.synchronizedMap(new HashMap<String, UserController>());

	public Group(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void send(Message message) throws IOException, EncodeException {
		for (String key : users.keySet()) {
			System.out.println(key);
			users.get(key).getSession().getBasicRemote().sendObject(message);
		}
	}

	public String addUser(String name, UserController session) {
		String tmpName = getTmepUsername();
		users.put(tmpName, session);
		System.out.println("새 유저가 그룹에 입장");
		return tmpName;
	}

	public User getUserByEmail(String email) {
		return users.get(email).getUser();
	}

	public List<String> getUserList() {
		List<String> list = new ArrayList<String>();
		for (String key : users.keySet()) {
			list.add(key);
		}
		return list;
	}
	
	public String getTmepUsername() {
		StringBuffer temp = new StringBuffer();
		Random rnd = new Random();
		for (int i = 0; i < 6; i++) {
			int rIndex = rnd.nextInt(1);
			switch (rIndex) {
			case 0:
				// a-z
				temp.append((char) ((int) (rnd.nextInt(26)) + 97));
				break;
			case 1:
				// 0-9
				temp.append((rnd.nextInt(10)));
				break;
			}
		}
		return temp.toString();
	}
	
	public int getSize() {
		return users.size();
	}
}
