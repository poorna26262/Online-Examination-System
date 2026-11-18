public class Question {
    private final String text;
    private final String[] options;
    private final int correctIndex;
    private final String explanation;

    public Question(String text, String[] options, int correctIndex, String explanation) {
        if (options == null || options.length != 4 || correctIndex < 0 || correctIndex > 3) {
            throw new IllegalArgumentException("Each question must have exactly 4 options and a valid answer.");
        }
        this.text = text;
        this.options = options.clone();
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }

    public String getText() {
        return text;
    }

    public String[] getOptions() {
        return options.clone();
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctIndex;
    }

    public String getCorrectAnswer() {
        return options[correctIndex];
    }

    public String getExplanation() {
        return explanation;
    }
}
