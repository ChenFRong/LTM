/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Lap
 */
public class RoomManager {
    private Map<String, Room> rooms = new HashMap<>();

    public Room createRoom(String roomCode) {
        Room room = new Room(roomCode);
        rooms.put(roomCode, room);
        return room;
    }

    public Room getRoom(String roomCode) {
        return rooms.get(roomCode);
    }

    public boolean addPlayerToRoom(String roomCode, Client player) {
        Room room = rooms.get(roomCode);
        if (room != null) {
            return room.addClient(player);
        }
        return false;
    }

    public void removeRoom(String roomCode) {
        rooms.remove(roomCode);
    }

    public boolean isRoomFull(String roomCode) {
        Room room = rooms.get(roomCode);
        return room != null && room.getSizeClient() == 2;
    }

    public void notifyPlayersAboutOpponents(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room != null && room.getSizeClient() == 2) {
            Client client1 = room.getClient1();
            Client client2 = room.getClient2();
            
            client1.sendData("OPPONENT_JOINED;" + roomCode + ";" + client2.getLoginUser() + ";FIRST");
            client2.sendData("OPPONENT_JOINED;" + roomCode + ";" + client1.getLoginUser() + ";SECOND");
        }
    }

    public Room find(String roomId) {
        return rooms.get(roomId);
    }

    public void remove(Room room) {
        rooms.remove(room.getId());
    }
}
