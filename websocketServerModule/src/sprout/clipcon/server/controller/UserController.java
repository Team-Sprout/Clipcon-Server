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
	private String userName;

	// change source
	@OnOpen
	public void handleOpen(Session userSession) {
		this.session = userSession;
		System.out.println("session is opened");
	}

	@OnMessage
	public void handleMessage(Message incomingMessage, Session userSession) throws IOException, EncodeException {
		System.out.println("[SERVER] message from client: " + incomingMessage.toString());
		String type = incomingMessage.getType(); // 받은 메시지의 타입 추출

		if (session != userSession) { // for test
			System.out.println("이런상황이 발생할 수 있을까");
			return;
		   }
		this.session = userSession; // 세션 assign

		System.out.println("[SERVER] message received success. type: " + type); // 메시지 타입 확인

		Message responseMsg = null; // 클라이언트에게 보낼 메시지 초기화

		switch (type) {

		case Message.REQUEST_CREATE_GROUP: /* 요청 타입: 그룹 생성 */

			server = Server.getInstance(); // "서버"의 instance 추가
			group = server.createGroup(); // 서버에 그룹을 추가하고 "이 객체가 소속된 그룹"의 instance 추가
			userName = group.addUser(session.getId(), this); // 그룹에 자신을 추가, 사용자의 이름을 받아옴 / XXX 수정 필요

			responseMsg = new Message().setType(Message.RESPONSE_CREATE_GROUP); // 응답 메시지 생성, 응답 타입은 "그룹 생성 요청에 대한 응답"
			responseMsg.add(Message.RESULT, Message.CONFIRM); // 응답 메시지에 내용 추가: 응답 결과
			responseMsg.add(Message.NAME, userName); // 응답 메시지에 내용 추가: 사용자 이름
			MessageParser.addMessageToGroup(responseMsg, group); // 응답 메시지에 내용 추가: 그룹 정보

		break;

		case Message.REQUEST_JOIN_GROUP: /* 요청 타입: 그룹 참가 */

			server = Server.getInstance();
			group = server.getGroupByPrimaryKey(incomingMessage.get(Message.GROUP_PK)); // 서버에서 "요청한 그룹키에 해당하는 객체"를 가져옴

			responseMsg = new Message().setType(Message.RESPONSE_JOIN_GROUP); // 응답 메세지 생성, 응답 타입인 "그룹 참가 요청에 대한 응답"
			if (group != null) { // 해당 그룹키에 매핑되는 그룹이 존재 시,
				userName = group.addUser(session.getId(), this); // 그룹에 자신을 추가, 사용자의 이름을 받아옴 / XXX 수정 필요
				responseMsg.add(Message.RESULT, Message.CONFIRM); // 응답 메시지에 내용 추가: 응답 결과
				responseMsg.add(Message.NAME, userName); // 응답 메시지에 내용 추가: 사용자 이름
				MessageParser.addMessageToGroup(responseMsg, group); // 응답 메시지에 내용 추가: 그룹 정보

				Message noti = new Message().setType(Message.NOTI_ADD_PARTICIPANT); // 알림 메시지 생성, 알림 타입은 "참가자에 대한 정보"
				noti.add(Message.PARTICIPANT_NAME, userName); // 알림 메시지에 내용 추가: 참가자 정보
				group.sendWithout(userName, noti);

			} else { // 해당 그룹키에 매핑되는 그룹이 존재하지 않을 시,
				responseMsg.add(Message.RESULT, Message.REJECT); // 응답 메시지에 내용 추가: 응답 결과
			}
		break;

		case Message.REQUEST_EXIT_GROUP: /* 요청 타입: 그룹 나가기 */
			responseMsg = new Message().setType(Message.RESPONSE_EXIT_GROUP);
			exitUserAtGroup();
			
//			server = Server.getInstance();
//			group = server.getGroupByPrimaryKey(incomingMessage.get(Message.GROUP_PK)); // 서버에서 "요청한 그룹키에 해당하는 객체"를 가져옴
//
//			responseMsg = new Message().setType(Message.RESPONSE_EXIT_GROUP); // 응답 메세지 생성, 응답 타입인 "그룹 참가 요청에 대한 응답"
//			if (group != null) { // 해당 그룹키에 매핑되는 그룹이 존재 시,
//				userName = incomingMessage.get(Message.NAME);
//				group.removeUser(userName); // 그룹에 자신을 삭제
//
//				responseMsg.add(Message.RESULT, Message.CONFIRM); // 응답 메시지에 내용 추가: 응답 결과
//				responseMsg.add(Message.NAME, userName); // 응답 메시지에 내용 추가: 사용자 이름
//				MessageParser.addMessageToGroup(responseMsg, group); // 응답 메시지에 내용 추가: 그룹 정보
//
//				Message noti = new Message().setType(Message.NOTI_EXIT_PARTICIPANT); // 알림 메시지 생성, 알림 타입은 "나간 사용자에 대한 정보"
//				noti.add(Message.PARTICIPANT_NAME, userName); // 알림 메시지에 내용 추가: 참가자 정보
//				boolean whetherToDestroy = group.sendAll(noti); // 그룹원 모두에게 나간 사용자에 대한 정보 전송
//				
//				//그룹을 파기한다.(서버 그룹 목록에서 삭제, 그룹 폴더 및 하위 파일들 삭제)
//				if(whetherToDestroy == true){
//					server.destroyGroup(group.getPrimaryKey());
//				}
//
//			} else { // 해당 그룹키에 매핑되는 그룹이 존재하지 않을 시,
//				responseMsg.add(Message.RESULT, Message.REJECT); // 응답 메시지에 내용 추가: 응답 결과
//			}
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
		if (userSession == null) {
			System.out.println("Session is null");
		}
		System.err.println("[SERVER] Session is closed. User terminated the program.");
		exitUserAtGroup();
	}
	
	private void exitUserAtGroup() {
		Message noti = new Message().setType(Message.NOTI_EXIT_PARTICIPANT); // 알림 메시지 생성, 알림 타입은 "참가자에 대한 정보"
		noti.add(Message.PARTICIPANT_NAME, userName); // 알림 메시지에 내용 추가: 참가자 정보
		try {
			group.sendWithout(userName, noti);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (EncodeException e) {
			e.printStackTrace();
		}
		group.removeUser(userName);
		
		if(group.getSize() == 0) {
			Server.getInstance().removeGroup(group);
		}
		System.out.println("[SERVER] User leaves the group.");
	}

	@OnError
	public void handleError(Throwable t) {
		System.err.println("[SERVER] Error was occured.");
		t.printStackTrace();
	}

	private void sendMessage(Session session, Message message) throws IOException, EncodeException {
		System.out.println("[SERVER] meesage to client: " + message);
		session.getBasicRemote().sendObject(message);
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