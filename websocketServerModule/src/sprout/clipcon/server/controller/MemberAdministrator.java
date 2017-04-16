package sprout.clipcon.server.controller;

import sprout.clipcon.server.model.User;

public class MemberAdministrator {
	public final static String CONFIRM = "confirm";
	public final static String REJECT = "reject";

	public static String getUserAuthentication(String code) {
		return CONFIRM;
	}

	public static User getUserByEmail(String email) {
		return new User(email, "", "");
	}
}
