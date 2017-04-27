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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.swing.ImageIcon;

import com.oreilly.servlet.MultipartRequest;

import sprout.clipcon.server.model.Contents;
import sprout.clipcon.server.model.Group;

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
	// private final String RECEIVE_LOCATION = "C:\\Users\\Administrator\\Desktop\\"; // 테스트 경로2
	private final String RECEIVE_LOCATION = "C:\\Users\\delf\\Desktop\\"; // 테스트 경로1
	// 업로드한 파일을 저장할 폴더
	private File receiveFolder;

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
		System.out.println("================================================================\ndoPost시작");

		Date sd = new Date();
		long len = request.getContentLengthLong();
		System.out.print("req len = " + len + " kb (");
		System.out.println((float) len / (1024 * 1024) + " mb)");

		userName = request.getParameter("userName");
		groupPK = request.getParameter("groupPK");
		uploadTime = request.getParameter("uploadTime");
		System.out.println("<Parameter> userName: " + userName + ", groupPK: " + groupPK + ", uploadTime: " + uploadTime + "\n");

		Group group = server.getGroupByPrimaryKey(groupPK);
		
		for (Part part : request.getParts()) {
			String partName = part.getName();
			Contents uploadContents;

			/*
			 * To find out file name, parse header value of content-disposition e.g. form-data; name="file"; filename=""
			 */
			System.out.println("<headerName: headerValue>");
			for (String headerName : part.getHeaderNames()) {
				System.out.println(headerName + ": " + part.getHeader(headerName));
			}

			System.out.println("...........>> " + partName);

			switch (partName) {
				case "stringData":
					String paramValue = getStringFromStream(part.getInputStream());
					uploadContents = new Contents(Contents.TYPE_STRING, userName, uploadTime, part.getSize());
					uploadContents.setContentsValue(paramValue);
					System.out.println("stringData: " + paramValue);
					// TODO[delf]: text의 크기가 일정 이상이면 파일로 저장
					
					group.addContents(uploadContents);
					break;

				case "imageData":
					uploadContents = new Contents(Contents.TYPE_IMAGE, userName, uploadTime, part.getSize());
					Image imageData = getImageDataStream(part.getInputStream(), groupPK, uploadContents.getContentsPKName());
					System.out.println("imageData: " + imageData.toString());
					group.addContents(uploadContents);
					break;

				// 여러 file들을 가져옴
				case "multipartFileData":
					String fileName = getFilenameInHeader(part.getHeader("content-disposition"));
					String saveFilePath = RECEIVE_LOCATION + groupPK; // 사용자가 속한 그룹의 폴더에 저장

					uploadContents = new Contents(Contents.TYPE_FILE, userName, uploadTime, part.getSize());
					uploadContents.setContentsValue(fileName);
					System.out.println("fileName: " + fileName + ", saveFilePath: " + saveFilePath);

					/* groupPK 폴더에 실제 File(파일명: 고유키) 저장 */
					getFileDatStream(part.getInputStream(), groupPK, uploadContents.getContentsPKName());
					
					group.addContents(uploadContents);
					break;

				default:
					System.out.println("어떤 형식에도 속하지 않음.");
			}
			System.out.println();
		}
		
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

		createFileReceiveFolder(saveFilePath); // 그룹 폴더 존재 확인

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
		ImageIO.write(bImageFromConvert, "jpg", new File(saveFilePath, imagefileName));

		// image 객체로 변환
		Image ImageData = (Image) bImageFromConvert;
		return ImageData;
	}

	/** File Data를 수신하는 Stream */
	public void getFileDatStream(InputStream stream, String groupPK, String fileName) throws IOException {

		Date start = new Date();
		String saveFilePath = RECEIVE_LOCATION + groupPK; // 사용자가 속한 그룹의 폴더에 저장
		String saveFileFullPath = saveFilePath + "\\" + fileName;

		createFileReceiveFolder(saveFilePath); // 그룹 폴더 존재 확인

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
	/** 업로드한 파일을 저장할 그룹 폴더 생성 */
	private void createFileReceiveFolder(String saveFilePath) {
		receiveFolder = new File(saveFilePath);
		System.out.println("폴더 생성");
		// C:\\Program Files에 LinKlipboard폴더가 존재하지 않으면
		if (!receiveFolder.exists()) {
			receiveFolder.mkdir(); // 폴더 생성
			System.out.println("------------------" + saveFilePath + " 폴더 생성");
		}
	}

	/** Contents에 대한 정보 Setting */
	private void setContentsInfo(Contents uploadContents, long contentsSize, String contentsType, String contentsValue) {
		System.out.println("<contentsPKName>: " + uploadContents.contentsPKName);
		uploadContents.setContentsSize(contentsSize);
		uploadContents.setUploadUserName(userName);
		uploadContents.setUploadTime(uploadTime);
		System.out.println("<CommonSetting> ContentsSize: " + uploadContents.getContentsSize() + ", UploadUserName: " + uploadContents.getUploadUserName() + ", UploadTime: " + uploadContents.getUploadTime());

		uploadContents.setContentsType(contentsType);
		uploadContents.setContentsValue(contentsValue);
		System.out.println("<UniqueSetting> contentsType: " + uploadContents.getContentsType() + ", contentsValue: " + uploadContents.getContentsValue());

		//saveContentsToHistory(uploadContents);
	}

	/** Image를 Resizing한 ImageIcon으로 return */
	public ImageIcon getResizingImageIcon(Image imageData) {
		// FIXME: 이미지의 크기를 줄일 때, 비율을 맞출 것
		imageData = imageData.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
		ImageIcon resizingImageIcon = new ImageIcon(imageData);

		return resizingImageIcon;
	}

	/** request Msg 출력 */
	public void requestMsgLog(HttpServletRequest request) {

		/* server가 받은 request 시작줄 정보 */
		System.out.println("==================STARTLINE==================");
		System.out.println("Request Method: " + request.getMethod());
		System.out.println("Request RequestURI: " + request.getRequestURI());
		System.out.println("Request Protocol: " + request.getProtocol());

		/* server가 받은 request 헤더 정보 */
		/* server가 받은 기본적인 request header msg 정보 */
		System.out.println("===================HEADER====================");
		Enumeration headerNames = request.getHeaderNames();

		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();

			System.out.println(headerName + ": " + request.getHeader(headerName));
		}
		System.out.println("-------------------------------------------");
		System.out.println("Request LocalAddr: " + request.getLocalAddr());
		System.out.println("Request LocalName: " + request.getLocalName());
		System.out.println("Request LocalPort: " + request.getLocalPort());
		System.out.println("-------------------------------------------");
		System.out.println("Request RemoteAddr: " + request.getRemoteAddr());
		System.out.println("Request RemoteHost: " + request.getRemoteHost());
		System.out.println("Request RemotePort: " + request.getRemotePort());
		System.out.println("Request RemoteUser: " + request.getRemoteUser());

		System.out.println("==================ENTITY====================");
		System.out.println("userName: " + request.getParameter("userName"));
		System.out.println("groupPK: " + request.getParameter("groupPK"));
		System.out.println("downloadDataPK: " + request.getParameter("downloadDataPK"));
		System.out.println("===========================================");
		System.out.println();
		System.out.println();
	}

	/** Client로 response Msg 전달 */
	public void responseMsgLog(HttpServletResponse response) {
		PrintWriter writer;
		try {
			writer = response.getWriter();

			response.setContentType("text/html");
			// response.setCharacterEncoding("UTF-8");

			writer.println("Http Post Response: " + response.toString());

			/* client가 받은 response 시작줄 정보 */
			writer.println("==================STARTLINE==================");
			writer.println("Response Status: " + response.getStatus());
			writer.println("Response ContentType: " + response.getContentType());

			/* client가 받은 response 헤더 정보 */
			writer.println("==================HEADER=====================");
			Collection<String> headerNames = response.getHeaderNames();

			while (!headerNames.isEmpty()) {
				String headerName = (String) headerNames.toString();

				writer.println(headerName + ": " + response.getHeader(headerName));
			}

			writer.println("===================ENTITY====================");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class TestThread extends Thread {
	@Override
	public void run() {

	}
}