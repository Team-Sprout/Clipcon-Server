package sprout.clipcon.server.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.websocket.EncodeException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sprout.clipcon.server.controller.UserController;
import sprout.clipcon.server.model.message.Message;

@Getter
@Setter
@NoArgsConstructor
public class Group {
	private String primaryKey;
	private String name;
	private Map<String, UserController> users = Collections.synchronizedMap(new HashMap<String, UserController>());
	private History history;
	
	//[희정] debug mode for download test
	public static int cnt;

	public Group(String primaryKey) {
		this.primaryKey = primaryKey;
		this.history = new History(primaryKey);
		
		//[희정] debug mode for download test
		System.out.println("Group 생성자 불림");
		setDefaultHistory(this);
	}

	public void sendWithout(String user, Message message) throws IOException, EncodeException {
		System.out.println("자신을 제외한 그룹 전체에게 보내다.");
		for (String key : users.keySet()) {
			if (key.equals(user)) // 제외
				continue;
			users.get(key).getSession().getBasicRemote().sendObject(message);
		}
	}
	
	public void sendAll(Message message) throws IOException, EncodeException {
		System.out.println("그룹 전체에게 보내다.");
		for (String key : users.keySet()) {
			users.get(key).getSession().getBasicRemote().sendObject(message);
		}
	}

	public String addUser(String name, UserController session) {
		// [희정] debug mode for download test
		//String tmpName = getTempUsername();
		String tmpName = "test" + ++cnt;
		
		users.put(tmpName, session);
		System.out.println("새 유저가 그룹에 입장");
		return tmpName;
	}

	public List<String> getUserList() {
		List<String> list = new ArrayList<String>();
		for (String key : users.keySet()) {
			list.add(key);
		}
		return list;
	}

	public Contents addContents(Contents contents) {
		return history.addContents(contents);
	}

	public Contents getContents(String key) {
		return history.getContentsByPK(key);
	}

	public int getSize() {
		return users.size();
	}

	public String getTempUsername() {
		StringBuffer temp = new StringBuffer();
		Random rnd = new Random();
		for (int i = 0; i < 6; i++) {
			int rIndex = rnd.nextInt(1);
			switch (rIndex) {
			case 0:
				// a-z
				temp.append((char) ((int) (rnd.nextInt(26)) + 97));
			break;
			case 1:
				// 0-9
				temp.append((rnd.nextInt(10)));
			break;
			}
		}
		return temp.toString();
	}

	public void removeUser(String userName) {
		users.remove(userName);
	}
	
	// [희정] default setting for download test
	public void setDefaultHistory(Group abcABCGroup) {
		/* test를 위한 setting (원래는 알림을 받았을 때 세팅) */
		Contents content1 = new Contents(Contents.TYPE_STRING, "test1", "2017-05-01 PM 11:11:11", 94);
		content1.setContentsValue("동해물과 백두산이 마르고 닳도록 하느님이 보우하사 우리 나라 만세\n"); //string value

		Contents content2 = new Contents(Contents.TYPE_IMAGE, "test2", "2017-05-02 PM 22:22:22", 6042);
		content2.setContentsValue(null);

		Contents content3 = new Contents(Contents.TYPE_FILE, "test3", "2017-05-03 PM 33:33:33", 5424225);
		content3.setContentsValue("IU-Palette.mp3"); //file name

		Contents content4 = new Contents(Contents.TYPE_MULTIPLE_FILE, "test4", "2017-05-04 PM 44:44:44", 79895123);
		content4.addFilePath("aaa", Contents.TYPE_DIRECTORY);
		content4.addFilePath("aaa" + File.separator + "bbbb", Contents.TYPE_DIRECTORY);
		content4.addFilePath("aaa" + File.separator + "bbbb", "2.zip");
		content4.addFilePath("aaa" + File.separator + "bbbb" + File.separator + "cccc", Contents.TYPE_DIRECTORY);
		content4.addFilePath("aaa", "hello.hwp");

		// test) abcABC group의 History에 setting
		abcABCGroup.addContents(content1);
		abcABCGroup.addContents(content2);
		abcABCGroup.addContents(content3);
		abcABCGroup.addContents(content4);
	}
}
