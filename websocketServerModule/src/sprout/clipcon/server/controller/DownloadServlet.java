package sprout.clipcon.server.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

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

	private Server server = Server.getInstance();

	// root location where group folder exists
	private final String ROOT_LOCATION = Server.RECEIVE_LOCATION;

	private static final int CHUNKSIZE = 4096;
	private static final String LINE_FEED = "\r\n";
	private String charset = "UTF-8";

	private String userName = null;
	private String groupPK = null;
	private String downloadDataPK = null;

	/** Constructor UploadServlet */
	public DownloadServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("================================================================\ndoGet START");

		// get Request Data
		userName = request.getParameter("userName");
		groupPK = request.getParameter("groupPK");
		downloadDataPK = request.getParameter("downloadDataPK");

		System.out.println("<<Parameter>>\n userName: " + userName + ", groupPK: " + groupPK + ", downloadDataPK: "
				+ downloadDataPK + "\n");

		
		Group group = server.getGroupByPrimaryKey(groupPK);
		
		Contents contents = group.getContents(downloadDataPK);
		String contentsType = contents.getContentsType();

		switch (contentsType) {
		case Contents.TYPE_STRING:
			String stringData = contents.getContentsValue();
			long stringSize = 0;

			response.setContentType("text/plain; charset=UTF-8");
			response.setHeader("Content-Length", stringSize + LINE_FEED);
			response.setHeader("Content-Disposition", "form-data; name=stringData" + "\"" + LINE_FEED);

			sendStringData(stringData, response.getOutputStream());
			break;

		case Contents.TYPE_IMAGE:
			String imageFileName = contents.getContentsPKName();
			long imageSize = contents.getContentsSize();

			response.setContentType("image/png");
			response.setHeader("Content-Length", imageSize + LINE_FEED);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + imageFileName + LINE_FEED);
			response.setHeader("Content-Transfer-Encoding", "binary" + "\"" + LINE_FEED);
			// Transfer the image file data in the directory. (ByteArrayStream)
			sendFileData(imageFileName, response.getOutputStream());
			break;

		case Contents.TYPE_FILE:
			String fileName = contents.getContentsPKName();
			long fileSize = contents.getContentsSize();

			setHeaderForSendingFile(fileName, fileSize, response);
			// Transfer the file data in the directory. (FileStream)
			sendFileData(fileName, response.getOutputStream());
			break;

		case Contents.TYPE_MULTIPLE_FILE:
			String multipleFileName = contents.getContentsPKName();
			long multipleFileSize = contents.getContentsSize();

			setHeaderForSendingFile(multipleFileName, multipleFileSize, response);
			// Transfer the zip(multiple) file data in the directory. (FileStream)
			sendFileData(multipleFileName, response.getOutputStream());
			break;

		default:
			System.out.println("<<DOWNLOAD SERVLET>> It does not belong to any format.");
		}
		// TmpLog.responseMsgLog(response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// doGet(request, response);
	}

	/** Setting Header Info For Sending File and Multiple File */
	public void setHeaderForSendingFile(String fileName, long fileSize, HttpServletResponse response) {
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Length", fileSize + LINE_FEED);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + LINE_FEED);
		response.setHeader("Content-Transfer-Encoding", "binary" + "\"" + LINE_FEED);
	}

	/** Send String Data */
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

	/** Send Captured Image Data, File Data and Multiple Data */
	public void sendFileData(String fileName, OutputStream outputStream) {
		// Get file data to send to client
		File sendFileContents = new File(ROOT_LOCATION + groupPK + File.separator + fileName);

		try {
			FileInputStream inputStream = new FileInputStream(sendFileContents);
			byte[] buffer = new byte[CHUNKSIZE];
			int bytesRead = -1;
			System.out.println("[DEBUG] delf: byte size: " + buffer.length);
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			inputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
