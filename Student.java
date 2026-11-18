public class Student {
    private final String username;
    private String password;
    private String displayName;

    public Student(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean authenticate(String enteredPassword) {
        return password.equals(enteredPassword);
    }

    public boolean updateProfile(String newDisplayName, String newPassword) {
        if (newDisplayName == null || newDisplayName.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        displayName = newDisplayName.trim();
        password = newPassword;
        return true;
    }
}
