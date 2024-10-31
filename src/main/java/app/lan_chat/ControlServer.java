package app.lan_chat;

import app.lan_chat.model.ChatRoom;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/control")
public class ControlServer {
    private final Set<ChatRoom> chatRooms = new HashSet<>();

    // add few mock chat rooms for testing
    public ControlServer() {
        chatRooms.add(new ChatRoom("Room 1", 5000));
        chatRooms.add(new ChatRoom("Room 2", 5001));
        chatRooms.add(new ChatRoom("Room 3", 5002));
    }

    // GET method to list all available chat rooms
    @GetMapping("/rooms")
    public Set<ChatRoom> getAvailableChatRooms() {
        return chatRooms;
    }

    // POST method to create a new chat room
    @PostMapping("/new-room")
    public String createNewChatRoom(@RequestBody ChatRoom chatRoom) {
        if (!chatRooms.add(chatRoom)) return "Chat room already exists.";
        // TODO: start a new GroupChat server

        return "Chat room created successfully.";
    }
}
