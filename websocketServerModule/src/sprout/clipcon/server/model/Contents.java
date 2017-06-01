package sprout.clipcon.server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
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
	private String multipleFileListInfo; // 도연 추가

	// String Type: String object value, (single) File Type: FileOriginName
	private String contentsValue = "";

	private int primaryKey = 0;

	public Contents(String type, String userName, String time, long size) {
		this.contentsType = type;
		this.uploadUserName = userName;
		this.uploadTime = time;
		this.contentsSize = size;
	}
	
	// 도연 추가
		public Contents(String type, String userName, String time, long size, String multipleFileListInfo) {
			this(type, userName, time, size);
			this.multipleFileListInfo = multipleFileListInfo;
		}
}
