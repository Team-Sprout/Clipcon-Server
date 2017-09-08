package sprout.clipcon.server.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import sprout.clipcon.server.model.Group;

public class Server {
	private static Server uniqueInstance;
	/** All groups on the server */
	private Map<String, Group> groups = Collections.synchronizedMap(new HashMap<String, Group>());

	public static final String SERVER_ROOT_LOCATION = System.getProperty("user.home") + File.separator + "Desktop"+ File.separator;

	// directory of download files
	public static final String RECEIVE_LOCATION = SERVER_ROOT_LOCATION + "clipcon_download";
	
	public static final String LATEST_WINDOWS_CLIENT_VERSION = "1.1";
	public static final String LATEST_ADNROID_CLIENT_VERSION = "1.1";
	
	// change source
	private Server() {
		System.out.println("Construct Server >>>");

		File initReceiveLocation = new File(RECEIVE_LOCATION);

		if (!initReceiveLocation.exists()) {
			initReceiveLocation.mkdir(); // Create Directory
		}
	}

	public static Server getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new Server();
		}
		return uniqueInstance;
	}

	public Group getGroupByPrimaryKey(String key) {

		Set<String> set = groups.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			System.out.print(it.next() + ", ");
		}
		System.out.println();

		Group targetGroup = groups.get(key);
		if (targetGroup != null) {
		}
		return targetGroup;
	}

	public Group createGroup() {
		String groupKey = generatePrimaryKey(5);
		System.out.println("group key: " + groupKey);

		Group newGroup = new Group(groupKey);
		groups.put(groupKey, newGroup);
		return newGroup;
	}

	/** If no user in group, remove this group at list and remote directory(file download dir). */
	public void destroyGroup(String groupPrimaryKey) {
		groups.remove(groupPrimaryKey);

		deleteAllFilesInGroupDir(RECEIVE_LOCATION + File.separator + groupPrimaryKey);
	}

	/** Delete all files in group directory */
	public void deleteAllFilesInGroupDir(String parentDirPath) {
		// Get the files in the folder into an array.
		File file = new File(parentDirPath);

		if (file.exists()) {
			File[] tempFile = file.listFiles();

			if (tempFile.length > 0) {
				for (int i = 0; i < tempFile.length; i++) {
					if (tempFile[i].isFile()) {
						tempFile[i].delete();
					}
					else { // Recursive function
						deleteAllFilesInGroupDir(tempFile[i].getPath());
					}
					tempFile[i].delete();
				}
				file.delete();
			}
		}
	}

	public void removeGroup(Group group) {
		Group removeGroup = groups.remove(group.getPrimaryKey());
		if (removeGroup != null) {
			deleteAllFilesInGroupDir(removeGroup.getPrimaryKey());
		}
	}

	/**
	 * generate random String
	 * 
	 * @param length
	 *            length of String
	 * @return string generated randomly 
	 */
	private String generatePrimaryKey(int length) {
		StringBuffer temp = new StringBuffer();
		Random rnd = new Random();
		// for (int i = 0; i < length; i++) {
		// int rIndex = rnd.nextInt(2) + 1; 
		// switch (rIndex) {
		// // case 0:
		// // temp.append((char) ((int) (rnd.nextInt(26)) + 65));
		// // bre;
		// case 1:
		// temp.append((char) ((int) (rnd.nextInt(26)) + 97));
		// break;
		// case 2:
		// temp.append((rnd.nextInt(10)));
		// break;
		// }
		// }
		//
		// int rindex = rnd.nextInt(length) + 1;

		for (int i = 0; i < 2; i++) {
			temp.append((char) ((int) (rnd.nextInt(26)) + 97));
		}
		for (int i = 0; i < length - 2; i++) {
			temp.append((rnd.nextInt(10)));
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
				}
				else if (file.isDirectory()) {
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
/*
	class ServerStatusCheck extends Thread {

		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			String input = null;
			while (true) {
				input = sc.nextLine();
				switch (input) {
				case "g":
					System.out.println("┌──────────────SERVER STATUS");
					System.out.println("├ Group Count: " + groups.size());
					System.out.println("├ Group List...  ");
					Set<String> set = groups.keySet();
					Iterator<String> it = set.iterator();
					while (it.hasNext()) {
						System.out.println("│\t- " + it.next());
					}
					break;

				default:
					Group search = groups.get(input);
					if (search == null) {
						System.out.println("[SERVER] Group named \"" + input + "\" is not found.");
					}
					else {
						List<String> list = search.getUserList();
						System.out.println("┌──────────────GROUP INFO: " + input);
						System.out.println("├ User Count: " + list.size());
						System.out.println("├ User List...  ");
						Iterator<String> listIt = list.iterator();
						while (listIt.hasNext()) {
							System.out.println("│\t- " + listIt.next());
						}
					}
					break;
				}
				System.out.println("└───────────────────────────");
				input = null;
			}
		}
	}
	*/
}
