package sprout.clipcon.server.controller;

import java.io.IOException;
import java.util.Map;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import lombok.Getter;
import sprout.clipcon.server.model.AddressBook;
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

		System.out.println("[Server] message received success. type: " + type);

		switch (type) {
		case "sign in":
			String email = incomingMessage.get(Message.EMAIL);
			String name = incomingMessage.get("password");
			String result = MemberAdministrator.getUserAuthentication(email, name);
			if (result.equals(MemberAdministrator.CONFIRM)) { // 서버: 승인
				System.out.println("if in");
				server = Server.getInstance();	// 서버 객체 할당
				user = MemberAdministrator.getUserByEmail("사용자 이메일");
				server.enterUserInLobby(user);			// 서버에 사용자 입장
				
				User tmpUser = testCreateTmpUser();
				Message sendMsg = new Message().setType(Message.RESPONSE_SIGN_IN);
				MessageParser.AddUserInfoToMessage(sendMsg, tmpUser);
				sendMsg.add("result", result);
				System.out.println("delf: " + sendMsg);
				session.getBasicRemote().sendObject(sendMsg);
				
			} else { // 서버: 거부
				System.out.println("거부됨");
			}

			break;		

		case Message.REQUEST_SIGN_UP:
			break;

		case Message.REQUEST_CREATE_GROUP:
			group = server.createGroup("그룹 이름");	// 해당 이름으로 그룹 생성
			group.addUser(user.getEmail(), this);		// 그 그룹에 참여
			break;

		case Message.REQUEST_JOIN_GROUP:
			group = server.getGroupByPrimaryKey("그룹고유키");
			group.addUser(user.getEmail(), this);
			break;

		case Message.REQUEST_GET_ADDRESSBOOK:
			break;

		case Message.TEST_DEBUG_MODE:
			server = Server.getInstance();	// 서버 객체 할당
			// server.testGroup.addUser(incomingMessage.get(Message.EMAIL), session);

			break;

		default:
			// 지금은 지정되지 않은 type의 요청이 오면 그 메시지를 그대로 돌려줌.
			System.out.println("예외사항");
			userSession.getBasicRemote().sendObject(incomingMessage);
			break;
		}
	}

	@OnClose
	public void handleClose(Session userSession) {
		if (userSession == null) {
			System.out.println("null");
		}
		// group.getUsers().remove(userSession);
	}

	@OnError
	public void handleError(Throwable t) {
		System.out.println("나감");
	}

	private User testCreateTmpUser() {
		User user = new User("test", "12");
		
		AddressBook ab = user.getAddressBook();
		Map<String, String> users = ab.getUsers();
		users.put("em1", "n1");
		users.put("em2", "n2");
		users.put("em3", "n3");
		users.put("em5", "n5");
		
		return user;
	}
}