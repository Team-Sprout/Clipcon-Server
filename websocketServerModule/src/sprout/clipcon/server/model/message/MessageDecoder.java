package sprout.clipcon.server.model.message;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.json.JSONObject;

/**
 * Decode string received from client as object (Message)
 */
public class MessageDecoder implements Decoder.Text<Message> {
	JSONObject json;

	public void destroy() {
	}

	public void init(EndpointConfig arg0) {
	}

	public Message decode(String incomingMessage) throws DecodeException {
		System.out.println("=============== Check the received string from client ===============\n" + incomingMessage
				+ "\n---------------------------------------------------");
		Message message = new Message().setJson(incomingMessage);
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
