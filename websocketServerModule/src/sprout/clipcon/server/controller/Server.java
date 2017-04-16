package sprout.clipcon.server.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sprout.clipcon.server.model.Group;
import sprout.clipcon.server.model.User;

public class Server {
	private static Server uniqueInstance;
	private MemberAdministrator MemberAdministrator = new MemberAdministrator();
	private Map<String, Group> groups = Collections.synchronizedMap(new HashMap<String, Group>());	// 서버 내 존재한는 그룹
	private Set<User> userOnLobby = Collections.synchronizedSet(new HashSet<User>());				// 그룹에 참여하지 않은 접속 중인 사용자

	private Server() {
	}

	public static Server getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new Server();
		}
		return uniqueInstance;
	}

	/**
	 * 사용자가 서버에 입장 
	 * @param user 서버에 입장한 사용자 */
	public void enterUserInLobby(User user) {
		userOnLobby.add(user);
	}

	public void exitUSerAtLobby(User user) {
		// TODO: static?
		userOnLobby.remove(user);
	}

	// public void

	/**
	 * 해당 그룹에 사용자 추가
	 * @param key 그룹 고유 키
	 * @param user 그룹에 입장 할 사용자 
	 * @return 그룹의 존재 여부. 그룹이 존재하지 않으면 false, 존재하고 사용자가 정상적으로 그룹에 추가 됐으면 true. */
	public Group getGroupByPrimaryKey(String key) {
		Group targetGroup = groups.get(key);
		if (targetGroup != null) {
		}
		return targetGroup;
	}

	public Group createGroup(String name) {
		String groupKey = generatePrimaryKey();
		return groups.put(groupKey, new Group(groupKey, name));
	}

	private String generatePrimaryKey() {
		return "";
	}
}
