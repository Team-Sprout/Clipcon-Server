package sprout.clipcon.server.model.message;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.json.JSONObject;

/**
 * 클라이언트에서 받은 string을 object(Message)로 decoding. */
public class MessageDecoder implements Decoder.Text<Message> {
	JSONObject json;

	public void destroy() {
	}

	public void init(EndpointConfig arg0) {
	}

	public Message decode(String incommingMessage) throws DecodeException {
		System.out.println("=============== 서버는 받은 string 확인 ===============\n" + incommingMessage + "\n---------------------------------------------------");
		Message message = new Message().setJson(incommingMessage);
		// message.setJson(incommingMessage);
		
		return message;
	}

	public boolean willDecode(String message) {
		boolean flag = true;
		try {
		} catch (Exception e) {
			System.out.println("false!");
			flag = false;
		}
		return flag;
	}
}
