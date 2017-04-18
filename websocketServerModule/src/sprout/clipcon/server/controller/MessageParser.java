package sprout.clipcon.server.controller;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import sprout.clipcon.server.model.AddressBook;
import sprout.clipcon.server.model.Group;
import sprout.clipcon.server.model.User;
import sprout.clipcon.server.model.message.Message;

public class MessageParser {

	/**
	 * @param message 서버에서 받은 Message객체
	 * @return message 로부터 변환된 User객체 */
	public static User getUserByMessage(Message message) {
		User user = new User(); // 반환할 객체
		user.setEmail(message.get(Message.EMAIL));	// User객체에 email 삽입
		user.setName(message.get(Message.NAME));	// User객체에 name 삽입

		// user에 삽입할 AddressBook객체 생성
		AddressBook addressBook = new AddressBook();
		Map<String, String> users = addressBook.getUsers();

		// message에서 JSONObject추출
		JSONObject jsonMsg = message.getJson();
		JSONArray array = jsonMsg.getJSONArray(Message.LIST);

		Iterator<?> it = array.iterator();
		while (it.hasNext()) {
			JSONObject tmpJson = (JSONObject) it.next();
			String email = tmpJson.getString(Message.EMAIL);
			String name = tmpJson.getString(Message.NAME);
			users.put(email, name);
		}
		user.setAddressBook(addressBook);
		return user;
	}

	public static AddressBook getAddressBookByMessage(Message message) {
		AddressBook addressBook = new AddressBook();
		Map<String, String> users = addressBook.getUsers();
		JSONObject jsonMsg = message.getJson();
		JSONArray array = jsonMsg.getJSONArray(Message.LIST);

		Iterator<?> it = array.iterator();
		while (it.hasNext()) {
			JSONObject tmpJson = (JSONObject) it.next();
			String email = tmpJson.getString(Message.EMAIL);
			String name = tmpJson.getString(Message.NAME);
			// User tmpUser = new User(email, tmpJson.getString(Message.NAME));
			users.put(email, name);
		}

		for (String key : users.keySet()) {
			System.out.println(key + " " + users.get(key));
		}

		return addressBook;
	}

	public static Message getMeessageByAddressBook(AddressBook addressBook) {
		Map<String, String> users = addressBook.getUsers();
		Message message = new Message().setType(Message.ADDRESS_BOOK);

		JSONArray array = new JSONArray();
		for (String key : users.keySet()) {
			JSONObject tmp = new JSONObject();
			tmp.put(Message.EMAIL, users.get(key));
			tmp.put(Message.NAME, users.get(key));
			array.put(tmp);
		}
		message.getJson().put(Message.LIST, array);
		return message;
	}

	public static Message getMessageByUser(User user) {
		Message message = new Message().setType(Message.USER_INFO); // 반환할 객체, 타입은 '유저정보'

		message.add(Message.EMAIL, user.getEmail());	// email 삽입
		message.add(Message.NAME, user.getName());		// name 삽입

		// Json으로 변환할 주소록 Map
		Map<String, String> users = user.getAddressBook().getUsers();
		// 주소록 내용 담을 JsonArray
		JSONArray array = new JSONArray();
		// array에 주소록 내용 삽입
		for (String key : users.keySet()) {
			JSONObject tmp = new JSONObject();
			tmp.put(Message.EMAIL, users.get(key));
			tmp.put(Message.NAME, users.get(key));
			array.put(tmp);
		}
		// array를 message에 삽입
		message.getJson().put(Message.LIST, array);

		return message;
	}

	public static Message AddUserInfoToMessage(Message message, User user) {
		message.add(Message.EMAIL, user.getEmail());	// email 삽입
		message.add(Message.NAME, user.getName());		// name 삽입

		// Json으로 변환할 주소록 Map
		Map<String, String> users = user.getAddressBook().getUsers();
		// 주소록 내용 담을 JsonArray
		JSONArray array = new JSONArray();
		// array에 주소록 내용 삽입
		for (String key : users.keySet()) {
			System.out.println("만드는 중");
			JSONObject tmp = new JSONObject();
			tmp.put(Message.EMAIL, users.get(key));
			tmp.put(Message.NAME, users.get(key));
			array.put(tmp);
		}
		// array를 message에 삽입
		message.getJson().put(Message.LIST, array);
		System.out.println(message);
		return message;
	}

	public static Message getMessageByGroup(Group group) {
		Message message = new Message().setType(Message.GROUP_INFO);
		message.add("groupkey", group.getPrimaryKey());
		message.add("groupname", group.getName());
		message.add("list", group.getUserList());

		return message;
	}

	// public static Group getGroupByMessage(Message message) {
	// Group group = new Group();
	// String key = message.get("groupkey");
	// String name = message.get("groupname");
	// group.setPrimaryKey(key);
	// group.setName(name);
	//
	// group.setUsers(message.getObject("list"));
	// return group;
	// }

	public static void main(String[] args) {

		AddressBook ab = new AddressBook();
		Map<String, String> users = ab.getUsers();

		users.put("em1", "n1");
		users.put("em2", "n2");
		users.put("em3", "n3");
		users.put("em5", "n5");

		User tmp = new User("abab", "1212");
		tmp.setAddressBook(ab);
		Message tmpMessage = getMessageByUser(tmp);
		User tmpUser = getUserByMessage(tmpMessage);
		System.out.println(tmpUser.getName());
		System.out.println(tmpUser.getEmail());

		System.out.println("주소록 출력");
		Map<String, String> tmpMap = tmpUser.getAddressBook().getUsers();
		for (String key : tmpMap.keySet()) {
			System.out.println(key + " " + tmpMap.get(key));
		}
	}

}
