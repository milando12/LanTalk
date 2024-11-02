package app.lan_chat.model;

public class ExitMessage extends Message {
    private boolean exit;

    public ExitMessage(String sender, boolean exit) {
        super(sender);
        this.exit = exit;
    }

    @Override
    public String toString() {
        return "\"" + sender + "\" has left the chat.";
    }
}