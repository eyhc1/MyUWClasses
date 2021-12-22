package me.eyhc.ui;

import me.eyhc.utils.ParseClasses;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.InputMismatchException;
import java.util.Objects;

public class LoginUI extends JFrame implements ActionListener {
    private static final String TITLE = "ScheduleMyClasses 3.0.0";
    private static final int WIDTH = 380;
    private static final int HEIGHT = 220;
    private final JTextField netidEntry;
    private final JPasswordField passwordEntry;
    private final JCheckBox agree;
    private final JButton loginBut;
    private boolean prompt2fa;


    public LoginUI(String netid, String password) {
        this.netidEntry = new JTextField(netid, 16);
        this.netidEntry.setFont(this.netidEntry.getFont().deriveFont(14f));
        this.passwordEntry = new JPasswordField(password, 16);
        this.passwordEntry.setFont(this.passwordEntry.getFont().deriveFont(14f));
        this.agree = new JCheckBox();
        this.prompt2fa = false;
        JLabel netidLable = new JLabel("UW NetID");
        netidLable.setFont(netidLable.getFont().deriveFont(14f));
        JLabel passwordLable = new JLabel("Password");
        passwordLable.setFont(passwordLable.getFont().deriveFont(14f));
        this.loginBut = new JButton("Start Export");
        this.loginBut.setFont(this.loginBut.getFont().deriveFont(18f));
        this.loginBut.setEnabled(false);
        this.loginBut.addActionListener(this);
        this.agree.addActionListener(e -> loginBut.setEnabled(true));
        String longMsg1 = "I have acknowledged that the program is not affiliated with UW and I will use the program at my own risk.";
        JLabel clarifyPrompt = new JLabel(
                String.format("<html><div style=\"width:%dpx;\">%s</div></html>", WIDTH - 128, longMsg1));
        clarifyPrompt.setFont(clarifyPrompt.getFont().deriveFont(12f));

        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        JPanel p3 = new JPanel();
        JPanel p4 = new JPanel();

        this.setLayout(new GridLayout(4, 1));

        p1.add(netidLable);
        p1.add(this.netidEntry);
        p2.add(passwordLable);
        p2.add(this.passwordEntry);
        p3.add(this.agree);
        p3.add(clarifyPrompt);
        p4.add(loginBut);

        this.add(p1);
        this.add(p2);
        this.add(p3);
        this.add(p4);

        this.setSize(WIDTH, HEIGHT);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        this.setTitle(TITLE);
    }

    public LoginUI() {
        LoginUI loginUI = new LoginUI("","");
        this.netidEntry = loginUI.netidEntry;
        this.passwordEntry = loginUI.passwordEntry;
        this.agree = loginUI.agree;
        this.loginBut = loginUI.loginBut;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("Start Export")) {
            if (this.agree.isSelected()) {
                String netID = this.netidEntry.getText();
                String password = this.passwordEntry.getText();
                try {
                    if (netID.length() < 1 || password.length() < 1) {
                        throw new InputMismatchException("NetID and/or Password cannot be empty!");
                    }
                    this.setTitle("Please wait while the application is attempting to log in");
                    this.loginBut.setText("Logging in...");
                    this.loginBut.setEnabled(false);
                    this.update(this.getGraphics());
                    ParseClasses p = new ParseClasses();
                    String content = p.getUW(netID, password,
                            "https://my.uw.edu/api/v1/visual_schedule/current", this.prompt2fa).body().text();
                    p.parseNExport(content, "ClassSchedule_" + netID);
                    this.hide();
                    String[] o = {"Exit", "Make another one"};
                    int choice = JOptionPane.showOptionDialog(this,
                            "Your schedule has successfully exported!",
                            "Success!",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            new ImageIcon(Objects.requireNonNull(LoginUI.class.getClassLoader()
                                    .getResource("confirmed.png"))),
                            o,
                            o[0]);
                    if (choice == 1) {
                        this.setTitle(TITLE);
                        this.loginBut.setText("Start Export");
                        this.loginBut.setEnabled(true);
                        this.show();
                    } else {
                        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    this.setTitle(TITLE);
                    this.loginBut.setText("Start Export");
                    this.loginBut.setEnabled(true);
                    this.update(this.getGraphics());
                }
            } else {
                this.loginBut.setEnabled(false);
                JOptionPane.showMessageDialog(this,
                        "You have to agree and acknowledge the terms, then check the box!",
                        "Please check the box",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setPrompt2faEnabled(Boolean enabled) {
        this.prompt2fa = enabled;
    }
}
