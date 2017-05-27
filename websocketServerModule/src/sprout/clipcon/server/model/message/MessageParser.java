package sprout.clipcon.server.model.message;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

// import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;

import sprout.clipcon.server.model.Contents;
import sprout.clipcon.server.model.Group;

public class MessageParser {

	public static Message addMessageToGroup(Message message, Group group) {
		message.add(Message.GROUP_PK, group.getPrimaryKey());
		List<String> userList = group.getUserList();

		JSONArray array = new JSONArray();
		Iterator<String> it = userList.iterator();
		while (it.hasNext()) {
			array.put(it.next());
		}
		message.add(Message.LIST, array);
		return message;
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
		String test = getBase64StringByImage(image);
		message.add("imageString", test);
		System.out.println("image string: " + test);
		return message;
	}

	private static String getBase64StringByImage(Image image) {
		byte[] imageBytes = getImgBytes(image);
		return Base64.getEncoder().encodeToString(imageBytes);
	}

	private static byte[] getImgBytes(Image image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(toBufferedImage(image), "png", baos);
		} catch (IOException ex) {
			// handle it here.... not implemented yet...
		}
		return baos.toByteArray();
	}

	private static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	public static void main(String[] args) {

	}
}