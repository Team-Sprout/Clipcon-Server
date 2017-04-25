package sprout.clipcon.server.controller;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import sprout.clipcon.server.model.Group;

public class Server {
	private static Server uniqueInstance;
	/** 서버 내 존재하는 그룹들 */
	private Map<String, Group> groups = Collections.synchronizedMap(new HashMap<String, Group>());

	private Server() {
		Group testGroup = new Group("godoy");
		groups.put("godoy", testGroup);

		new Thread(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				while (true) {
					System.out.println(Thread.currentThread().getName() + " " + i++);
				}
			}
		});// .start();
	}

	public static Server getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new Server();
		}
		return uniqueInstance;
	}

	/**
	 * 해당 그룹에 사용자 추가
	 * @param key 그룹 고유 키
	 * @return 그룹의 존재 여부. 그룹이 존재하면 true, 그렇지 않으면 false */
	public Group getGroupByPrimaryKey(String key) {
		Group targetGroup = groups.get(key);
		if (targetGroup != null) {
		}
		return targetGroup;
	}

	/**
	 * 그룹 생성 후 서버의 그룹 목록에 추가 
	 * @return 생성된 그룹 객체 */
	public Group createGroup() {
		String groupKey = generatePrimaryKey(5);
		System.out.println("할당된 그룹 키는 " + groupKey);
		Group newGroup = new Group(groupKey);
		groups.put(groupKey, newGroup);
		return newGroup;
	}

	/**
	 * 영어 대문자, 소문자, 숫자로 혼합된 문자열 생성.
	 * @param length 생성될 문자열 길이 
	 * @return 생성된 문자열 */
	private String generatePrimaryKey(int length) {
		StringBuffer temp = new StringBuffer();
		Random rnd = new Random();
		for (int i = 0; i < length; i++) {
			int rIndex = rnd.nextInt(3);
			switch (rIndex) {
			case 0:
				temp.append((char) ((int) (rnd.nextInt(26)) + 97));
				break;
			case 1:
				temp.append((char) ((int) (rnd.nextInt(26)) + 65));
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
					System.out.println("파일 이름 = " + file.getPath());
				} else if (file.isDirectory()) {
					System.out.println("디렉 이름 = " + file.getPath());
					subDirList(file.getCanonicalPath().toString());
				}
			}
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) {
		subDirList("C:\\Users\\delf\\Desktop\\dev");
	}
}
