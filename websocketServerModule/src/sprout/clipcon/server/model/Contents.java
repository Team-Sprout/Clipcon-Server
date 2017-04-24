package sprout.clipcon.server.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Contents {
	public static String TYPE_STRING = "STRING";
	public static String TYPE_IMAGE = "IMAGE";
	public static String TYPE_FILE = "FILE";

	private String contentsType;
	private long contentsSize;

	// 그룹내의 각 Data를 구분하는 고유키값
	private static int contentsPKValue = 0;
	public String contentsPKName;

	private String uploadUserName;
	private String uploadTime;

	// String Type: String값, File Type: FileOriginName
	private String contentsValue;
	
	public Contents(String type, String userEmail, String time, long size) {
		this();
		this.contentsType = type;
		this.uploadUserName = userEmail;
		this.uploadTime = time;
		this.contentsSize = size;
	}

	/** 생성 시 고유키값을 할당한다. */
	public Contents() {
		this.contentsPKName = Integer.toString(++contentsPKValue);
		System.out.println("Contents 생성자 호출, ++contentsPKValue");
	}
}
