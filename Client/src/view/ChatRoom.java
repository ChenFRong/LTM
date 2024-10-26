package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import run.ClientRun;
import javax.swing.text.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom extends JFrame {
    private JTextPane chatPane;
    private JTextField messageField;
    private JButton sendButton;
    private JButton exitButton;
    private String currentUser;
    private String otherUser;
    private static Map<String, ChatRoom> instances = new HashMap<>();
    private boolean otherUserLeft = false;
    private List<ChatMessage> savedMessages = new ArrayList<>();
    private boolean otherUserJoined = false;
    private List<String> pendingMessages = new ArrayList<>();

    private ChatRoom(String currentUser, String otherUser) {
        super("Chat với " + otherUser);
        this.currentUser = currentUser;
        this.otherUser = otherUser;
        setSize(400, 500);
        setLayout(new BorderLayout());

        // Chat area
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messageField = new JTextField();
        messageField.setBorder(BorderFactory.createCompoundBorder(
            messageField.getBorder(), 
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        sendButton = new JButton("Gửi");
        sendButton.setBackground(new Color(0, 120, 215));
        sendButton.setForeground(Color.WHITE);
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Exit button
        exitButton = new JButton("Thoát");
        exitButton.setBackground(new Color(220, 53, 69));
        exitButton.setForeground(Color.WHITE);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(exitButton);
        add(topPanel, BorderLayout.NORTH);

        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        exitButton.addActionListener(e -> exitChat());

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitChat();
            }
        });
        setLocationRelativeTo(null);
    }

    private void exitChat() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn rời khỏi cuộc trò chuyện?",
            "Xác nhận thoát",
            JOptionPane.YES_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ClientRun.socketHandler.exitChatRoom(otherUser);
            dispose();
            ClientRun.openScene(ClientRun.SceneName.HOMEVIEW);
        }
    }

    public static synchronized ChatRoom getInstance(String currentUser, String otherUser) {
        String key = generateKey(currentUser, otherUser);
        if (!instances.containsKey(key)) {
            instances.put(key, new ChatRoom(currentUser, otherUser));
        }
        return instances.get(key);
    }

    private static String generateKey(String user1, String user2) {
        // Tạo key duy nhất cho mỗi cặp người dùng, không phân biệt thứ tự
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            if (otherUserJoined) {
                ClientRun.socketHandler.sendChatMessage(otherUser, message);
                addMessage(currentUser, message, true);
            } else {
                pendingMessages.add(message);
                addMessage(currentUser, message, true);
                JOptionPane.showMessageDialog(this, 
                    "Tin nhắn sẽ được gửi khi người kia tham gia lại cuộc trò chuyện.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            messageField.setText("");
        }
    }

    private void sendPendingMessages() {
        for (String message : pendingMessages) {
            ClientRun.socketHandler.sendChatMessage(otherUser, message);
        }
        pendingMessages.clear();
    }

    public void addMessage(String sender, String message, boolean isCurrentUser) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatPane.getStyledDocument();
            SimpleAttributeSet style = new SimpleAttributeSet();

            // Set alignment
            StyleConstants.setAlignment(style, isCurrentUser ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);

            // Create a new paragraph for the message
            try {
                doc.insertString(doc.getLength(), "\n", style);
                int paragraphStart = doc.getLength();

                // Set color for sender name
                StyleConstants.setForeground(style, isCurrentUser ? Color.BLUE : Color.RED);
                doc.insertString(doc.getLength(), sender + ": ", style);

                // Set color for message
                StyleConstants.setForeground(style, Color.BLACK);
                doc.insertString(doc.getLength(), message, style);

                // Apply paragraph alignment
                doc.setParagraphAttributes(paragraphStart, doc.getLength() - paragraphStart, style, false);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            // Scroll to the bottom
            chatPane.setCaretPosition(doc.getLength());
        });
    }

    public void setOtherUserLeft(boolean left) {
        this.otherUserLeft = left;
    }

    public void saveMessage(String sender, String message) {
        savedMessages.add(new ChatMessage(sender, message));
    }

    public void loadSavedMessages() {
        for (ChatMessage msg : savedMessages) {
            addMessage(msg.sender, msg.message, msg.sender.equals(currentUser));
        }
        savedMessages.clear();
    }

    public void displayExitMessage(String exitedUser) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                exitedUser + " đã rời khỏi cuộc trò chuyện.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            
            //addSystemMessage(exitedUser + " đã rời khỏi cuộc trò chuyện.");
        });
        otherUserJoined = false;
    }

    public void setOtherUserJoined(boolean joined) {
        this.otherUserJoined = joined;
        if (joined) {
            sendPendingMessages();
        }
    }

    private static class ChatMessage {
        String sender;
        String message;

        ChatMessage(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }
    }
}
