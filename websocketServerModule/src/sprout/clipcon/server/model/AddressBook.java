package sprout.clipcon.server.model;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

import lombok.Getter;
import sprout.clipcon.server.controller.Server;
import sprout.clipcon.server.model.message.Message;

@Getter
public class AddressBook {
	private Map<String, String> users = new HashMap<String, String>();
	// private Server server = Server.getInstance();

	public Session getSessionByEmail(String email) {
		// return users.get(email).getSession(); // 폐기 코드
		// TODO: 로비에서 찾아서 세션 가져다주기
		return null;
	}

	public void addAddressByMessage(Message message) {
		String email = message.get(Message.EMAIL);
		users.put(message.get(Message.EMAIL), message.get(Message.NAME));
	}
	
	public void deleteAddress(Message message) {
		users.remove(message.get(Message.EMAIL));
	}
}
