package sprout.clipcon.server.model.message;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.json.JSONArray;
import org.json.JSONObject;

import sprout.clipcon.server.model.AddressBook;
import sprout.clipcon.server.model.Contents;
import sprout.clipcon.server.model.Group;

public class MessageParser {

	public static AddressBook getAddressBookByMessage(Message message) {
		AddressBook addressBook = new AddressBook();
		Map<String, String> users = addressBook.getUsers();
		JSONObject jsonMsg = message.getJson();
		JSONArray array = jsonMsg.getJSONArray(Message.LIST);

		Iterator<?> it = array.iterator();
		while (it.hasNext()) {
			JSONObject tmpJson = (JSONObject) it.next();
			String email = tmpJson.getString(Message.EMAIL);
			String name = tmpJson.getString(Message.NAME);
			// User tmpUser = new User(email, tmpJson.getString(Message.NAME));
			users.put(email, name);
		}
		return addressBook;
	}

	// public static Message getMessageByUser(User user) {
	// Message message = new Message().setType(Message.USER_INFO); // 반환할 객체, 타입은 '유저정보'
	// message.add(Message.NAME, user.getName());// name 삽입
	// return message;
	// }

	public static Message addMessageToGroup(Message message, Group group) {
		message.add(Message.GROUP_PK, group.getPrimaryKey());
		List<String> userList = group.getUserList();
		System.out.println("list size: " + userList.size());

		JSONArray array = new JSONArray();
		Iterator<String> it = userList.iterator();
		while (it.hasNext()) {
			array.put(it.next());
		}
		message.add(Message.LIST, array);
		return message;
		// TODO:
	}

	public static Message appendMessageByGroup(Message message, Group group) {
		return new Message();
	}

	public static Message addContentsToMessage(Message message, Contents contents) {
		message.add("contentsType", contents.getContentsType()); // type
		message.add("contentsSize", contents.getContentsSize()); // size
		message.add("contentsPKName", contents.getContentsPKName()); // pk
		message.add("uploadUserName", contents.getUploadUserName()); // name
		message.add("uploadTime", contents.getUploadTime()); // time
		message.add("contentsValue", contents.getContentsValue());

		return message;
	}

	public static Message addImageToMessage(Message message, Image image) {
		image = getResizingImageIcon(image);
		message.add("imageString", getBase64StringByImage(image));
		return message;
	}

	/** Image를 Resizing한 ImageIcon으로 return */
	private  static Image getResizingImageIcon(Image imageData) {
		// FIXME: 이미지의 크기를 줄일 때, 비율을 맞출 것
		imageData = imageData.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
		return imageData;
	}

	private static byte[] getImgBytes(Image image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(getBufferedImage(image), "JPEG", baos);
		} catch (IOException ex) {
			// handle it here.... not implemented yet...
		}
		return baos.toByteArray();
	}

	private static BufferedImage getBufferedImage(Image image) {
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// Graphics2D g2d = bi.createGraphics();
		// g2d.drawImage(image, 0, 0, null);
		return bi;
	}

	private static String getBase64StringByImage(Image image) {
		byte[] imageBytes = getImgBytes(image);
		return Base64.getEncoder().encodeToString(imageBytes);
	}

	// public static Group getGroupByMessage(Message message) {
	// Group group = new Group();
	// String key = message.get("groupkey");
	// String name = message.get("groupname");
	// group.setPrimaryKey(key);
	// group.setName(name);
	//
	// group.setUsers(message.getObject("list"));
	// return group;
	// }

	public static void main(String[] args) {

		// List<String> list = new ArrayList<String>();
		// list.add("em1");
		// list.add("em2");
		// list.add("em3");
		// list.add("em4");
		//
		// Group tmpGroup = new Group();
		// tmpGroup.setPrimaryKey("##");
		//
		// Message tmpMessage = new Message().setType(Message.TEST_DEBUG_MODE);
		//
		// JSONArray array = new JSONArray();
		// Iterator<?> it = list.iterator();
		// while (it.hasNext()) {
		// array.put(it.next());
		// }
		// tmpMessage.add(Message.LIST, array);
		// System.out.println(tmpMessage);
	}

	public List<String> createTestList() {
		List<String> list = new ArrayList<String>();
		list.add("em1");
		list.add("em2");
		list.add("em3");
		list.add("em4");
		return list;
	}

}
