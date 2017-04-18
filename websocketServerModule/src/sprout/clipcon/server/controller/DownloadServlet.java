package sprout.clipcon.server.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DownloadServlet
 */
@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String userEmail = null;
	private String groupPK = null;
	private String downloadData = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DownloadServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// get Request Data
		downloadData = request.getParameter("downloadData");

		// 서버

		// Copy it to response's OutputStream

		// Set response Headers
		response.setContentType("multipart/mixed");
		response.setHeader("Content-Disposition", "attachment; filename=");

		//
		// // Set the response type and specify the boundary string
		// response.setContentType("multipart/x-mixed-replace;boundary=END");
		//
		// // Set the content type based on the file type you need to download
		// String contentType = "Content-type: text/rtf";
		//
		// // List of files to be downloaded
		// List files = new ArrayList();
		// files.add(new File("C:/first.txt"));
		// files.add(new File("C:/second.txt"));
		// files.add(new File("C:/third.txt"));
		//
		// ServletOutputStream out = response.getOutputStream();
		//
		// // Print the boundary string
		// out.println();
		// out.println("--END");
		//
		// for (File file : files) {
		//
		// // Get the file
		// FileInputStream fis = null;
		// try {
		// fis = new FileInputStream(file);
		//
		// } catch (FileNotFoundException fnfe) {
		// // If the file does not exists, continue with the next file
		// System.out.println("Couldfind file " + file.getAbsolutePath());
		// continue;
		// }
		//
		// BufferedInputStream fif = new BufferedInputStream(fis);
		//
		// // Print the content type
		// out.println(contentType);
		// out.println("Content-Disposition: attachment; filename=" +
		// file.getName());
		// out.println();
		//
		// System.out.println("Sending " + file.getName());
		//
		// // Write the contents of the file
		// int data = 0;
		// while ((data = fif.read()) != -1) {
		// out.write(data);
		// }
		// fif.close();
		//
		// // Print the boundary string
		// out.println();
		// out.println("--END");
		// out.flush();
		// System.out.println("Finisheding file " + file.getName());
		// }
		//
		// // Print the ending boundary string
		// out.println("--END--");
		// out.flush();
		// out.close();

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// doGet(request, response);
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
