package sprout.clipcon.server.controller;

import sprout.clipcon.server.model.User;

public class MemberAdministrator {
	public final static String CONFIRM = "confirm";
	public final static String REJECT = "reject";

	public static String getUserAuthentication(String code, String pwd) {
		System.out.println(code + pwd);
		if (code.equals("test") && pwd.equals("12")) {
			return CONFIRM;
		}
		return REJECT;
	}

	public static User getUserByEmail(String email) {
		return new User();
	}
}
