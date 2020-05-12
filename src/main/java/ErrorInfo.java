public class ErrorInfo {
    private String message;
    private int index;

    public ErrorInfo(String message, int index) {
        this.message = message;
        this.index = index;
    }

    public String getMessage() {
        return message;
    }

    public int getIndex() {
        return index;
    }
}
