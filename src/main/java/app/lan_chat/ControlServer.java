package app.lan_chat;

import app.lan_chat.model.ChatRoom;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/control")
public class ControlServer {
    private final Set<ChatRoom> chatRooms = new HashSet<>();

    public ControlServer() {
//        chatRooms.add(new ChatRoom("Room 1", 5000));
//        chatRooms.add(new ChatRoom("Room 2", 5001));
//        chatRooms.add(new ChatRoom("Room 3", 5002));
    }

    @GetMapping("/rooms")
    public ResponseEntity<Set<ChatRoom>> getAvailableChatRooms() {
        return ResponseEntity.ok(chatRooms);
    }

    @PostMapping("/new-room")
    public ResponseEntity<String> createNewChatRoom(@RequestBody ChatRoom chatRoom) {
        if (!chatRooms.add(chatRoom)) return ResponseEntity.badRequest().body("Chat room already exists.");

        ChatRoomServer newServer = new ChatRoomServer(chatRoom.getPort());
        Thread serverThread = new Thread(newServer);
        serverThread.start();

        return ResponseEntity.ok("Chat room created successfully.");
    }
}
