package sprout.clipcon.server.model;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
	public final static String TYPE_MULTIPLE_FILE = "MULTIPLE_FILE";

	private String contentsType;
	private long contentsSize;
	private String contentsPKName;
	private String uploadUserName;
	private String uploadTime;

	private String contentsValue = "";
	//  String Type: String값, (single) File Type: FileOriginName

	private int primaryKey = 0;
	
	private Map<String, String[]> filePaths;
	public static final int FILE_PATH = 0;
	public static final int FILE_NAME = 1;

	public Contents() {
		System.out.println("\n디폴트 생성자 드루옴~~~");
	}
	
	public Contents(String type, String userEmail, String time, long size) {
		this.contentsType = type;
		this.uploadUserName = userEmail;
		this.uploadTime = time;
		this.contentsSize = size;
		
		System.out.println("\n생성자 드루옴~~~");

		if (contentsType.equals(TYPE_MULTIPLE_FILE)) {
			filePaths = Collections.synchronizedMap(new HashMap<String, String[]>());
			System.out.println("TYPE_MULTIPLE_FILE인 생성자 드루옴~~~");
		}
	}

	/** @param path 상대경로
	 * @param origineFileName 파일명 */
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
		return (getFilePath(key) + File.separator + getFileName(key));
	}

	/** TEST */
	public void printAllFileInfo() {
		if (filePaths != null) {
			Iterator<String> itr = filePaths.keySet().iterator();
			System.out.println("------------------------------------Contents 안의 파일 내용 출력");

			while (itr.hasNext()) {
				String key = itr.next();
				String[] tmp = filePaths.get(key);
				System.out.println("key: " + key + ", value: " + tmp[0] + "||" + tmp[1]);
			}
		}
	}
}
