// src/main/java/app/lan_chat/MessageUtils.java

package app.lan_chat;

import app.lan_chat.model.ExitMessage;
import app.lan_chat.model.Message;
import app.lan_chat.model.TextMessage;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class MessageUtils {
    private static final Gson gson = Converters.registerOffsetDateTime(new GsonBuilder()).create();

    ;

    /**
     * Serializes a Message object to a JSON string.
     * @param message The Message object to serialize.
     * @return The JSON string representation of the message.
     */
    public static String serializeMessage(Message message) {
        return gson.toJson(message);
    }

    /**
     * Attempts to deserialize a JSON string into a TextMessage.
     * @param jsonMessage The JSON string to deserialize.
     * @return The TextMessage object if the JSON represents a TextMessage, null otherwise.
     */
    public static TextMessage deserializeTextMessage(String jsonMessage) {
        try {
            return gson.fromJson(jsonMessage, TextMessage.class);
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to parse message as TextMessage: " + e.getMessage());
            return null;
        }
    }

    /**
     * Attempts to deserialize a JSON string into an ExitMessage.
     * @param jsonMessage The JSON string to deserialize.
     * @return The ExitMessage object if the JSON represents an ExitMessage, null otherwise.
     */
    public static ExitMessage deserializeExitMessage(String jsonMessage) {
        try {
            return gson.fromJson(jsonMessage, ExitMessage.class);
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to parse message as ExitMessage: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a Message object is of type TextMessage.
     * @param message The Message object to check.
     * @return true if the message is a TextMessage, false otherwise.
     */
    public static boolean isTextMessage(Message message) {
        return message instanceof TextMessage;
    }

    /**
     * Deserializes a JSON string to the appropriate Message subclass based on its content.
     * @param jsonMessage The JSON string to deserialize.
     * @return The appropriate Message subclass object, or null if parsing fails.
     */
    public static Message deserializeMessage(String jsonMessage) {
        if (jsonMessage.contains("\"text\"")) {  // Simple heuristic check for TextMessage
            return deserializeTextMessage(jsonMessage);
        } else if (jsonMessage.contains("\"exit\"")) {
            return deserializeExitMessage(jsonMessage);
        }
        return null;
    }
}
