package sprout.clipcon.server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
//@Setter
@NoArgsConstructor
public class Contents {
	public final static String TYPE_STRING = "STRING";
	public final static String TYPE_IMAGE = "IMAGE";
	public final static String TYPE_FILE = "FILE";
	public final static String TYPE_MULTIPLE_FILE = "MULTIPLE_FILE";

	private String contentsType;
	private long contentsSize;
	@Setter
	private String contentsPKName;
	private String uploadUserName;
	private String uploadTime;

	// String Type: String object value, (single) File Type: FileOriginName
	@Setter
	private String contentsValue = "";

	private int primaryKey = 0;

	public Contents(String type, String userName, String time, long size) {
		this.contentsType = type;
		this.uploadUserName = userName;
		this.uploadTime = time;
		this.contentsSize = size;
	}
}
