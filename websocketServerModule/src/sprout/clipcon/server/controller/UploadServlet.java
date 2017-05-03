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
	private final String RECEIVE_LOCATION = "C:\\Users\\Administrator\\Desktop\\"; // 테스트 경로2
	// private final String RECEIVE_LOCATION = "C:\\Users\\delf\\Desktop\\"; // 테스트 경로1

	private String userName = null;
	private String groupPK = null;
	private String uploadTime = null;
	private String createFolder = null;
	private boolean flag = false;

	private long multipleFileTotalSize = 0;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// requestMsgLog(request);
		System.out.println("================================================================\ndoPost시작");

		Date sd = new Date();
		long len = request.getContentLengthLong();
		System.out.print("req len = " + len + " kb (");
		System.out.println((float) len / (1024 * 1024) + " mb)");

		userName = request.getParameter("userName");
		groupPK = request.getParameter("groupPK");
		uploadTime = request.getParameter("uploadTime");
		createFolder = request.getParameter("createFolder");
		System.out.println("<<Parameter>>\n userName: " + userName + ", groupPK: " + groupPK + ", uploadTime: "
				+ uploadTime + ", createFolder: " + createFolder + "\n");

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

				// TODO[delf]: text의 크기가 일정 이상이면 파일로 저장
				break;

			case "imageData":
				uploadContents = new Contents(Contents.TYPE_IMAGE, userName, uploadTime, part.getSize());
				group.addContents(uploadContents);

				Image imageData = getImageDataStream(part.getInputStream(), groupPK,
						uploadContents.getContentsPKName());

				System.out.println("imageData: " + imageData.toString());

				break;

			// 여러 file들을 가져옴
			case "multipartFileData":
				String fileName = getFilenameInHeader(part.getHeader("Content-Disposition"));
				String relativeFilePath = part.getHeader("Content-RelativePath");

				String saveFilePath = RECEIVE_LOCATION + groupPK; // 사용자가 속한 그룹의 폴더에 저장
				createFolder(saveFilePath); // 업로드한 파일을 저장할 그룹 폴더 생성

				/*
				 * file data가 하나인 경우 내부 폴더 생성 X
				 */
				if (createFolder.equals("FALSE")) {
					uploadContents = new Contents(Contents.TYPE_FILE, userName, uploadTime, part.getSize());
					uploadContents.setContentsValue(fileName);

					group.addContents(uploadContents);

					System.out.println(uploadContents.getContentsPKName());
					System.out.println("fileName: " + fileName + ", saveFilePath: " + saveFilePath
							+ ", relativeFilePath: " + relativeFilePath);

					// groupPK 폴더에 실제 File(파일명: 고유키) 저장
					getFileDataStream(part.getInputStream(), saveFilePath, uploadContents.getContentsPKName());
				}

				/*
				 * file data가 여러 개인 경우 내부 폴더 생성 O, 각 data를 Contents에 추가
				 */
				else if (createFolder.equals("TRUE")) {
					if (uploadContents == null) {
						uploadContents = new Contents(Contents.TYPE_MULTIPLE_FILE, userName, uploadTime,
								part.getSize());
						multipartUploadContents = group.addContents(uploadContents);
					}
					System.out.println("fileName: " + fileName + ", saveFilePath: " + saveFilePath
							+ "relativeFilePath: " + relativeFilePath);
					String filePKName = multipartUploadContents.addFilePath(relativeFilePath, fileName);

					multipleFileTotalSize = multipleFileTotalSize + part.getSize();
					multipartUploadContents.setContentsSize(multipleFileTotalSize);

					saveFilePath = saveFilePath + File.separator + uploadContents.getContentsPKName();
					System.out.println("내부 폴더 이름: " + saveFilePath);
					createFolder(saveFilePath); // 내부 폴더 생성

					// groupPK 폴더의 내부 폴더에 실제 File(파일명: 고유키) 저장
					getFileDataStream(part.getInputStream(), saveFilePath, filePKName);
				}
				break;

			// 여러 directory 정보들을 가져옴
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
				System.out.println("어떤 형식에도 속하지 않음.");
			}
		}
		multipartUploadContents.printAllFileInfo(); // TEST

		Message uploadNoti = new Message().setType(Message.NOTI_UPLOAD_DATA); // 알림 메시지 생성, 알림 타입은 "데이터 업로드"
		MessageParser.addContentsToMessage(uploadNoti, uploadContents);

		try {
			group.send(userName, uploadNoti);
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

		// file 형태로 저장
		ImageIO.write(bImageFromConvert, "png", new File(saveFilePath, imagefileName));

		// image 객체로 변환
		Image ImageData = (Image) bImageFromConvert;
		return ImageData;
	}

	/** 수신받은 File Data를 수신하는 Stream */
	// 가 아니라 파일화 하는 역할
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

		return fileName;
	}

	// XXX: 모델 구현 시, 확인하기
	/**
	 * Folder 생성 메서드
	 * 
	 * @param saveFilePath
	 *            이 이름으로 폴더 생성
	 */
	private void createFolder(String folderName) {
		File receiveFolder = new File(folderName);

		// 저장할 그룹 폴더가 존재하지 않으면
		if (!receiveFolder.exists()) {
			receiveFolder.mkdir(); // 폴더 생성
			System.out.println("------------------------------------" + folderName + " 폴더 생성");
		}
	}

	/** Image를 Resizing한 ImageIcon으로 return */
	public ImageIcon getResizingImageIcon(Image imageData) {
		// FIXME: 이미지의 크기를 줄일 때, 비율을 맞출 것
		imageData = imageData.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
		ImageIcon resizingImageIcon = new ImageIcon(imageData);

		return resizingImageIcon;
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

	// XXX: 여기 있던 로그 코드 지저분해서 따로 TmpLog로 뺌
}