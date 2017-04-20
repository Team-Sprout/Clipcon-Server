package sprout.clipcon.server.model.message;

import java.awt.List;

import org.json.JSONML;
import org.json.JSONObject;
import org.json.JSONWriter;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Message {
	private String type;
	private JSONObject json;

	public Message setJson(String string) {
		json = new JSONObject(string);
		type = json.getString(TYPE);
		return this;
	}

	public Message setJson(JSONObject json) {
		type = json.getString(TYPE);
		return this;
	}

	public Message setType(String type) {
		if (json == null)
			json = new JSONObject();
		this.type = type;
		json.put(TYPE, type);
		return this;
	}

	public Message(String type, String jsonString) {
		this.type = type;
		json = new JSONObject();
		json.put(TYPE, type);
		json.put(CONTENTS, jsonString);
		System.out.println(json.toString());
	}

	// public void add(String key, String value) {
	// json.put(key, value);
	// }

	public void add(String key, Object value) {
		json.put(key, value);
	}

	public String get(String key) {
		return json.get(key).toString();
	}

	public Object getObject(String key) {
		return json.get(key);
	}

	public String toString() {
		return json.toString();
	}

	public Message addObject(String key, Object object) {
		
		if (this.json == null)
			json = new JSONObject();
		json.put(key, object);
		return this;
	}

	public final static String TYPE = "message type";

	public final static String REQUEST_CREATE_GROUP = "request/create group";
	public final static String REQUEST_JOIN_GROUP = "request/join group";
	public final static String REQUEST_TEST = "request/test";

	public final static String RESPONSE_CREATE_GROUP = "response/create group";
	public final static String RESPONSE_JOIN_GROUP = "response/join group";

	public final static String RESULT = "result";
	public final static String CONFIRM = "confirm";
	public final static String REJECT = "reject";

	public final static String GROUP_PK = "group pk";

	public final static String EMAIL = "email";
	public final static String NAME = "name";
	public final static String CONTENTS = "contents";

	public final static String LIST = "list";
	public final static String USER_INFO = "user information";
	public final static String GROUP_INFO = "group information";
	public final static String TEST_DEBUG_MODE = "debug";
}
