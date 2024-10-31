package app.lan_chat.model;

public class ChatRoom {
    private String name;
    private int port;

    public ChatRoom(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChatRoom chatRoom = (ChatRoom) obj;
        return port == chatRoom.port;
    }

    @Override
    public int hashCode() {
        return port;
    }
}
