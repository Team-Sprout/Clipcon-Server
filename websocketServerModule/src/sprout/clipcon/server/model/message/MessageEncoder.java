package sprout.clipcon.server.model.message;

/**
 * 클라이언트로 보낼 object(Message)를 string으로 encoding. */
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.json.JSONObject;

public class MessageEncoder implements Encoder.Text<Message> {
	private JSONObject tmp;

	public void destroy() {
	}

	public void init(EndpointConfig arg0) {
		tmp = new JSONObject();
	}

	public String encode(Message message) throws EncodeException {
		return message.getJson().toString();
	}
}
