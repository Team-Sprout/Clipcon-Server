package sprout.clipcon.server.controller;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import lombok.Getter;
import sprout.clipcon.server.model.Group;
import sprout.clipcon.server.model.User;
import sprout.clipcon.server.model.message.ChatMessageDecoder;
import sprout.clipcon.server.model.message.ChatMessageEncoder;
import sprout.clipcon.server.model.message.Message;

@Getter
@ServerEndpoint(value = "/ServerEndpoint", encoders = { ChatMessageEncoder.class }, decoders = { ChatMessageDecoder.class })
public class UserController {
	private Server server;	// 서버
	private Group group;	// 참여 중인 그룹
	private User user;		// user 정보
	private Session session;
	private String userName;

	@OnOpen
	public void handleOpen(Session userSession) {
		this.session = userSession;
	}

	@OnMessage
	public void handleMessage(Message incomingMessage, Session userSession) throws IOException, EncodeException {
		String type = incomingMessage.getType();

		if (session != userSession) { // for test
			System.out.println("이런상황이 발생할 수 있을까");
			return;
		}
		this.session = userSession;

		System.out.println("[Server] message received success. type: " + type);

		Message responseMsg = null;
		switch (type) {

		case Message.REQUEST_CREATE_GROUP:
			server = Server.getInstance();			// 서버 객체 할당
			group = server.createGroup();	// 해당 이름으로 그룹 생성

			responseMsg = new Message().setType(Message.RESPONSE_CREATE_GROUP);
			userName = group.addUser(session.getId(), this);
			responseMsg.add(Message.NAME, userName);
			responseMsg.add(Message.RESULT, Message.CONFIRM);
			MessageParser.getMessageByGroup(responseMsg, group);// 그룹 정보를 포함하는 Message 객체 생성
			break;

		case Message.REQUEST_JOIN_GROUP:
			server = Server.getInstance();
			group = server.getGroupByPrimaryKey("godoy");	 // 그룹키에 해당하는 객체 추출
			// group = server.getGroupByPrimaryKey(incomingMessage.get("groupPK")); // 그룹키에 해당하는 객체 추출
			responseMsg = new Message().setType(Message.RESPONSE_JOIN_GROUP);

			if (group != null) {
				responseMsg.add(Message.RESULT, Message.CONFIRM);
				userName = group.addUser(session.getId(), this);
				responseMsg.add(Message.NAME, userName);
				MessageParser.getMessageByGroup(responseMsg, group);// 그룹 정보를 포함하는 Message 객체 생성
			} else {
				responseMsg.add(Message.RESULT, Message.REJECT);
			}
			break;

		case Message.TEST_DEBUG_MODE:
			server = Server.getInstance();	// 서버 객체 할당
			// server.testGroup.addUser(incomingMessage.get(Message.EMAIL), session);

			break;

		default:
			// 지금은 지정되지 않은 type의 요청이 오면 그 메시지를 그대로 돌려줌.
			System.out.println("예외사항");
			break;
		}
		System.out.println("===== 클라이언트에게 보낸 메시지 =====\n" + responseMsg + "\n============================");
		sendMessage(session, responseMsg);					 // 전송
	}

	@OnClose
	public void handleClose(Session userSession) {
		System.out.println("나감");
		if (userSession == null) {
			System.out.println("세션이 null");
		}
		// group.getUsers().remove(userSession);
	}

	@OnError
	public void handleError(Throwable t) {
		System.out.println("오류 발생");
		t.printStackTrace();
	}

	private void sendMessage(Session session, Message message) throws IOException, EncodeException {
		session.getBasicRemote().sendObject(message);
	}

	// for test and debugging
	private String createTmpGroup() {
		Group group = server.createGroup();
		if (group == null) {
			System.out.println("그룹이 만들어지지 않음");
		}
		return group.getPrimaryKey();
	}

	public static void main(String[] args) {
		Message tmpMessage = new Message().setJson("{\"group pk\":\"godoy\",\"message type\":\"request/join group\"}");
		try {
			new UserController().handleMessage(tmpMessage, null);
		} catch (Exception e) {
			System.out.println("예외 무시");
			e.printStackTrace();
		}
	}
}