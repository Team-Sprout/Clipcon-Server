package sprout.clipcon.server.model;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import sprout.clipcon.server.controller.UserController;

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

	// String Type: String값, (single) File Type: FileOriginName
	private String contentsValue = "";

	private int primaryKey = 0;

	public Contents(String type, String userName, String time, long size) {
		this.contentsType = type;
		this.uploadUserName = userName;
		this.uploadTime = time;
		this.contentsSize = size;
		
		// System.out.println("Contents Constructor");
	}
}
