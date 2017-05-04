package sprout.clipcon.server.model.message;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.JSONArray;

import sprout.clipcon.server.model.Contents;
import sprout.clipcon.server.model.Group;

public class MessageParser {

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
	private static Image getResizingImageIcon(Image imageData) {
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
	/** @author delf
	 * client code */
	public static Image getImagebyMessage(Message message) {
		String imageString = message.get("imageString");
		byte[] imageBytes = Base64.getDecoder().decode(imageString);
		BufferedImage imag;
		try {
			imag = ImageIO.read(new ByteArrayInputStream(imageBytes));
			return imag;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	public static void main(String[] args) {

	}
}