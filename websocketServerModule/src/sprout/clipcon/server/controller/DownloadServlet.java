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

import lombok.NoArgsConstructor;
import sprout.clipcon.server.model.Contents;
import sprout.clipcon.server.model.Group;

/**
 * Servlet implementation class DownloadServlet
 */
@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	// root location where group folder exists
	private final String ROOT_LOCATION = Server.RECEIVE_LOCATION + File.separator;
	private final String LINE_FEED = "\r\n";
	private final String charset = "UTF-8";

	private Server server = Server.getInstance();

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("===================================================================\ndoGet START");

		// get Request Data
		String userName = request.getParameter("userName");
		String groupPK = request.getParameter("groupPK");
		String downloadDataPK = request.getParameter("downloadDataPK");

		System.out.println("<<Parameter>>\n userName: " + userName + ", groupPK: " + groupPK + ", downloadDataPK: " + downloadDataPK + "\n");

		Group group = server.getGroupByPrimaryKey(groupPK);

		Contents contents = group.getContents(downloadDataPK);
		String contentsType = contents.getContentsType();

		switch (contentsType) {
		case Contents.TYPE_STRING:
			String stringData = contents.getContentsValue();

			response.setContentType("text/plain; charset=UTF-8");
			response.setHeader("Content-Disposition", "form-data; name=stringData" + "\"" + LINE_FEED);

			sendStringData(stringData, response.getOutputStream());
			break;

		case Contents.TYPE_IMAGE:
			String imageFileName = contents.getContentsPKName();

			setHeaderForSendingFile("image/png", imageFileName, response);

			// Get file data to send to client
			File sendImageFileContents = new File(ROOT_LOCATION + groupPK + File.separator + imageFileName);
			response.setContentLengthLong(sendImageFileContents.length());

			// Transfer the image file data in the directory. (ByteArrayStream)
			sendFileData(sendImageFileContents, groupPK, response.getOutputStream());
			break;

		case Contents.TYPE_FILE:
			String fileName = contents.getContentsPKName();

			setHeaderForSendingFile("application/octet-stream", fileName, response);

			// Get file data to send to client
			File sendFileContents = new File(ROOT_LOCATION + groupPK + File.separator + fileName);
			response.setContentLengthLong(sendFileContents.length());

			// Transfer the file data in the directory. (FileStream)
			sendFileData(sendFileContents, groupPK, response.getOutputStream());
			break;

		case Contents.TYPE_MULTIPLE_FILE:
			String multipleFileName = contents.getContentsPKName();

			setHeaderForSendingFile("application/octet-stream", multipleFileName, response);

			// Get file data to send to client
			File sendMultipleFileContents = new File(ROOT_LOCATION + groupPK + File.separator + multipleFileName);
			response.setContentLengthLong(sendMultipleFileContents.length());

			// Transfer the zip(multiple) file data in the directory. (FileStream)
			sendFileData(sendMultipleFileContents, groupPK, response.getOutputStream());
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
	public void setHeaderForSendingFile(String contentType, String fileName, HttpServletResponse response) {
		response.setContentType(contentType);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + LINE_FEED);
		response.setHeader("Content-Transfer-Encoding", "binary" + "\"" + LINE_FEED);
	}

	/** Send String Data */
	public void sendStringData(String stringData, OutputStream outputStream) {
		try {
			System.out.println("[DownloadServlet] String Data Content: " + stringData);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);

			writer.append(stringData).append(LINE_FEED);
			writer.flush();
			writer.close();

			outputStream.close();
			System.out.println("[DownloadServlet] Close");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Send Captured Image Data, File Data and Multiple Data */
	public void sendFileData(File sendFileContents, String groupPK, OutputStream outputStream) {
		try {
			FileInputStream inputStream = new FileInputStream(sendFileContents);

			byte[] buffer = new byte[0xFFFF];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				System.out.println("[sendFileData] bytesRead size: " + bytesRead);
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			inputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
