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
import lombok.Setter;
import sprout.clipcon.server.model.Group;
import sprout.clipcon.server.model.message.Message;
import sprout.clipcon.server.model.message.MessageDecoder;
import sprout.clipcon.server.model.message.MessageEncoder;
import sprout.clipcon.server.model.message.MessageParser;

@Getter
@ServerEndpoint(value = "/ServerEndpoint", encoders = { MessageEncoder.class }, decoders = { MessageDecoder.class })
public class UserController {
	private Server server; // 서버
	private Group group; // 참여 중인 그룹
	// private User user; // user 정보
	private Session session;

	@Setter
	private String userName;

	// change source
	@OnOpen
	public void handleOpen(Session userSession) {
		this.session = userSession;
		System.out.println("session is opened");
	}

	@OnMessage
	public void handleMessage(Message incomingMessage, Session userSession) throws IOException, EncodeException {
		System.out.println("[UserController] message from client: " + incomingMessage.toString());
		String type = incomingMessage.getType(); // 받은 메시지의 타입 추출

		if (session != userSession) { // for test
			System.out.println("이런상황이 발생할 수 있을까");
			return;
		}
		this.session = userSession; // Session assign
		System.out.println("[UserController] message received success. type: " + type); // check message type

		Message responseMsg = null; // Initialize message to send to client

		switch (type) {
		/* Request Type: Create Group */
		case Message.REQUEST_CREATE_GROUP:

			server = Server.getInstance(); // get Server's instance
			group = server.createGroup(); // get Group in Server and add The instance of group that this object belongs to
			userName = group.addUser(session.getId(), this); // add yourself to the group, get the user's name / XXX 수정 필요

			responseMsg = new Message().setType(Message.RESPONSE_CREATE_GROUP); // create response message: Create Group
			responseMsg.add(Message.RESULT, Message.CONFIRM); // add response result
			responseMsg.add(Message.NAME, userName); // add user name
			MessageParser.addMessageToGroup(responseMsg, group); // add group info
			break;

		/* Request Type: Join Group */
		case Message.REQUEST_JOIN_GROUP:

			server = Server.getInstance();
			group = server.getGroupByPrimaryKey(incomingMessage.get(Message.GROUP_PK)); // get the "object corresponding to the requested group key" on the server

			responseMsg = new Message().setType(Message.RESPONSE_JOIN_GROUP); // create response message: Join Group

			// 해당 그룹키에 매핑되는 그룹이 존재 시,
			if (group != null) {
				userName = group.addUser(session.getId(), this);

				responseMsg.add(Message.RESULT, Message.CONFIRM);
				responseMsg.add(Message.NAME, userName);
				MessageParser.addMessageToGroup(responseMsg, group);

				Message noti = new Message().setType(Message.NOTI_ADD_PARTICIPANT); // create notification message: participant's info
				noti.add(Message.PARTICIPANT_NAME, userName); // add participant's info
				group.sendWithout(userName, noti);
			}
			// 해당 그룹키에 매핑되는 그룹이 존재하지 않을 시,
			else {
				responseMsg.add(Message.RESULT, Message.REJECT); // add response result
			}
			break;

		/* Request Type: Exit Group */
		case Message.REQUEST_EXIT_GROUP:

			server = Server.getInstance();
			group = server.getGroupByPrimaryKey(incomingMessage.get(Message.GROUP_PK)); // 서버에서 "요청한 그룹키에 해당하는 객체"를 가져옴

			responseMsg = new Message().setType(Message.RESPONSE_EXIT_GROUP); // 응답 메세지 생성, 응답 타입인 "그룹 참가 요청에 대한 응답"

			if (group != null) { // 해당 그룹키에 매핑되는 그룹이 존재 시,
				userName = incomingMessage.get(Message.NAME);
				group.removeUser(userName); // 그룹에 자신을 삭제

				responseMsg.add(Message.RESULT, Message.CONFIRM); // 응답 메시지에 내용 추가: 응답 결과
				responseMsg.add(Message.NAME, userName); // 응답 메시지에 내용 추가: 사용자 이름
				MessageParser.addMessageToGroup(responseMsg, group); // 응답 메시지에 내용 추가: 그룹 정보

				Message noti = new Message().setType(Message.NOTI_EXIT_PARTICIPANT); // 알림 메시지 생성, 알림 타입은 "나간 사용자에 대한 정보"
				noti.add(Message.PARTICIPANT_NAME, userName); // 알림 메시지에 내용 추가: 참가자 정보
				boolean whetherToDestroy = group.sendAll(noti); // 그룹원 모두에게 나간 사용자에 대한 정보 전송

				// 그룹을 파기한다.(서버 그룹 목록에서 삭제, 그룹 폴더 및 하위 파일들 삭제)
				if (whetherToDestroy == true) {
					server.destroyGroup(group.getPrimaryKey());
				}

			} else { // 해당 그룹키에 매핑되는 그룹이 존재하지 않을 시,
				responseMsg.add(Message.RESULT, Message.REJECT); // add response result
			}
			break;

		/* Request Type: Change Nickname */
		case Message.REQUEST_CHANGE_NAME:

			responseMsg = new Message().setType(Message.RESPONSE_CHANGE_NAME); // create response message: Change Nickname

			String originName = userName; // The user's origin name
			String changeUserName = incomingMessage.get(Message.CHANGE_NAME); // The user's new name

			group.changeUserName(userName, changeUserName); // Change User Nickname

			responseMsg.add(Message.RESULT, Message.CONFIRM);
			responseMsg.add(Message.CHANGE_NAME, userName); // add new nickname

			Message noti = new Message().setType(Message.NOTI_CHANGE_NAME); // create notification message: user's info who request changing name
			noti.add(Message.NAME, originName); // add user's origin name
			noti.add(Message.CHANGE_NAME, changeUserName); // add user's new name
			group.sendWithout(originName, noti);

			break;

		default:
			responseMsg = new Message().setType(Message.TEST_DEBUG_MODE);
			System.out.println("예외사항");
			break;
		}
		sendMessage(session, responseMsg); // 전송
	}

	@OnClose
	public void handleClose(Session userSession) {
		System.out.println("나감");
		if (userSession == null) {
			System.out.println("세션이 null");
		}

		Message noti = new Message().setType(Message.NOTI_EXIT_PARTICIPANT); // create notification message: Outgoing user's info
		noti.add(Message.PARTICIPANT_NAME, userName); // add Outgoing user's info
		try {
			group.sendWithout(userName, noti);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (EncodeException e) {
			e.printStackTrace();
		}
		group.removeUser(userName);
	}

	@OnError
	public void handleError(Throwable t) {
		System.out.println("오류 발생");
		t.printStackTrace();
	}

	private void sendMessage(Session session, Message message) throws IOException, EncodeException {
		System.out.println("[UserController] message to client: " + message);
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

	// test main method
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