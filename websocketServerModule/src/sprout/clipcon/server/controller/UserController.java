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

@ServerEndpoint(value = "/ServerEndpoint", encoders = { MessageEncoder.class }, decoders = { MessageDecoder.class })
public class UserController {
	private Server server; // �꽌踰�
	private Group group; // 李몄뿬 以묒씤 洹몃９
	// private User user; // user �젙蹂�
	@Getter
	private Session session;

	@Setter private String userName;

	// change source
	@OnOpen
	public void handleOpen(Session userSession) {
		this.session = userSession;
		System.out.println("session is opened");
	}

	@OnMessage
	public void handleMessage(Message incomingMessage, Session userSession) throws IOException, EncodeException {
		System.out.println("[UserController] message from client: " + incomingMessage.toString());
		String type = incomingMessage.getType(); // 諛쏆� 硫붿떆吏��쓽 ���엯 異붿텧

		if (session != userSession) { // for test
			System.out.println("�씠�윴�긽�솴�씠 諛쒖깮�븷 �닔 �엳�쓣源�");
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
			userName = group.addUser(session.getId(), this); // add yourself to the group, get the user's name / XXX �닔�젙 �븘�슂

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

			// �빐�떦 洹몃９�궎�뿉 留ㅽ븨�릺�뒗 洹몃９�씠 議댁옱 �떆,
			if (group != null) {
				userName = group.addUser(session.getId(), this);

				responseMsg.add(Message.RESULT, Message.CONFIRM);
				responseMsg.add(Message.NAME, userName);
				MessageParser.addMessageToGroup(responseMsg, group);

				Message noti = new Message().setType(Message.NOTI_ADD_PARTICIPANT); // create notification message: participant's info
				noti.add(Message.PARTICIPANT_NAME, userName); // add participant's info
				group.sendWithout(userName, noti);
			}
			// �빐�떦 洹몃９�궎�뿉 留ㅽ븨�릺�뒗 洹몃９�씠 議댁옱�븯吏� �븡�쓣 �떆,
			else {
				responseMsg.add(Message.RESULT, Message.REJECT); // add response result
			}
			break;

		/* Request Type: Exit Group */
		case Message.REQUEST_EXIT_GROUP:
			responseMsg = new Message().setType(Message.RESPONSE_EXIT_GROUP);
			exitUserAtGroup();
			break;
			
		/* Request Type: Change Nickname */
		case Message.REQUEST_CHANGE_NAME:
			responseMsg = new Message().setType(Message.RESPONSE_CHANGE_NAME); // create response message: Change Nickname

			String originName = userName; // The user's origin name
			String changeUserName = incomingMessage.get(Message.CHANGE_NAME); // The user's new name
			 
			System.out.println("originName: " + originName + ", changeUserName: " + changeUserName);
			
			group.changeUserName(userName, changeUserName); // Change User Nickname
			
			responseMsg.add(Message.RESULT, Message.CONFIRM);
			responseMsg.add(Message.CHANGE_NAME, changeUserName); // add new nickname
			
			Message noti = new Message().setType(Message.NOTI_CHANGE_NAME); // create notification message: user's info who request changing name
			noti.add(Message.NAME, originName); // add user's origin name
			noti.add(Message.CHANGE_NAME, changeUserName); // add user's new name
			 
			System.out.println("originName: " + originName + ", changeUserName: " + changeUserName);
			 // group.sendWithout(originName, noti);
			group.sendWithout(userName, noti);
			break;
			
		case Message.REQUEST_EXIT_PROGRAM:
			System.out.println("[INFO] user exit the program");
			responseMsg = new Message().setType(Message.RESPONSE_EXIT_GROUP);
			exitUserAtGroup();
			
			try {
				session.close();
				session = null;
			} catch (IOException e) {
				System.err.println("[ERR] session close");
			}
			
			break;
		default:
			responseMsg = new Message().setType(Message.TEST_DEBUG_MODE);
			System.out.println("�삁�쇅�궗�빆");
			break;
		}
		if(session != null) {
			sendMessage(session, responseMsg); // �쟾�넚
		}
	}

	@OnClose
	public void handleClose(Session userSession) {
		if (userSession == null) {
			System.out.println("Session is null");
		}
		System.err.println("[UserController] Session is closed. User terminated the program.");
		session = null;
		exitUserAtGroup();
	}

	@OnError
	public void handleError(Throwable t) {
		System.err.println("[UserController] Error was occured.");
		t.printStackTrace();
	}

	private void sendMessage(Session session, Message message) throws IOException, EncodeException {
		System.out.println("[UserController] message to client: " + message);
		session.getBasicRemote().sendObject(message);
	}

	private void exitUserAtGroup() {
		if(group != null) {
			Message noti = new Message().setType(Message.NOTI_EXIT_PARTICIPANT); // create notification message: outgoing user's info
			noti.add(Message.PARTICIPANT_NAME, userName); // add outgoing user's info
			if(userName == null) {
				System.out.println("userName is null");
			}
			if(group == null) {
				System.out.println("group is null");
			}
			
			try {
				group.sendWithout(userName, noti);
			} catch (IOException e) {
				System.err.println("[ERR] I/O exception. sending message");
				// e.printStackTrace();
			} catch (EncodeException e) {
				System.err.println("[ERR] Encode exception. sending message");
				// e.printStackTrace();
			}
			group.removeUser(userName);
			
			if (group.getSize() == 0) {
				Server.getInstance().removeGroup(group);
			}
			System.out.println("[UserController] User leaves the group.");
		}
	}

	// test main method
	public static void main(String[] args) {
		Message tmpMessage = new Message().setJson("{\"group pk\":\"godoy\",\"message type\":\"request/join group\"}");
		try {
			new UserController().handleMessage(tmpMessage, null);
		} catch (Exception e) {
			System.out.println("�삁�쇅 臾댁떆");
			e.printStackTrace();
		}
	}
}