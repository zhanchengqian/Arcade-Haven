package gui;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class ChatTest implements Runnable {

    // GUI
    public  JFrame frame = new JFrame();
    public JPanel panel;
    private JTextField msgInput;
    private JButton msgSendButton;
    private JButton refreshButton;
    private DefaultListModel<String> users = new DefaultListModel<>();
    private JList userlist;
    private JTextArea msgHist;
    private JScrollPane msgHistScroller;

    // Connection
    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    String username = "timcook";

    // Background thread
    private Thread thread;

    // Constructor
    public ChatTest(Socket socket, Scanner in, PrintWriter out, String username) {

        this.username = username;
        this.socket = socket;
        this.in = in;
        this.out = out;

        frame.add(panel);
        frame.setSize(640, 480);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        userlist.setModel(users);

        msgSendButton.addActionListener(e -> {
            if (userlist.getSelectedValue() == null) return;
            if (msgInput.getText().equals("")) return;

            String msg = msgInput.getText();
            msgInput.setText("");
            try {
                msg = String.format("message %s %s", userlist.getSelectedValue(), msg);
                out.println(msg);
                display(msg, Direction.SEND);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        refreshButton.addActionListener(e -> {
            try {
                out.println("userlist");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        thread = new Thread(this, "chat");
    }

    public void start() {
        frame.setVisible(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (in.hasNextLine())
                display(in.nextLine(), Direction.RECV);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void display(String msg, Direction dir) {
        EventQueue.invokeLater(() -> {
            System.out.println(msg);

            String[] tokens = msg.split(" ", 2);
            String type = tokens[0];
            String[] receiverBody = null;
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

            switch (type) {
                case "message":
                    if (dir == Direction.RECV) break;
                    receiverBody = tokens[1].split(" ", 2);
                    msgHist.append(String.format("(%s) to [%s]: %s%n", time, receiverBody[0], receiverBody[1]));
                    break;
                case "recvmsg":
                    receiverBody = tokens[1].split(" ", 2);
                    msgHist.append(String.format("(%s) from [%s]: %s%n", time, receiverBody[0], receiverBody[1]));
                    break;
                case "userlist":
                    users.removeAllElements();
                    Arrays.stream(tokens[1].split(" ")).forEach(users::addElement);
            }
        });
    }

    public static void main(String[] args) {
//        EventQueue.invokeLater(() -> new ChatTest().start());
    }
}