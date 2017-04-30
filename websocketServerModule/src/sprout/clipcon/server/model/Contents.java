package sprout.clipcon.server.model;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sprout.clipcon.server.controller.UserController;

@Getter
@Setter
@ToString
public class Contents {
	public final static String TYPE_STRING = "STRING";
	public final static String TYPE_IMAGE = "IMAGE";
	public final static String TYPE_FILE = "FILE";

	private String contentsType;
	private long contentsSize;
	private String contentsPKName;
	private String uploadUserName;
	private String uploadTime;

	// String Type: String°ª, File Type: FileOriginName
	private String contentsValue;

	private static int primaryKey = 0;
	
	private Map<String, String[]> filePaths;
	public static final int FILE_PATH = 0;
	public static final int FILE_NAME = 1;

	public Contents(String type, String userEmail, String time, long size) {
		this.contentsType = type;
		this.uploadUserName = userEmail;
		this.uploadTime = time;
		this.contentsSize = size;

		if (contentsType.equals(TYPE_FILE)) {
			filePaths = Collections.synchronizedMap(new HashMap<String, String[]>());
		}
	}

	public String addFilePath(String path, String origineFileName) {
		String key = Integer.toString(++primaryKey);
		String[] tmp = { path, origineFileName };
		filePaths.put(key, tmp);
		return key;
	}

	public String getFilePath(String key) {
		return filePaths.get(key)[FILE_PATH];
	}

	public String getFileName(String key) {
		return filePaths.get(key)[FILE_NAME];
	}

	public String getFilePathAndName(String key) {
		return (getFilePath(key) + File.pathSeparator + getFileName(key));
	}
}
