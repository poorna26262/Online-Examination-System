# Online Examination System – Java Swing

## Requirements
- JDK 8 or later
- IntelliJ IDEA, Eclipse, NetBeans, or another Java IDE

## Run
1. Open the `src` folder as a Java project or create a project and copy the `.java` files.
2. Run `Main.java`.

Or from a terminal in `src`:

```bash
javac *.java
java Main
```

## Demo login
- Username: `student`
- Password: `1234`

## Features
- Login screen with username and password
- Profile update before exam (display name and password)
- 10 sample Java MCQ questions, four options each
- One question displayed at a time
- Previous and Next navigation
- 30-minute countdown timer
- Automatic submission when timer reaches zero
- Manual submission with confirmation dialog
- Result with score, time taken, correct/incorrect breakdown, correct answers and explanations
- Window-close confirmation while an exam is active
- Logout returns to login

## Notes
This is an educational single-user demo. The demo account and profile changes exist only in memory and reset when the application closes. For production use, add persistent storage, secure password hashing, per-user accounts, and server-side exam timing.
