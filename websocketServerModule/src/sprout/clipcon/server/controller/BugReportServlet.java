package sprout.clipcon.server.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Servlet implementation class DownloadServlet
 */
@MultipartConfig(maxFileSize = 1024 * 1024 * 10, fileSizeThreshold = 1024 * 1024, maxRequestSize = 1024 * 1024 * 10)
@WebServlet("/bugreport")
public class BugReportServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final String outputFileName = "bugReport.txt";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		String page = "/bugreport2.jsp";
		
		RequestDispatcher dispatcher = request.getRequestDispatcher(page);
		dispatcher.forward(request, response);
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		

		/* Gets the bug message entered by the client */
		Part part = request.getPart("bugMessage");
		InputStream is = part.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder stringBuilder = new StringBuilder();
		String bugMessage = null;

		try {
			while ((bugMessage = bufferedReader.readLine()) != null) {
				stringBuilder.append(bugMessage + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		bugMessage = stringBuilder.toString();
		
		/* Set up the entire message to leave a bug report */
		String bugReport = "";
		// String bugMessage = request.getParameter("bugMessage"); // encoding problem
		bugReport += bugMessage + ", ";
		bugReport += UploadServlet.uploadTime() + ", ";
		if (request.getHeader("User-Agent").equals("pcProgram")) {
			bugReport += "pcProgram";
		} else {
			bugReport += "androidProgram";
		}

		System.out.println("[BugReportServlet] bugReport: " + bugReport);

		try {
			FileWriter fw = new FileWriter(Server.RECEIVE_LOCATION + File.separator + outputFileName, true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(bugReport);
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		response.sendRedirect("/globalclipboard");
	}
}
