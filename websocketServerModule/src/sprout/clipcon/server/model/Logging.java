package sprout.clipcon.server.model;

import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Logging {
	/** Print request Msg */
	public static void requestMsgLog(HttpServletRequest request) {

		/* request startline info */
		System.out.println("==================STARTLINE==================");
		System.out.println("Request Method: " + request.getMethod());
		System.out.println("Request RequestURI: " + request.getRequestURI());
		System.out.println("Request Protocol: " + request.getProtocol());

		/* server request header info */
		System.out.println("===================HEADER====================");
		Enumeration<String> headerNames = request.getHeaderNames();

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

	/** send response message to client */
	public static void responseMsgLog(HttpServletResponse response) {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		System.out.println("Http Post Response: " + response.toString());

		/* response status info */
		System.out.println("==================STARTLINE==================");
		System.out.println("Response Status: " + response.getStatus());
		System.out.println("Response ContentType: " + response.getContentType());

		/* response header info */
		System.out.println("==================HEADER=====================");
		Collection<String> headerNames = response.getHeaderNames();

		while (!headerNames.isEmpty()) {
			String headerName = (String) headerNames.toString();

			System.out.println(headerName + ": " + response.getHeader(headerName));
		}
		System.out.println("===================ENTITY====================");
	}
}
