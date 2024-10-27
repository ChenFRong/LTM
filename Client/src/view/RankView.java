package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RankView extends JFrame {

    private JTable rankTable;
    private DefaultTableModel tableModel;
    private JButton closeButton;

    public RankView() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Rank");
        setPreferredSize(new Dimension(600, 400));

        // Create table model with column names
        String[] columnNames = {"Rank", "Username", "Score", "Wins", "Draws", "Losses", "Avg Competitor", "Avg Time"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Create JTable with the model
        rankTable = new JTable(tableModel);
        rankTable.setFillsViewportHeight(true);

        // Add table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(rankTable);

        // Create close button
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        // Set layout
        setLayout(new BorderLayout());
        add(new JLabel("Rank", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(closeButton, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    // Method to add a player to the rank table
    public void setListUsers(int rank, String username, String score, String wins, String draws, String losses, String avgCompetitor, String avgTime) {
        tableModel.addRow(new Object[]{rank, username, score, wins, draws, losses, avgCompetitor, avgTime});
    }

    // Method to clear the rank table
    public void clearRankTable() {
        tableModel.setRowCount(0);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RankView().setVisible(true);
            }
        });
    }
}
