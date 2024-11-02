package app.lan_chat.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public abstract class Message {
    protected final OffsetDateTime timestamp;
    protected final String sender;

    public Message(String sender) {
        this.timestamp = OffsetDateTime .now();
        this.sender = sender;
    }

    public OffsetDateTime  getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }
}
