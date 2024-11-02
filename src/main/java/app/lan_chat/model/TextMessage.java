package app.lan_chat.model;

import java.time.format.DateTimeFormatter;

public class TextMessage extends Message {
    private final String text;

    public TextMessage(String sender, String text) {
        super(sender);
        this.text = text;
    }

    @Override
    public String toString() {
        return '[' + timestamp.format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + sender + ": " + text;
    }
}
