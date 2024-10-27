package run;

import controller.SocketHandler;
import view.ConnectServer;
import view.LoginView;
import view.RegisterView;
import view.HomeView;
import view.PlaywithFriend;
import java.util.HashMap;
import view.GameRoom;
import java.util.Map;

public class ClientRun{
    public enum SceneName {
        CONNECTSERVER,
        LOGIN,
        REGISTER,
        HOMEVIEW,
        PLAYWITHFRIEND
    }

    // scenes
    public static ConnectServer connectServer;
    public static LoginView loginView;
    public static RegisterView registerView;
    public static HomeView homeView;
    public static PlaywithFriend playWithFriendView;

    // controller 
    public static SocketHandler socketHandler;

    private static HashMap<String, GameRoom> activeGameRooms = new HashMap<>();
    private static Map<String, GameRoom> gameRooms = new HashMap<>();

    public ClientRun() {
        socketHandler = new SocketHandler();
        initScene();
        openScene(SceneName.CONNECTSERVER);
    }

    public void initScene() {
        connectServer = new ConnectServer();
        loginView = new LoginView();
        registerView = new RegisterView();
        homeView = new HomeView();
        playWithFriendView = new PlaywithFriend();
    }

    public static void openScene(SceneName sceneName) {
        closeAllScene();
        if (null != sceneName) {
            System.out.println("Opening scene: " + sceneName);
            String username = socketHandler.getLoginUser();
            double score = socketHandler.getScore();
            int wins = socketHandler.getWins();
            switch (sceneName) {
                case CONNECTSERVER:
                    connectServer.setVisible(true);
                    break;
                case LOGIN:
                    loginView.setVisible(true);
                    break;
                case REGISTER:
                    registerView.setVisible(true);
                    break;
                case HOMEVIEW:
                    homeView.setVisible(true);
                    homeView.updateUserInfo(username, score, wins);
                    homeView.satusLabel();
                    ((HomeView) homeView).enableQuickMatch();
//                    ((HomeView) homeView).enableCreateRoom();
                    break;
                case PLAYWITHFRIEND:
                    playWithFriendView.setVisible(true);
                    playWithFriendView.setUsername(username);
                    break;
                default:
                    break;
            }
        }
    }

    public static void closeScene(SceneName sceneName) {
        if (null != sceneName) {
            System.out.println("Closing scene: " + sceneName); // In ra màn hình cảnh đang đóng
            switch (sceneName) {
                case CONNECTSERVER:
                    connectServer.dispose();
                    break;
                case LOGIN:
                    loginView.dispose();
                    break;
                case REGISTER:
                    registerView.setVisible(false);
                    registerView.dispose();
                    break;
                case HOMEVIEW:
                    homeView.dispose();
                    break;
                case PLAYWITHFRIEND:
                    playWithFriendView.dispose();
                    break;   
                default:
                    break;
            }
        }
    }

    public static void closeAllScene() {
        System.out.println("Closing all scenes");
        if (connectServer != null) connectServer.dispose();
        if (loginView != null) loginView.dispose();
        if (registerView != null) registerView.dispose();
        if (homeView != null) homeView.dispose();
        if(playWithFriendView !=null)playWithFriendView.dispose();
    }

    public static void addGameRoom(String roomCode, GameRoom gameRoom) {
        gameRooms.put(roomCode, gameRoom);
        System.out.println("GameRoom added to map: " + roomCode); // Log để kiểm tra
    }

    public static GameRoom findGameRoom(String roomCode) {
        GameRoom gameRoom = gameRooms.get(roomCode);
        if (gameRoom == null) {
            System.out.println("GameRoom not found in map: " + roomCode); // Log để kiểm tra
        } else {
            System.out.println("GameRoom found in map: " + roomCode); // Log để kiểm tra
        }
        return gameRoom;
    }

    public static void createGameRoom(String roomCode, String playerName, boolean isHost) {
        if (!gameRooms.containsKey(roomCode)) {
            GameRoom gameRoom = new GameRoom(playerName, roomCode, isHost);
            gameRooms.put(roomCode, gameRoom);
        }
    }

    public static void removeGameRoom(String roomCode) {
        GameRoom gameRoom = gameRooms.remove(roomCode);
        if (gameRoom != null) {
            gameRoom.dispose();
        }
    }

    public static void openGameRoom(String roomCode) {
        GameRoom gameRoom = activeGameRooms.get(roomCode);
        if (gameRoom != null) {
            System.out.println("Opening GameRoom: " + roomCode);
            gameRoom.setVisible(true);
            gameRoom.toFront();
        } else {
            System.err.println("GameRoom not found for roomCode: " + roomCode);
        }
    }
//    public static void enableQuickMatch() {
//        if (homeView instanceof HomeView) {
//            ((HomeView) homeView).enableQuickMatch();
//        }
//    }
//
//    public static void disableQuickMatch() {
//        if (homeView instanceof HomeView) {
//            ((HomeView) homeView).disableQuickMatch();
//        }
//    }

    public static void main(String[] args) {
        new ClientRun();
    }

}
