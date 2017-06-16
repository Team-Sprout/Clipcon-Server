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

public class Group {
	@Getter
	private String primaryKey;
	private Map<String, UserController> users = Collections.synchronizedMap(new HashMap<String, UserController>());
	private History history;

	public Group(String primaryKey) {
		this.primaryKey = primaryKey;
		this.history = new History(primaryKey);
	}

	public void sendWithout(String user, Message message) throws IOException, EncodeException {
		System.out.println("[Group] send message to all users of group except \"" + user + "\" : " + message.toString());
		for (String key : users.keySet()) {
			if (key.equals(user)) // except
				continue;
			users.get(key).getSession().getBasicRemote().sendObject(message);
		}
	}

	public boolean sendAll(Message message) throws IOException, EncodeException {
		System.out.println("[Group] send message to all user of group: " + message.toString());

		if (users.size() == 0) {
			return true;
		}

		for (String key : users.keySet()) {
			users.get(key).getSession().getBasicRemote().sendObject(message);
		}
		return false;
	}

	public String addUser(String name, UserController session) {
		String tmpName = getTempUsername();
		users.put(tmpName, session);
		System.out.println("[Group] new user take part in group: " + primaryKey + ":" + tmpName);
		return tmpName;
	}

	public List<String> getUserList() {
		List<String> list = new ArrayList<String>();
		for (String key : users.keySet()) {
			list.add(key);
		}
		return list;
	}

	public void addContents(Contents contents) {
		history.addContents(contents);
	}

	public Contents getContents(String key) {
		return history.getContentsByPK(key);
	}

	public int getSize() {
		return users.size();
	}

	public String getTempUsername() {
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

	public void removeUser(String userName) {
		users.remove(userName);
	}

	/** change user name
	 * @param userName - user's origin name
	 * @param changeUserName - the name that user want to change */
	public void changeUserName(String userName, String changeUserName) {

		// // 아래 희정이코드
		// UserController newUserController = users.get(userName); // assign new newUserController
		// newUserController.setUserName(changeUserName); // set changeUserName to newUserController
		//
		// removeUser(userName); // delete origin user who request change nickname
		// users.put(changeUserName, newUserController); // add new user that key name is changeUserName
		// 여기까지 희정 코드

	}
}
