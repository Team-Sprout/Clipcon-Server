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
import java.util.Calendar;
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

/* maxFileSize: 최대 파일 크기(100MB)
 * fileSizeThreshold: 1MB 이하의 파일은 메모리에서 바로 사용
 * maxRequestSize:  */
@MultipartConfig(maxFileSize = 1024 * 1024 * 500, fileSizeThreshold = 1024 * 1024, maxRequestSize = 1024 * 1024 * 500)
@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {

	private Server server = Server.getInstance();

	public UploadServlet() {
		System.out.println("UploadServlet 생성");
	}

	// 업로드 파일을 저장할 위치
	//private final String RECEIVE_LOCATION = "C:\\Users\\Administrator\\Desktop\\"; // TEST PATH 2
	private final String RECEIVE_LOCATION = "C:\\Users\\delf\\Desktop\\"; // 

	private String userName = null;
	private String groupPK = null;
	private String uploadTime = null;
	private boolean flag = false;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// requestMsgLog(request);
		System.out.println("================================================================\ndoPost START");

		Date sd = new Date();
		long len = request.getContentLengthLong();
		System.out.print("req len = " + len + " kb (");
		System.out.println((float) len / (1024 * 1024) + " mb)");

		userName = request.getParameter("userName");
		groupPK = request.getParameter("groupPK");
		uploadTime = uploadTime(); // Time that server get request msg
		System.out.println("<<Parameter>>\n userName: " + userName + ", groupPK: " + groupPK + ", uploadTime: " + uploadTime + "\n");
		Group group = server.getGroupByPrimaryKey(groupPK);

		Contents uploadContents = null;
		Message uploadNoti = new Message().setType(Message.NOTI_UPLOAD_DATA); // 알림 메시지 생성, 알림 타입은 "데이터 업로드"
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
				MessageParser.addImageToMessage(uploadNoti, imageData);
				
				System.out.println("imageData: " + imageData.toString());
			break;

			case "fileData":
				createDirectory(RECEIVE_LOCATION + groupPK); // Create Directory to save uploaded file.

				uploadContents = new Contents(Contents.TYPE_FILE, userName, uploadTime, part.getSize());
				uploadContents.setContentsValue(getFilenameInHeader(part.getHeader("Content-Disposition"))); // save fileName

				group.addContents(uploadContents);
				// groupPK 폴더에 실제 File(파일명: 고유키) 저장
				getFileDataStream(part.getInputStream(), groupPK, uploadContents.getContentsPKName());
			break;

			case "multipartFileData":
				createDirectory(RECEIVE_LOCATION + groupPK); // Create Directory to save uploaded file.

				uploadContents = new Contents(Contents.TYPE_MULTIPLE_FILE, userName, uploadTime, part.getSize());
				uploadContents.setContentsValue(getFilenameInHeader(part.getHeader("Content-Disposition"))); // save fileName

				group.addContents(uploadContents);
				// groupPK 폴더에 실제 File(파일명: 고유키) 저장
				getFileDataStream(part.getInputStream(), groupPK, uploadContents.getContentsPKName());
			break;

			default:
				System.out.println("어떤 형식에도 속하지 않음.");
			}
		}

		
		MessageParser.addContentsToMessage(uploadNoti, uploadContents);
		

		try {
			group.sendAll(uploadNoti);
		} catch (EncodeException e) {
			e.printStackTrace();
		}
		System.out.println();

		System.out.println("서블릿 끝");
		Date ed = new Date();
		float t = (float) (ed.getTime() - sd.getTime()) / 1000;
		System.out.println("소요시간 = " + t + "초");
		System.out.print("속도 = " + (float) len / t + " kb/s (");
		System.out.println((float) len / t / (1024 * 1024) + " mb/s)");
		// responseMsgLog(response);
	}

	/** String Data를 수신하는 Stream */
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

	/** Image Data를 수신하는 Stream */
	public Image getImageDataStream(InputStream stream, String groupPK, String imagefileName) throws IOException {
		byte[] imageInByte;
		String saveFilePath = RECEIVE_LOCATION + groupPK; // 사용자가 속한 그룹의 폴더에 저장

		// 업로드한 파일을 저장할 그룹 폴더 생성
		createDirectory(saveFilePath);

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

		// file 형태로 저장
		ImageIO.write(bImageFromConvert, "png", new File(saveFilePath, imagefileName));

		// image 객체로 변환
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

	/** 수신받은 File Data를 수신하는 Stream */
	// 가 아니라 파일화 하는 역할
	public void getFileDataStream(InputStream stream, String groupPK, String fileName) throws IOException {
		Date start = new Date();
		String saveFilePath = RECEIVE_LOCATION + groupPK; // 사용자가 속한 그룹의 폴더에 저장
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
			System.out.println("루프 횟수 = " + testCnt);
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
		System.out.println("소요시간: " + (end.getTime() - start.getTime()));
	}

	/** Request Header "content-disposition"에서 filename 추출 */
	private String getFilenameInHeader(String requestHeader) {
		int beginIndex = requestHeader.indexOf("filename") + 10;
		int endIndex = requestHeader.length() - 1;

		String fileName = requestHeader.substring(beginIndex, endIndex);
		System.out.print("fileName: " + fileName);

		return fileName;
	}

	// XXX: 모델 구현 시, 확인하기
	/**
	 * Folder 생성 메서드
	 * 
	 * @param directoryName
	 *            이 이름으로 Directory 생성
	 */
	private void createDirectory(String directoryName) {
		File receiveFolder = new File(directoryName);
		System.out.println("directoryName: " + directoryName);

		// 저장할 그룹 폴더가 존재하지 않으면
		if (!receiveFolder.exists()) {
			receiveFolder.mkdir(); // 폴더 생성
			System.out.println("------------------------------------" + directoryName + " 폴더 생성");
		}
	}

	/** @return Current Time YYYY-MM-DD HH:MM:SS  */
	public String uploadTime() {
		Calendar cal = Calendar.getInstance();
		String year = Integer.toString(cal.get(Calendar.YEAR));
		String month = Integer.toString(cal.get(Calendar.MONTH) + 1);

		String date = Integer.toString(cal.get(Calendar.DATE));
		String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
		if (Integer.parseInt(hour) < 10) {
			hour = "0" + hour;
		}
		if (Integer.parseInt(hour) > 12) {
			hour = "PM " + Integer.toString(Integer.parseInt(hour) - 12);
		} else {
			hour = "AM " + hour;
		}

		String minute = Integer.toString(cal.get(Calendar.MINUTE));
		if (Integer.parseInt(minute) < 10) {
			minute = "0" + minute;
		}
		String sec = Integer.toString(cal.get(Calendar.SECOND));
		if (Integer.parseInt(sec) < 10) {
			sec = "0" + sec;
		}

		return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + sec;
	}

//	private String getStringFromBitmap(Image bitmapPicture) {
//		String encodedImage;
//		ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
//		bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);
//		byte[] b = byteArrayBitmapStream.toByteArray();
//		encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
//		return encodedImage;
//	}
}
