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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.websocket.EncodeException;

import sprout.clipcon.server.model.Contents;
import sprout.clipcon.server.model.Group;
import sprout.clipcon.server.model.message.Message;
import sprout.clipcon.server.model.message.MessageParser;

/* maxFileSize: Max File Size(100MB)
 * fileSizeThreshold: Files less than 1MB used directly in memory
 * maxRequestSize:  */
@MultipartConfig(maxFileSize = 1024 * 1024 * 500, fileSizeThreshold = 1024 * 1024, maxRequestSize = 1024 * 1024 * 500)
@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = 3432460337698180662L;

	private Server server = Server.getInstance();

	// root location where to save the upload file
	private final String RECEIVE_LOCATION = Server.RECEIVE_LOCATION;

	private String userName = null;
	private String groupPK = null;
	private String uploadTime = null;
	private boolean flag = false;

	/** Constructor UploadServlet */
	public UploadServlet() {
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// requestMsgLog(request);
		System.out.println("================================================================\ndoPost START");

		Date sd = new Date();
		long len = request.getContentLengthLong();
		System.out.print("req len = " + len + " kb (");
		System.out.println((float) len / (1024 * 1024) + " mb)");

		request.setCharacterEncoding("UTF-8");

		userName = request.getParameter("userName");
		groupPK = request.getParameter("groupPK");
		uploadTime = uploadTime(); // Time that server get request msg
		System.out.println("[SERVER] == Parameter info == \n **userName: " + userName + "\n *groupPK: " + groupPK
				+ "\n *uploadTime: " + uploadTime);

		Group group = server.getGroupByPrimaryKey(groupPK);

		Contents uploadContents = null;

		// Notification message generation, notification type "Data upload"
		Message uploadNoti = new Message().setType(Message.NOTI_UPLOAD_DATA);

		for (Part part : request.getParts()) {
			String partName = part.getName();

			switch (partName) {
			case "stringData":
				String paramValue = getStringFromStream(part.getInputStream());
				uploadContents = new Contents(Contents.TYPE_STRING, userName, uploadTime, part.getSize());
				uploadContents.setContentsValue(paramValue);

				System.out.println("[for test]group toString: " + group.toString());
				group.addContents(uploadContents);

				System.out.println("stringData: " + paramValue);
				break;

			case "imageData":
				uploadContents = new Contents(Contents.TYPE_IMAGE, userName, uploadTime, part.getSize());
				group.addContents(uploadContents);

				Image imageData = getImageDataStream(part.getInputStream(), groupPK,
						uploadContents.getContentsPKName());
				MessageParser.addImageToMessage(uploadNoti, imageData);

				System.out.println("imageData: " + imageData.toString());
				break;

			case "fileData":
				createDirectory(RECEIVE_LOCATION + groupPK); // Create Directory to save uploaded file.

				uploadContents = new Contents(Contents.TYPE_FILE, userName, uploadTime, part.getSize());
				uploadContents.setContentsValue(getFilenameInHeader(part.getHeader("Content-Disposition"))); // save fileName

				group.addContents(uploadContents);
				// Save the actual File (filename: unique key) in the groupPK folder
				getFileDataStream(part.getInputStream(), groupPK, uploadContents.getContentsPKName());

				break;
			case "multipartFileData":
				createDirectory(RECEIVE_LOCATION + groupPK); // Create Directory to save uploaded file.

				uploadContents = new Contents(Contents.TYPE_MULTIPLE_FILE, userName, uploadTime, part.getSize());
				uploadContents.setContentsValue(getFilenameInHeader(part.getHeader("Content-Disposition"))); // save fileName

				group.addContents(uploadContents);
				// Save the actual File (filename: unique key) in the groupPK folder
				getFileDataStream(part.getInputStream(), groupPK, uploadContents.getContentsPKName());
				break;

			default:
				System.out.println("<<UPLOAD SERVLET>> It does not belong to any format.");
			}
		}
		MessageParser.addContentsToMessage(uploadNoti, uploadContents);

		try {
			group.sendAll(uploadNoti);
		} catch (EncodeException e) {
			e.printStackTrace();
		}
		System.out.println();

		System.out.println("End of servlet");
		Date ed = new Date();
		float t = (float) (ed.getTime() - sd.getTime()) / 1000;
		System.out.println("Time = " + t + "sec");
		System.out.print("Speed = " + (float) len / t + " kb/s (");
		System.out.println((float) len / t / (1024 * 1024) + " mb/s)");
		// responseMsgLog(response);
	}

	/** The stream that receives the String data */
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

	/** The stream that receives the Captured Image data */
	public Image getImageDataStream(InputStream stream, String groupPK, String imagefileName) throws IOException {
		byte[] imageInByte;
		String saveFilePath = RECEIVE_LOCATION + groupPK; // Save to a folder in the group to which the user belongs

		// Create a group folder(using groupPK) to store uploaded files
		createDirectory(saveFilePath);

		try {
			// opens an output stream to save into file
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			int bytesRead = -1;
			byte[] buffer = new byte[0xFFFF]; // 65536

			// input stream from the HTTP connection
			while ((bytesRead = stream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			}
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

		// Save as file
		ImageIO.write(bImageFromConvert, "png", new File(saveFilePath, imagefileName));

		// Convert to image object
		Image ImageData = (Image) bImageFromConvert;
		return ImageData;
	}

	/** The stream that receives the File Data and make real file */
	// 가 아니라 파일화 하는 역할
	public void getFileDataStream(InputStream stream, String groupPK, String fileName) throws IOException {
		Date start = new Date();
		String saveFilePath = RECEIVE_LOCATION + groupPK; // Save to a folder in the group to which the user belongs
		String saveFileFullPath = saveFilePath + File.separator + fileName;

		// opens an output stream to save into file
		FileOutputStream fileOutputStream = new FileOutputStream(saveFileFullPath);

		int bytesRead = -1;
		byte[] buffer = new byte[0xFFFF]; // 65536
		long totalBytesRead = 0;
		int percentCompleted = 0;

		try {
			// input stream from the HTTP connection
			while ((bytesRead = stream.read(buffer)) != -1) {
				totalBytesRead += bytesRead;
				percentCompleted = (int) (totalBytesRead * 100 / 206498989);

				// [TODO] FOR progress bar test
				// System.out.println(percentCompleted);

				fileOutputStream.write(buffer, 0, bytesRead);
			}

			fileOutputStream.flush();
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
		System.out.println("Time: " + (end.getTime() - start.getTime()));
	}

	/** Extract filename from Request Header "content-disposition" */
	private String getFilenameInHeader(String requestHeader) {
		int beginIndex = requestHeader.indexOf("filename") + 10;
		int endIndex = requestHeader.length() - 1;

		String fileName = requestHeader.substring(beginIndex, endIndex);
		System.out.print("fileName: " + fileName);

		return fileName;
	}

	// XXX: CHECK!! When model implementation
	/**
	 * Create a directory
	 * 
	 * @param directoryName
	 *            The name of the directory you want to create
	 */
	private void createDirectory(String directoryName) {
		File receiveFolder = new File(directoryName);
		System.out.println("directoryName: " + directoryName);

		if (!receiveFolder.exists()) {
			receiveFolder.mkdir(); // Create Directory
			System.out.println("------------------------------------" + directoryName + " Create Directory");
		}
	}

	/** @return Current Time YYYY-MM-DD HH:MM:SS */
	public String uploadTime() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss");

		return sdf.format(date).toString();
	}
}
