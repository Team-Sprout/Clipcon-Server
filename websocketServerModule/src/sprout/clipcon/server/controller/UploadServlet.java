package sprout.clipcon.server.controller;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.swing.ImageIcon;
import javax.websocket.EncodeException;

import lombok.NonNull;
import sprout.clipcon.server.model.Contents;
import sprout.clipcon.server.model.Group;
import sprout.clipcon.server.model.message.Message;
import sprout.clipcon.server.model.message.MessageParser;

/* maxFileSize: �ִ� ���� ũ��(100MB)
 * fileSizeThreshold: 1MB ������ ������ �޸𸮿��� �ٷ� ���
 * maxRequestSize:  */
@MultipartConfig(maxFileSize = 1024 * 1024 * 500, fileSizeThreshold = 1024 * 1024, maxRequestSize = 1024 * 1024 * 500)
@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {

	private Server server = Server.getInstance();

	public UploadServlet() {
		System.out.println("UploadServlet ����");
	}

	// ���ε� ������ ������ ��ġ
	private final String RECEIVE_LOCATION = "C:\\Users\\Administrator\\Desktop\\"; // �׽�Ʈ ���2
	// private final String RECEIVE_LOCATION = "C:\\Users\\delf\\Desktop\\"; // �׽�Ʈ ���1

	private String userName = null;
	private String groupPK = null;
	private String uploadTime = null;
	private String createFolder = null;
	private boolean flag = false;

	private long multipleFileTotalSize = 0;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// requestMsgLog(request);
		System.out.println("================================================================\ndoPost����");

		Date sd = new Date();
		long len = request.getContentLengthLong();
		System.out.print("req len = " + len + " kb (");
		System.out.println((float) len / (1024 * 1024) + " mb)");

		userName = request.getParameter("userName");
		groupPK = request.getParameter("groupPK");
		uploadTime = request.getParameter("uploadTime");
		createFolder = request.getParameter("createFolder");
		System.out.println("<<Parameter>>\n userName: " + userName + ", groupPK: " + groupPK + ", uploadTime: " + uploadTime + ", createFolder: " + createFolder + "\n");
		Group group = server.getGroupByPrimaryKey(groupPK);

		Contents multipartUploadContents = new Contents();
		Contents uploadContents = null;

		for (Part part : request.getParts()) {
			String partName = part.getName();

			/*
			 * To find out file name, parse header value of content-disposition e.g. form-data; name="file"; filename=""
			 */
			System.out.println("\n<headerName: headerValue>");
			for (String headerName : part.getHeaderNames()) {
				System.out.println(headerName + ": " + part.getHeader(headerName));
			}

			System.out.println("...........>> " + partName);

			switch (partName) {
			case "stringData":
				String paramValue = getStringFromStream(part.getInputStream());
				uploadContents = new Contents(Contents.TYPE_STRING, userName, uploadTime, part.getSize());
				uploadContents.setContentsValue(paramValue);
				group.addContents(uploadContents);

				System.out.println("stringData: " + paramValue);

			break;

			case "imageData":
				uploadContents = new Contents(Contents.TYPE_IMAGE, userName, uploadTime, part.getSize());
				group.addContents(uploadContents);

				Image imageData = getImageDataStream(part.getInputStream(), groupPK, uploadContents.getContentsPKName());

				System.out.println("imageData: " + imageData.toString());

			break;

			// ���� file���� ������
			case "multipartFileData":
				String fileName = getFilenameInHeader(part.getHeader("Content-Disposition"));
				String relativeFilePath = part.getHeader("Content-RelativePath");

				String saveFilePath = RECEIVE_LOCATION + groupPK; // ����ڰ� ���� �׷��� ������ ����
				createFolder(saveFilePath); // ���ε��� ������ ������ �׷� ���� ����

				/*
				 * file data�� �ϳ��� ��� ���� ���� ���� X
				 */
				if (createFolder.equals("FALSE")) {
					uploadContents = new Contents(Contents.TYPE_FILE, userName, uploadTime, part.getSize());
					uploadContents.setContentsValue(fileName);

					group.addContents(uploadContents);

					System.out.println(uploadContents.getContentsPKName());
					System.out.println("fileName: " + fileName + ", saveFilePath: " + saveFilePath + ", relativeFilePath: " + relativeFilePath);

					// groupPK ������ ���� File(���ϸ�: ����Ű) ����
					getFileDataStream(part.getInputStream(), saveFilePath, uploadContents.getContentsPKName());
				}

				/*
				 * file data�� ���� ���� ��� ���� ���� ���� O, �� data�� Contents�� �߰�
				 */
				else if (createFolder.equals("TRUE")) {
					if (uploadContents == null) {
						uploadContents = new Contents(Contents.TYPE_MULTIPLE_FILE, userName, uploadTime, part.getSize());
						multipartUploadContents = group.addContents(uploadContents);
					}
					System.out.println("fileName: " + fileName + ", saveFilePath: " + saveFilePath + "relativeFilePath: " + relativeFilePath);
					String filePKName = multipartUploadContents.addFilePath(relativeFilePath, fileName);

					multipleFileTotalSize = multipleFileTotalSize + part.getSize();
					multipartUploadContents.setContentsSize(multipleFileTotalSize);

					saveFilePath = saveFilePath + File.separator + uploadContents.getContentsPKName();
					System.out.println("���� ���� �̸�: " + saveFilePath);
					createFolder(saveFilePath); // ���� ���� ����

					// groupPK ������ ���� ������ ���� File(���ϸ�: ����Ű) ����
					getFileDataStream(part.getInputStream(), saveFilePath, filePKName);
				}
			break;

			// ���� directory �������� ������
			case "directoryData":
				if (uploadContents == null) {
					uploadContents = new Contents(Contents.TYPE_MULTIPLE_FILE, userName, uploadTime, part.getSize());
					multipartUploadContents = group.addContents(uploadContents);
				}
				String directoryName = getStringFromStream(part.getInputStream());
				directoryName = directoryName.substring(0, directoryName.length() - 1);

				System.out.println("directoryName: " + directoryName);
				multipartUploadContents.addFilePath(directoryName, null);
			break;

			default:
				System.out.println("� ���Ŀ��� ������ ����.");
			}
		}
		multipartUploadContents.printAllFileInfo(); // TEST

		Message uploadNoti = new Message().setType(Message.NOTI_UPLOAD_DATA); // �˸� �޽��� ����, �˸� Ÿ���� "������ ���ε�"
		MessageParser.addContentsToMessage(uploadNoti, uploadContents);

		try {
			group.sendAll(uploadNoti);
		} catch (EncodeException e) {
			e.printStackTrace();
		}
		System.out.println();

		System.out.println("���� ��");
		Date ed = new Date();
		float t = (float) (ed.getTime() - sd.getTime()) / 1000;
		System.out.println("�ҿ�ð� = " + t + "��");
		System.out.print("�ӵ� = " + (float) len / t + " kb/s (");
		System.out.println((float) len / t / (1024 * 1024) + " mb/s)");
		// responseMsgLog(response);
	}

	/** String Data�� �����ϴ� Stream */
	public String getStringFromStream(InputStream stream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;

		try {
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stringBuilder.toString();
	}

	/** Image Data�� �����ϴ� Stream */
	public Image getImageDataStream(InputStream stream, String groupPK, String imagefileName) throws IOException {
		byte[] imageInByte;
		String saveFilePath = RECEIVE_LOCATION + groupPK; // ����ڰ� ���� �׷��� ������ ����

		// ���ε��� ������ ������ �׷� ���� ����
		createFolder(saveFilePath);

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
			byte[] buffer = new byte[0xFFFF]; // 65536

			for (int len; (len = stream.read(buffer)) != -1;)
				byteArrayOutputStream.write(buffer, 0, len);

			byteArrayOutputStream.flush();

			imageInByte = byteArrayOutputStream.toByteArray();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// convert byte array back to BufferedImage
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageInByte);
		BufferedImage bImageFromConvert = ImageIO.read(byteArrayInputStream);

		// file ���·� ����
		ImageIO.write(bImageFromConvert, "png", new File(saveFilePath, imagefileName));

		// image ��ü�� ��ȯ
		Image ImageData = (Image) bImageFromConvert;
		return ImageData;
	}

	// public ImageIcon getByteArrayByImage(Image image) throws IOException {
	// // ImageIO.write(im, formatName, output)
	// ByteArrayOutputStream out = new ByteArrayOutputStream();
	// ImageIO.write(image, "JPEG", out);
	// byte[] imageBytes = out.toByteArray();
	// BufferedImage bi = ImageIO.read(new ByteArrayInputStream(imageBytes));
	// }

	/** File Data�� �����ϴ� Stream */
	/** ���Ź��� File Data�� �����ϴ� Stream */

	// �� �ƴ϶� ����ȭ �ϴ� ����
	public void getFileDataStream(InputStream stream, String saveFilePath, String fileName) throws IOException {

		Date start = new Date();
		String saveFileFullPath = saveFilePath + File.separator + fileName;

		// opens an output stream to save into file
		FileOutputStream fileOutputStream = new FileOutputStream(saveFileFullPath);

		int bytesRead = -1;
		byte[] buffer = new byte[0xFFFF]; // 65536

		int testCnt = 0;
		try {
			// input stream from the HTTP connection
			while ((bytesRead = stream.read(buffer)) != -1) {
				testCnt++;
				fileOutputStream.write(buffer, 0, bytesRead);
			}
			fileOutputStream.flush();
			System.out.println("���� Ƚ�� = " + testCnt);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fileOutputStream.close();
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Date end = new Date();
		System.out.println("�ҿ�ð�: " + (end.getTime() - start.getTime()));
	}

	/** Request Header "content-disposition"���� filename ���� */
	private String getFilenameInHeader(String requestHeader) {
		int beginIndex = requestHeader.indexOf("filename") + 10;
		int endIndex = requestHeader.length() - 1;

		String fileName = requestHeader.substring(beginIndex, endIndex);

		return fileName;
	}

	// XXX: �� ���� ��, Ȯ���ϱ�
	/**
	 * Folder ���� �޼���
	 * 
	 * @param saveFilePath
	 *            �� �̸����� ���� ����
	 */
	private void createFolder(String folderName) {
		File receiveFolder = new File(folderName);

		// ������ �׷� ������ �������� ������
		if (!receiveFolder.exists()) {
			receiveFolder.mkdir(); // ���� ����
			System.out.println("------------------------------------" + folderName + " ���� ����");
		}
	}

	// O
	// private String getStringFromBitmap(Image bitmapPicture) {
	// String encodedImage;
	// ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
	// bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);
	// byte[] b = byteArrayBitmapStream.toByteArray();
	// encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
	// return encodedImage;
	// }

	// ���� �ִ� �α� �ڵ� �������ؼ� ���� TmpLog�� ��
}
// O
// private String getStringFromBitmap(Image bitmapPicture) {
// String encodedImage;
// ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
// bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);
// byte[] b = byteArrayBitmapStream.toByteArray();
// encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
// return encodedImage;
// }

// XXX: ���� �ִ� �α� �ڵ� �������ؼ� ���� TmpLog�� ��
