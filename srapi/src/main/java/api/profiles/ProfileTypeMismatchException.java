package api.profiles;

public class ProfileTypeMismatchException extends Exception {
	private static final long serialVersionUID = 1L;

	public ProfileTypeMismatchException(ProfileType needed) {
		super("Only " + needed + " can do this");
	}

}
