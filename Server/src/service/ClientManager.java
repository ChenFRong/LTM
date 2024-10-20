package service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import service.Client;

public class ClientManager {
    // Danh sách người dùng trực tuyến
    private ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();

    // Thêm client vào danh sách
    public void addClient(String username, Client client) {
        clients.put(username, client);
    }

    // Xóa client khỏi danh sách
    public void removeClient(String username) {
        clients.remove(username);
    }

    // Lấy danh sách người dùng trực tuyến
    public String getListUseOnline() {
        List<String> onlineUsers = new ArrayList<>(clients.keySet());
        return String.join(",", onlineUsers); // Trả về danh sách dưới dạng chuỗi
    }

    // Kiểm tra xem người dùng có đang trực tuyến không
    public boolean isUserOnline(String username) {
        return clients.containsKey(username);
    }

    // Lấy client theo username
    public Client getClient(String username) {
        return clients.get(username);
    }
}
