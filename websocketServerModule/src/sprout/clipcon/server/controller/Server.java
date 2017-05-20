package sprout.clipcon.server.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import sprout.clipcon.server.model.Group;

public class Server {
	private static Server uniqueInstance;
	/** All groups on the server */
	private Map<String, Group> groups = Collections.synchronizedMap(new HashMap<String, Group>());

	// 업로드 파일을 저장할 위치
	public static final String RECEIVE_LOCATION = "C:\\Users\\Administrator\\Desktop\\"; // TEST PATH 2
//	public static final String RECEIVE_LOCATION = "C:\\Users\\delf\\Desktop\\"; //

	// change source
	private Server() {
	}

	public static Server getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new Server();
		}
		return uniqueInstance;
	}

	/**
	 * 해당 그룹에 사용자 추가
	 * 
	 * @param key
	 *            그룹 고유 키
	 * @return 그룹의 존재 여부. 그룹이 존재하면 true, 그렇지 않으면 false
	 */
	public Group getGroupByPrimaryKey(String key) {
		Group targetGroup = groups.get(key);
		if (targetGroup != null) {
		}
		return targetGroup;
	}

	/**
	 * 그룹 생성 후 서버의 그룹 목록에 추가
	 * 
	 * @return 생성된 그룹 객체
	 */
	public Group createGroup() {
		// [희정] debug mode for download test
		String groupKey = generatePrimaryKey(5);
		// String groupKey = "abcABC";
		System.out.println("할당된 그룹 키는 " + groupKey);
		Group newGroup = new Group(groupKey);
		groups.put(groupKey, newGroup);
		return newGroup;
	}

	/** 그룹 안의 모든 사용자가 나가면 그룹 목록에서 그룹 삭제 후 dir 삭제*/
	public void destroyGroup(String groupPrimaryKey) {
		groups.remove(groupPrimaryKey);
		
		deleteAllFilesInGroupDir(RECEIVE_LOCATION + groupPrimaryKey);
	}

	/** Delete all files in group directory */
	public void deleteAllFilesInGroupDir(String parentDirPath) {
		// Get the files in the folder into an array.
		File file = new File(parentDirPath);
		
		if(file.exists()){
			File[] tempFile = file.listFiles();

			if (tempFile.length > 0) {
				for (int i = 0; i < tempFile.length; i++) {
					if (tempFile[i].isFile()) {
						tempFile[i].delete();
					} else { // Recursive function
						deleteAllFilesInGroupDir(tempFile[i].getPath());
					}
					tempFile[i].delete();
				}
				file.delete();
			}
		}
	}

	/**
	 * 영어 대문자, 소문자, 숫자로 혼합된 문자열 생성.
	 * 
	 * @param length
	 *            생성될 문자열 길이
	 * @return 생성된 문자열
	 */
	private String generatePrimaryKey(int length) {
		StringBuffer temp = new StringBuffer();
		Random rnd = new Random();
		for (int i = 0; i < length; i++) {
			int rIndex = rnd.nextInt(2) + 1;
			switch (rIndex) {
			// case 0:
			// temp.append((char) ((int) (rnd.nextInt(26)) + 65));
			// bre;
			case 1:
				temp.append((char) ((int) (rnd.nextInt(26)) + 97));
				break;
			case 2:
				temp.append((rnd.nextInt(10)));
				break;
			}
		}
		return temp.toString();
	}

	/** the method for test and debug. */
	public static void subDirList(String source) {
		File dir = new File(source);
		File[] fileList = dir.listFiles();
		try {
			for (int i = 0; i < fileList.length; i++) {
				File file = fileList[i];
				if (file.isFile()) {
					System.out.println("File name = " + file.getPath());
				} else if (file.isDirectory()) {
					System.out.println("Dir name = " + file.getPath());
					subDirList(file.getCanonicalPath().toString());
				}
			}
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, a hh:mm:ss");
		System.out.println(sdf.format(date).toString());
	}
}
