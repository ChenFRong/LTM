/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Lap
 */
public class GameRoom {
    private String roomCode;
    private List<Client> players = new ArrayList<>(2);
    private Client firstPlayer;

    public GameRoom(String roomCode) {
        this.roomCode = roomCode;
        this.firstPlayer = null;
    }

    public boolean addPlayer(Client player) {
        if (players.size() < 2 && !players.contains(player)) {
            String playerUsername = player.getLoginUser();
            players.add(player);
            if (firstPlayer == null) {
                firstPlayer = player;
                System.out.println("First player joined room " + roomCode + ": " + playerUsername);
            } else {
                System.out.println("Second player joined room " + roomCode + ": " + playerUsername);
            }
            System.out.println("Current players in room " + roomCode + ":");
            for (int i = 0; i < players.size(); i++) {
                System.out.println((i + 1) + ". " + players.get(i).getLoginUser());
            }
            if (players.size() == 2) {
                notifyPlayersAboutOpponents();
            }
            return true;
        }
        return false;
    }

    public List<String> getPlayerNames() {
        List<String> names = players.stream().map(Client::getLoginUser).collect(Collectors.toList());
        System.out.println("Players in room " + roomCode + ": " + String.join(", ", names));
        return names;
    }

    public void notifyPlayersAboutOpponents() {
        if (players.size() == 2) {
            Client player1 = firstPlayer;
            Client player2 = players.get(0).equals(firstPlayer) ? players.get(1) : players.get(0);
            
            String player1Username = player1.getLoginUser();
            String player2Username = player2.getLoginUser();
            
            System.out.println("Notifying players about opponents in room " + roomCode);
            System.out.println("Sending to " + player1Username + ": OPPONENT_JOINED;" + roomCode + ";" + player2Username + ";FIRST");
            player1.sendData("OPPONENT_JOINED;" + roomCode + ";" + player2Username + ";FIRST");
            
            System.out.println("Sending to " + player2Username + ": OPPONENT_JOINED;" + roomCode + ";" + player1Username + ";SECOND");
            player2.sendData("OPPONENT_JOINED;" + roomCode + ";" + player1Username + ";SECOND");
            
            System.out.println("Room " + roomCode + " is now full:");
            System.out.println("1. " + player1Username + " (First player)");
            System.out.println("2. " + player2Username + " (Second player)");
        }
    }

    public boolean isFull() {
        return players.size() == 2;
    }

    public List<Client> getPlayers() {
        return players;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void printPlayerList() {
        System.out.println("Players in room " + roomCode + ":");
        for (int i = 0; i < players.size(); i++) {
            Client player = players.get(i);
            String playerType = (player == firstPlayer) ? "First Player" : "Second Player";
            System.out.println((i + 1) + ". " + player.getLoginUser() + " (" + playerType + ")");
        }
        System.out.println("Total players: " + players.size());
    }
}
