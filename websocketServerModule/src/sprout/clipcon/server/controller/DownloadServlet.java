package sprout.clipcon.server.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sprout.clipcon.server.model.Contents;
import sprout.clipcon.server.model.Group;

/**
 * Servlet implementation class DownloadServlet
 */
@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// 파일을 저장되어있는 root location
	private final String ROOT_LOCATION = "C:\\Users\\Delf\\Desktop\\";

	private static final int CHUNKSIZE = 4096;
	private static final String LINE_FEED = "\r\n";
	private String charset = "UTF-8";

	private String userName = null;
	private String groupPK = null;
	private String downloadDataPK = null;

	private Server server = Server.getInstance();
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DownloadServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		requestMsgLog(request);

		// get Request Data
		userName = request.getParameter("userEmail");
		groupPK = request.getParameter("groupPK");
		downloadDataPK = request.getParameter("downloadDataPK");
		System.out.println("<Parameter> userEmail: " + userName + ", groupPK: " + groupPK + ", downloadDataPK: " + downloadDataPK);
		System.out.println();
		
		// XXX[ALL]: 여기 있던 코드는 github나 맨 아래 주석을 참조할 것 지저분해서 버렸음
		
		Group group = server.getGroupByPrimaryKey(groupPK);
		Contents contents = group.getContents(downloadDataPK);
		String contentsType = contents.getContentsType();
		
		// XXX[희정]: 이 부분 테스트 바람
		switch (contentsType) {
			case "STRING":
				String stringData = contents.getContentsValue();

				response.setHeader("Content-Disposition", "form-data; name=stringData" + "\"" + LINE_FEED);
				response.setContentType("text/plain; charset=UTF-8");

				sendStringData(stringData, response.getOutputStream());

				break;
			case "IMAGE":
				String imageFileName = contents.getContentsPKName();

				response.setContentType("image/jpeg");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + imageFileName + LINE_FEED);
				response.setHeader("Content-Transfer-Encoding", "binary" + "\"" + LINE_FEED);

				// dir에 있는 image file을 가져와 전송. (ByteArrayStream)
				sendFileData(imageFileName, response.getOutputStream());

				break;
			case "FILE":
				String fileName = contents.getContentsPKName();

				// response.setContentType("multipart/mixed");
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + LINE_FEED);
				response.setHeader("Content-Transfer-Encoding", "binary" + "\"" + LINE_FEED);

				// dir에 있는 file을 가져와 전송. (FileStream)
				sendFileData(fileName, response.getOutputStream());

				break;
			default:
				System.out.println("어떤 형식에도 속하지 않음.");
		}

		// responseMsgLog(response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// doGet(request, response);
	}

	/** String Data 전송 */
	public void sendStringData(String stringData, OutputStream outputStream) {
		try {

			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);

			writer.append(stringData).append(LINE_FEED);
			writer.flush();
			writer.close();

			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Captured Image Data, File Data를 전송 */
	public void sendFileData(String fileName, OutputStream outputStream) {
		// 보낼 file data를 가져오기
		File sendFileContents = new File(ROOT_LOCATION + groupPK + "\\" + fileName);

		try {
			FileInputStream inputStream = new FileInputStream(sendFileContents);
			byte[] buffer = new byte[CHUNKSIZE];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			inputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 여러 File Data를 전송(자료 조사 필요) */
	public void sendMultipartData(ArrayList<String> fileFullPathList) {
		//
		// try {
		// MultipartUtility multipart = new MultipartUtility(SERVER_URL +
		// SERVER_SERVLET, charset);
		// setCommonParameter(multipart);
		//
		// // Iterator 통한 전체 조회
		// Iterator iterator = fileFullPathList.iterator();
		//
		// // 여러 파일을 순서대로 처리
		// while (iterator.hasNext()) {
		// String fileFullPath = (String) iterator.next();
		//
		// System.out.println("fileFullPathList: " + fileFullPath);
		// System.out.println();
		//
		// // 업로드할 파일 생성
		// File uploadFile = new File(fileFullPath);
		//
		// /* uploadFilename is the name of the sequence input variable in the
		// called project the value is the name that will be given to the file
		// */
		// multipart.addFilePart("multipartFileData", uploadFile);
		// }
		//
		// List<String> response = multipart.finish();
		// System.out.println("SERVER REPLIED");
		// // responseMsgLog();
		//
		// for (String line : response) {
		// System.out.println(line);
		// }
		// } catch (IOException ex) {
		// System.err.println(ex);
		// }
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
		System.out.println("userEmail: " + request.getParameter("userEmail"));
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



//// 서버에서 groupPK로 해당 history에서 downloadDataPK인 Contents를 찾는다.
//		/* test를 위한 setting */
//		Contents testcontent = new Contents();
//
//		/* File의 경우 */
//		// testcontent.setContentsPKName("2");
//		// testcontent.setContentsSize(80451275);
//		// testcontent.setContentsType(Contents.TYPE_FILE);
//		// testcontent.setContentsValue("taeyeon.mp3");
//		// testcontent.setUploadTime("2017-4-19 날짜 10:19:34");
//		// testcontent.setUploadUserName("gmlwjd9405@naver.com");
//
//		// testcontent.setContentsPKName("3");
//		// testcontent.setContentsSize(387);
//		// testcontent.setContentsType(Contents.TYPE_FILE);
//		// testcontent.setContentsValue("bbbb.jpeg");
//		// testcontent.setUploadTime("2017-4-19 날짜 10:19:34");
//		// testcontent.setUploadUserName("gmlwjd9405@naver.com");
//
//		/* String의 경우 */
//		testcontent.setContentsPKName("1");
//		testcontent.setContentsSize(45);
//		testcontent.setContentsType(Contents.TYPE_STRING);
//		testcontent.setContentsValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
//		testcontent.setUploadTime("2017-4-19 날짜 10:53:06");
//		testcontent.setUploadUserName("gmlwjd9405@naver.com");
//
//		/* Image의 경우 */
//		// testcontent.setContentsPKName("1");
//		// testcontent.setContentsSize(4733);
//		// testcontent.setContentsType(Contents.TYPE_IMAGE);
//		// testcontent.setContentsValue("");
//		// testcontent.setUploadTime("2017-4-19 날짜 11:06:04");
//		// testcontent.setUploadUserName("gmlwjd9405@naver.com");
//
//		String contentsType = testcontent.getContentsType();
//
//		// 해당 downloadDataPK의 Contents타입을 client에 알림
//		// response.setHeader("contentsType", "");
//
//		// 해당 downloadDataPK의 Contents타입에 따라 다르게 처리(Set response Headers)




 