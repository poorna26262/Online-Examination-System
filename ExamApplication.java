import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class ExamApplication extends JFrame {
    private static final int EXAM_DURATION_SECONDS = 30 * 60;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final Student demoStudent =
            new Student("student", "1234", "Demo Student");

    private final List<Question> questions = createQuestions();
    private final int[] answers = new int[questions.size()];

    private JPanel loginPanel;
    private JPanel profilePanel;
    private JPanel examPanel;
    private JPanel resultPanel;

    private JTextField loginUsername;
    private JPasswordField loginPassword;
    private JTextField displayNameField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private JLabel questionNumberLabel;
    private JLabel timerLabel;
    private JLabel questionTextLabel;
    private final JRadioButton[] optionButtons = new JRadioButton[4];
    private final ButtonGroup optionGroup = new ButtonGroup();

    private int currentQuestion = 0;
    private int remainingSeconds = EXAM_DURATION_SECONDS;
    private boolean examStarted = false;
    private boolean examFinished = false;
    private boolean allowClose = false;
    private long examStartMillis;
    private Timer countdownTimer;

    public ExamApplication() {
        setTitle("Online Examination System");
        setSize(850, 620);
        setMinimumSize(new Dimension(700, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        initializeScreens();
        add(cards, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });

        showCard("LOGIN");
    }

    private void initializeScreens() {
        loginPanel = buildLoginPanel();
        profilePanel = buildProfilePanel();
        examPanel = buildExamPanel();

        cards.add(loginPanel, "LOGIN");
        cards.add(profilePanel, "PROFILE");
        cards.add(examPanel, "EXAM");
    }

    private JPanel buildLoginPanel() {
        JPanel panel = createCenteredPanel();
        JLabel title = heading("ONLINE EXAMINATION SYSTEM");
        panel.add(title);

        loginUsername = new JTextField(22);
        loginPassword = new JPasswordField(22);

        addFormRow(panel, "Username:", loginUsername);
        addFormRow(panel, "Password:", loginPassword);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        panel.add(loginButton);

        JLabel hint = new JLabel("Demo login: student / 1234");
        hint.setForeground(Color.GRAY);
        panel.add(hint);
        getRootPane().setDefaultButton(loginButton);
        return panel;
    }

    private JPanel buildProfilePanel() {
        JPanel panel = createCenteredPanel();
        panel.add(heading("UPDATE STUDENT PROFILE"));

        displayNameField = new JTextField(22);
        newPasswordField = new JPasswordField(22);
        confirmPasswordField = new JPasswordField(22);

        addFormRow(panel, "Display name:", displayNameField);
        addFormRow(panel, "New password:", newPasswordField);
        addFormRow(panel, "Confirm password:", confirmPasswordField);

        JButton startButton = new JButton("Save Profile & Start Exam");
        startButton.addActionListener(e -> saveProfileAndStart());
        panel.add(startButton);

        JLabel note = new JLabel("You can update your display name and password before the exam.");
        note.setForeground(Color.GRAY);
        panel.add(note);
        return panel;
    }

    private JPanel buildExamPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel top = new JPanel(new BorderLayout());
        questionNumberLabel = new JLabel();
        questionNumberLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        timerLabel = new JLabel();
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        timerLabel.setForeground(new Color(180, 30, 30));
        top.add(questionNumberLabel, BorderLayout.WEST);
        top.add(timerLabel, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        questionTextLabel = new JLabel();
        questionTextLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        questionTextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(questionTextLabel);
        center.add(Box.createVerticalStrut(25));

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 16));
            optionButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            optionGroup.add(optionButtons[i]);
            center.add(optionButtons[i]);
            center.add(Box.createVerticalStrut(12));
        }
        panel.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        JButton previousButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JButton submitButton = new JButton("Submit Exam");

        previousButton.addActionListener(e -> movePrevious());
        nextButton.addActionListener(e -> moveNext());
        submitButton.addActionListener(e -> confirmManualSubmit());

        bottom.add(previousButton);
        bottom.add(nextButton);
        bottom.add(submitButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void login() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());

        if (username.equals(demoStudent.getUsername()) && demoStudent.authenticate(password)) {
            displayNameField.setText(demoStudent.getDisplayName());
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            showCard("PROFILE");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveProfileAndStart() {
        String name = displayNameField.getText().trim();
        String password = new String(newPasswordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (name.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Please complete all profile fields.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }
        if (password.length() < 4) {
            showError("Password must contain at least 4 characters.");
            return;
        }
        if (!demoStudent.updateProfile(name, password)) {
            showError("Profile update failed.");
            return;
        }

        startExam();
    }

    private void startExam() {
        for (int i = 0; i < answers.length; i++) answers[i] = -1;
        currentQuestion = 0;
        remainingSeconds = EXAM_DURATION_SECONDS;
        examFinished = false;
        examStarted = true;
        examStartMillis = System.currentTimeMillis();

        renderQuestion();
        updateTimerLabel();

        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new Timer(1000, e -> {
            remainingSeconds--;
            updateTimerLabel();
            if (remainingSeconds <= 0) {
                countdownTimer.stop();
                submitExam(true);
            }
        });
        countdownTimer.start();
        showCard("EXAM");
    }

    private void renderQuestion() {
        Question question = questions.get(currentQuestion);
        questionNumberLabel.setText("Question " + (currentQuestion + 1)
                + " of " + questions.size());
        questionTextLabel.setText("<html><div style='width:680px'>"
                + escapeHtml(question.getText()) + "</div></html>");

        String[] options = question.getOptions();
        optionGroup.clearSelection();

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText((char) ('A' + i) + ". " + options[i]);
            optionButtons[i].setSelected(answers[currentQuestion] == i);
        }
    }

    private void saveCurrentAnswer() {
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].isSelected()) {
                answers[currentQuestion] = i;
                return;
            }
        }
        answers[currentQuestion] = -1;
    }

    private void moveNext() {
        saveCurrentAnswer();
        if (currentQuestion < questions.size() - 1) {
            currentQuestion++;
            renderQuestion();
        } else {
            JOptionPane.showMessageDialog(this, "You are on the last question.");
        }
    }

    private void movePrevious() {
        saveCurrentAnswer();
        if (currentQuestion > 0) {
            currentQuestion--;
            renderQuestion();
        } else {
            JOptionPane.showMessageDialog(this, "You are on the first question.");
        }
    }

    private void confirmManualSubmit() {
        saveCurrentAnswer();
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to submit your exam?",
                "Confirm Submission", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            submitExam(false);
        }
    }

    private void submitExam(boolean automatic) {
        if (examFinished) return;
        saveCurrentAnswer();
        examFinished = true;
        examStarted = false;
        if (countdownTimer != null) countdownTimer.stop();

        long elapsedSeconds = Math.min(EXAM_DURATION_SECONDS,
                Math.max(0, (System.currentTimeMillis() - examStartMillis) / 1000));
        if (automatic) elapsedSeconds = EXAM_DURATION_SECONDS;

        showResult(elapsedSeconds, automatic);
    }

    private void showResult(long elapsedSeconds, boolean automatic) {
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).isCorrect(answers[i])) score++;
        }

        JPanel result = new JPanel(new BorderLayout(12, 12));
        result.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = heading("EXAM RESULT");
        result.add(title, BorderLayout.NORTH);

        JPanel summary = new JPanel(new GridLayout(0, 1, 5, 5));
        summary.add(new JLabel("Student: " + demoStudent.getDisplayName()));
        summary.add(new JLabel("Score: " + score + " out of " + questions.size()));
        summary.add(new JLabel("Time taken: " + formatTime(elapsedSeconds)));
        summary.add(new JLabel(automatic ? "Status: Automatically submitted (time expired)"
                : "Status: Submitted successfully"));
        result.add(summary, BorderLayout.NORTH);

        JTextArea breakdown = new JTextArea();
        breakdown.setEditable(false);
        breakdown.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        StringBuilder report = new StringBuilder("ANSWER BREAKDOWN\n\n");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            boolean correct = q.isCorrect(answers[i]);
            report.append("Q").append(i + 1).append(": ")
                    .append(correct ? "CORRECT" : "INCORRECT").append("\n")
                    .append("Your answer: ")
                    .append(answers[i] == -1 ? "Not answered" : q.getOptions()[answers[i]])
                    .append("\nCorrect answer: ").append(q.getCorrectAnswer()).append("\n")
                    .append("Explanation: ").append(q.getExplanation()).append("\n\n");
        }
        breakdown.setText(report.toString());
        result.add(new JScrollPane(breakdown), BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(logoutButton);
        result.add(buttonPanel, BorderLayout.SOUTH);

        if (resultPanel != null) cards.remove(resultPanel);
        resultPanel = result;
        cards.add(resultPanel, "RESULT");
        showCard("RESULT");
        revalidate();
        repaint();
    }

    private void logout() {
        if (countdownTimer != null) countdownTimer.stop();
        examStarted = false;
        examFinished = false;
        loginPassword.setText("");
        loginUsername.setText("");
        showCard("LOGIN");
    }

    private void updateTimerLabel() {
        timerLabel.setText("Time Remaining: " + formatTime(remainingSeconds));
        if (remainingSeconds <= 60) {
            timerLabel.setForeground(Color.RED);
        } else {
            timerLabel.setForeground(new Color(180, 30, 30));
        }
    }

    private void handleWindowClose() {
        if (examStarted && !examFinished) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to quit? Your exam will be ended.",
                    "Quit During Exam", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        allowClose = true;
        if (countdownTimer != null) countdownTimer.stop();
        dispose();
    }

    private void showCard(String name) {
        cardLayout.show(cards, name);
    }

    private JPanel createCenteredPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        return panel;
    }

    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setBorder(new EmptyBorder(0, 0, 25, 0));
        return label;
    }

    private void addFormRow(JPanel panel, String label, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 7));
        JLabel text = new JLabel(label);
        text.setPreferredSize(new Dimension(125, 28));
        row.add(text);
        row.add(field);
        panel.add(row);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private List<Question> createQuestions() {
        List<Question> list = new ArrayList<>();
        list.add(new Question("Which keyword is used to create a class in Java?",
                new String[]{"class", "struct", "define", "new"}, 0,
                "The class keyword declares a class in Java."));
        list.add(new Question("Which method is the entry point of a Java application?",
                new String[]{"start()", "main()", "run()", "init()"}, 1,
                "The main method is the standard Java application entry point."));
        list.add(new Question("Which collection stores elements in insertion order and allows duplicates?",
                new String[]{"HashSet", "TreeSet", "ArrayList", "Map"}, 2,
                "ArrayList preserves insertion order and allows duplicate elements."));
        list.add(new Question("Which Swing component groups radio buttons?",
                new String[]{"JPanel", "ButtonGroup", "JFrame", "JLabel"}, 1,
                "ButtonGroup makes radio button selection mutually exclusive."));
        list.add(new Question("Which keyword prevents a class member from being accessed directly outside its class?",
                new String[]{"public", "static", "private", "void"}, 2,
                "private restricts direct access to the declaring class."));
        list.add(new Question("Which package contains the Swing GUI components?",
                new String[]{"java.io", "java.swing", "javax.swing", "java.awt.event"}, 2,
                "Swing components are in the javax.swing package."));
        list.add(new Question("Which loop checks its condition before each iteration?",
                new String[]{"while", "do-while", "switch", "try"}, 0,
                "A while loop checks the condition before executing its body."));
        list.add(new Question("What does JVM stand for?",
                new String[]{"Java Variable Method", "Java Virtual Machine",
                        "Java Visual Model", "Joint Virtual Memory"}, 1,
                "JVM means Java Virtual Machine."));
        list.add(new Question("Which interface is commonly used to handle button clicks in Swing?",
                new String[]{"Runnable", "ActionListener", "Serializable", "Comparable"}, 1,
                "ActionListener handles action events such as button clicks."));
        list.add(new Question("Which keyword is used to inherit a class in Java?",
                new String[]{"implements", "inherits", "extends", "super"}, 2,
                "The extends keyword declares class inheritance."));
        return list;
    }
}
