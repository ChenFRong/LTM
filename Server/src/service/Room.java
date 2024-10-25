package service;

import controller.UserController;
import helper.CountDownTimer;
import helper.CustumDateTimeFormatter;
import model.ProductModel;
import model.UserModel;
import run.ServerRun;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class Room {
    private String id;
    private String time = "00:00";
    private Client client1 = null, client2 = null;
    private ArrayList<Client> clients = new ArrayList<>();
    
    boolean gameStarted = false;
    CountDownTimer matchTimer;
    CountDownTimer waitingTimer;
    
    ProductModel currentProduct;
    double priceGuessClient1;
    double priceGuessClient2;
    
    String playAgainC1;
    String playAgainC2;
    String waitingTime= "00:00";
    
    private static final int MAX_ROUNDS = 5;
    private int currentRound = 0;
    private boolean isGameOver = false;
    private double scoreClient1 = 0;
    private double scoreClient2 = 0;
    private static final double WINNING_THRESHOLD = 0.1; // 10% difference threshold
    
    public LocalDateTime startedTime;

    public Room(String id) {
        this.id = id;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void startGame() {
        gameStarted = true;
        currentRound = 1;
        isGameOver = false;
        scoreClient1 = 0;
        scoreClient2 = 0;
        
        currentProduct = ServerRun.productManager.getRandomProduct();
        
        matchTimer = new CountDownTimer(30);
        matchTimer.setTimerCallBack(
            null,
            (Callable) () -> {
                time = "" + CustumDateTimeFormatter.secondsToMinutes(matchTimer.getCurrentTick());
                return null;
            },
            1
        );
        
        // Gửi thông tin sản phẩm cho cả hai người chơi
        String productInfo = "START_GAME;success;" 
                + id + ";" 
                + currentProduct.getName() + ";" 
                + currentProduct.getImagePath() + ";" 
                + currentRound;
        broadcast(productInfo);
    }
    
    public void handleRoundEnd() throws SQLException {
        String result = handleResultClient();
        String[] resultParts = result.split(";");
        String roundWinner = resultParts[0];
        double roundScoreClient1 = Double.parseDouble(resultParts[1]);
        double roundScoreClient2 = Double.parseDouble(resultParts[2]);
        double totalScoreClient1 = Double.parseDouble(resultParts[3]);
        double totalScoreClient2 = Double.parseDouble(resultParts[4]);

        String nameClient1 = client1.getLoginUser(); 
        String nameClient2 = client2.getLoginUser();

        broadcast("ROUND_RESULT;success"  + ";" + roundWinner + ";" + currentProduct.getPrice() + ";" 
                  + priceGuessClient1 + ";" + priceGuessClient2 + ";" 
                  + roundScoreClient1 + ";" + roundScoreClient2 + ";"
                  + totalScoreClient1 + ";" + totalScoreClient2 + ";"
                  + nameClient1 + ";" + nameClient2+ ";"+currentRound);

        if (currentRound < MAX_ROUNDS) {
            currentRound++;
            startNextRound();
        } else {
            endGame();
        }
    }

     private void startNextRound() {
        resetGuesses();
        currentProduct = ServerRun.productManager.getRandomProduct();
        String productInfo = "NEXT_ROUND;success;" 
                + id + ";" 
                + currentProduct.getName() + ";" 
                + currentProduct.getImagePath() + ";" 
                + currentRound;
        broadcast(productInfo);
        matchTimer.restart();
    }
    
    private void endGame() throws SQLException {
        String winner = determineWinner();
        updateUserStats(winner);
        broadcast("GAME_OVER;success;" + winner + ";" + client1.getLoginUser() + ";" + client2.getLoginUser() + ";" + id + ";" + scoreClient1 + ";" + scoreClient2);
        waitingClientTimer();
    }
    
    private String determineWinner() {
        if (scoreClient1 > scoreClient2) {
            return client1.getLoginUser();
        } else if (scoreClient2 > scoreClient1) {
            return client2.getLoginUser();
        } else {
            return "DRAW";
        }
    }

    private void resetGuesses() {
        priceGuessClient1 = 0;
        priceGuessClient2 = 0;
    }
    

    //hàm này để đếm ngược thời gian chờ phản hồi có tiếp tục không
    public void waitingClientTimer() {
        waitingTimer = new CountDownTimer(30);
        waitingTimer.setTimerCallBack(
            null,
            (Callable) () -> {
                waitingTime = "" + CustumDateTimeFormatter.secondsToMinutes(waitingTimer.getCurrentTick());
                System.out.println("waiting: " + waitingTime);
                if (waitingTime.equals("00:00")) {
                    handleTimeoutPlayAgain();
                }
                return null;
            },
            1
        );// Bắt đầu bộ đ���m ngược
    }

    private void handleTimeoutPlayAgain() {
        if (playAgainC1 == null) {
            client1.sendData("PLAY_AGAIN_TIMEOUT");
            playAgainC1 = "NO";
        }
        if (playAgainC2 == null) {
            client2.sendData("PLAY_AGAIN_TIMEOUT");
            playAgainC2 = "NO";
        }
        if (playAgainC1.equals("NO") && playAgainC2.equals("NO")) {
            broadcast("ASK_PLAY_AGAIN;NO");
            deleteRoom();
        } else {
            handlePlayAgain();
        }
    }
    
    public void deleteRoom() {
        if (waitingTimer != null) {
            waitingTimer.cancel(); // Hủy bỏ bộ đếm thời gian
            waitingTimer = null;
        }
        client1.setJoinedRoom(null);
        client1.setcCompetitor(null);
        client2.setJoinedRoom(null);
        client2.setcCompetitor(null);
        ServerRun.roomManager.remove(this);
    }
    
    public void resetRoom() {
    gameStarted = false;
    currentProduct = null;
    priceGuessClient1 = 0;
    priceGuessClient2 = 0;
    playAgainC1 = null;
    playAgainC2 = null;
    time = "00:00";
    waitingTime = "00:00";
    currentRound = 0;
    isGameOver = false;

    // Khởi động lại waitingTimer
    if (waitingTimer != null) {
        waitingTimer.cancel();
    }
    waitingTimer = new CountDownTimer(30);
    waitingTimer.setTimerCallBack(
        null,
        (Callable) () -> {
            waitingTime = "" + CustumDateTimeFormatter.secondsToMinutes(waitingTimer.getCurrentTick());
            System.out.println("waiting: " + waitingTime);
            if (waitingTime.equals("00:00")) {
                handleTimeoutPlayAgain();
            }
            return null;
        },
        1
    );
}
    
    public String handleResultClient() {
        double actualPrice = currentProduct.getPrice();
        double diff1 = Math.abs(priceGuessClient1 - actualPrice);
        double diff2 = Math.abs(priceGuessClient2 - actualPrice);
        
        // double percentDiff1 = diff1 / actualPrice;
        // double percentDiff2 = diff2 / actualPrice;
        
        String roundWinner;
        double roundScoreClient1 = 0;
        double roundScoreClient2 = 0;

        if (priceGuessClient1 <=actualPrice && priceGuessClient2 <= actualPrice) {
            if (diff1 < diff2) {
                roundWinner = client1.getLoginUser();
                roundScoreClient1 = 1;
            } else if (diff2 < diff1) {
                roundWinner = client2.getLoginUser();
                roundScoreClient2 = 1;
            } else {
                roundWinner = "DRAW";
                roundScoreClient1 = 0.5;
                roundScoreClient2 = 0.5;
            }
        } else if (priceGuessClient1 <=actualPrice) {
            roundWinner = client1.getLoginUser();
            roundScoreClient1 = 1;
        } else if (priceGuessClient2 <= actualPrice) {
            roundWinner = client2.getLoginUser();
            roundScoreClient2 = 1;
        }  else{
                roundWinner = "DRAW";
                roundScoreClient1 = 0.5;
                roundScoreClient2 = 0.5;
        }
        
        scoreClient1 += roundScoreClient1;
        scoreClient2 += roundScoreClient2;
        
        return roundWinner + ";" + roundScoreClient1 + ";" + roundScoreClient2 + ";" + scoreClient1 + ";" + scoreClient2;
    }
    
    private double calculateScore(double difference) {
        return Math.max(0, 100 - difference);
    }

    public void draw() throws SQLException {
        UserModel user1 = new UserController().getUser(client1.getLoginUser());
        UserModel user2 = new UserController().getUser(client2.getLoginUser());
        
        user1.setDraw(user1.getDraw() + 1);
        user2.setDraw(user2.getDraw() + 1);
        
        user1.setScore(user1.getScore() + 0.5f);
        user2.setScore(user2.getScore() + 0.5f);
        
        // Cập nhật thông tin người chơi
        updateUserStats(user1, user2, 0.5f, 0.5f);
    }
    
    public void client1Win(float score) throws SQLException {
        UserModel user1 = new UserController().getUser(client1.getLoginUser());
        UserModel user2 = new UserController().getUser(client2.getLoginUser());
        
        user1.setWin(user1.getWin() + 1);
        user2.setLose(user2.getLose() + 1);
        
        user1.setScore(user1.getScore() + score);
        
        updateUserStats(user1, user2, score, 0);
    }
    
    public void client2Win(float score) throws SQLException {
        UserModel user1 = new UserController().getUser(client1.getLoginUser());
        UserModel user2 = new UserController().getUser(client2.getLoginUser());
        
        user2.setWin(user2.getWin() + 1);
        user1.setLose(user1.getLose() + 1);
        
        user2.setScore(user2.getScore() + score);
        
        updateUserStats(user1, user2, 0, score);
    }
    
    private void updateUserStats(UserModel user1, UserModel user2, float scoreUser1, float scoreUser2) throws SQLException {
        int totalMatchUser1 = user1.getWin() + user1.getDraw() + user1.getLose();
        int totalMatchUser2 = user2.getWin() + user2.getDraw() + user2.getLose();
        
        float newAvgCompetitor1 = (totalMatchUser1 * user1.getAvgCompetitor() + user2.getScore()) / (totalMatchUser1 + 1);
        float newAvgCompetitor2 = (totalMatchUser2 * user2.getAvgCompetitor() + user1.getScore()) / (totalMatchUser2 + 1);
        
        user1.setAvgCompetitor(newAvgCompetitor1);
        user2.setAvgCompetitor(newAvgCompetitor2);
        
        // Cập nhật thời gian trung bình nếu cần
        
        new UserController().updateUser(user1);
        new UserController().updateUser(user2);
    }
    
    private void updateUserStats(String winner) throws SQLException {
        UserModel user1 = new UserController().getUser(client1.getLoginUser());
        UserModel user2 = new UserController().getUser(client2.getLoginUser());
        
        if (winner.equals("DRAW")) {
            user1.setDraw(user1.getDraw() + 1);
            user2.setDraw(user2.getDraw() + 1);
            user1.setScore(user1.getScore() + (float)scoreClient1);
            user2.setScore(user2.getScore() + (float)scoreClient2);
        } else if (winner.equals(client1.getLoginUser())) {
            user1.setWin(user1.getWin() + 1);
            user2.setLose(user2.getLose() + 1);
            user1.setScore(user1.getScore() + (float)scoreClient1);
        } else {
            user2.setWin(user2.getWin() + 1);
            user1.setLose(user1.getLose() + 1);
            user2.setScore(user2.getScore() + (float)scoreClient2);
        }
        
        updateUserStats(user1, user2, (float)scoreClient1, (float)scoreClient2);
    }
    
    public void userLeaveGame (String username) throws SQLException {
        if (client1.getLoginUser().equals(username)) {
            client2Win(0);
        } else if (client2.getLoginUser().equals(username)) {
            client1Win(0);
        }
    }
    
    public String handlePlayAgain() {
        if (playAgainC1 == null || playAgainC2 == null) {
            return "WAITING";
        } else if (playAgainC1.equals("NO") || playAgainC2.equals("NO")) {
            if (waitingTimer != null) {
                waitingTimer.cancel(); // Hủy bỏ bộ đếm thời gian
                waitingTimer = null;
            }
            return "NO";
        } else if (playAgainC1.equals("YES") && playAgainC2.equals("YES")) {
            if (waitingTimer != null) {
                waitingTimer.cancel(); // Hủy bỏ bộ đếm thời gian
                waitingTimer = null;
            }
            return "YES";
        }
        return "NO";
    }
    
    
    // add/remove client
    public boolean addClient(Client c) {
        if (clients.size() < 2 && !clients.contains(c)) {
            clients.add(c);
            if (client1 == null) {
                client1 = c;
            } else if (client2 == null) {
                client2 = c;
            }
            return true;
        }
        return false;
    }

    public boolean removeClient(Client c) {
        if (clients.remove(c)) {
            if (c == client1) {
                client1 = null;
            } else if (c == client2) {
                client2 = null;
            }
            return true;
        }
        return false;
    }

    // broadcast messages
    public void broadcast(String msg) {
        for (Client c : clients) {
            c.sendData(msg);
        }
    }
    
    public Client find(String username) {
        for (Client c : clients) {
            if (c.getLoginUser()!= null && c.getLoginUser().equals(username)) {
                return c;
            }
        }
        return null;
    }

    // gets sets
    public void setCurrentProduct(ProductModel product) {
        this.currentProduct = product;
    }

    public ProductModel getCurrentProduct() {
        return this.currentProduct;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Client getClient1() {
        return client1;
    }

    public void setClient1(Client client1) {
        this.client1 = client1;
    }

    public Client getClient2() {
        return client2;
    }

    public void setClient2(Client client2) {
        this.client2 = client2;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public void setClients(ArrayList<Client> clients) {
        this.clients = clients;
    }
    
    public int getSizeClient() {
        return clients.size();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    
    public void setPriceGuessClient1(double priceGuess) {
        this.priceGuessClient1 = priceGuess;
    }
    
    public void setPriceGuessClient2(double priceGuess) {
        this.priceGuessClient2 = priceGuess;
    }

    public double getPriceGuessClient1() {
        return priceGuessClient1;
    }

    public void setResultClient1(double priceGuess) {
        this.priceGuessClient1 = priceGuess;
    }

    public double getPriceGuessClient2() {
        return priceGuessClient2;
    }

    public void setResultClient2(double priceGuess) {
        this.priceGuessClient2 = priceGuess;
    }

    public String getPlayAgainC1() {
        return playAgainC1;
    }

    public void setPlayAgainC1(String playAgainC1) {
        this.playAgainC1 = playAgainC1;
    }

    public String getPlayAgainC2() {
        return playAgainC2;
    }

    public void setPlayAgainC2(String playAgainC2) {
        this.playAgainC2 = playAgainC2;
    }

    public String getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(String waitingTime) {
        this.waitingTime = waitingTime;
    }
}
