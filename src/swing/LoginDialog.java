package swing;
import client.auth.AuthException;
import client.controller.impl.ImplClientController;


import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LoginDialog extends JDialog {

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbUsername;
    private JLabel lbPassword;
    private JButton btnLogin;
    private JButton btnCancel;

    private ImplClientController clientController;

    private boolean connected;

    public LoginDialog(Frame parent, ImplClientController clientController) {
        super(parent, "Окно авторизации", true);

        this.clientController = clientController;
        this.connected = false;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbUsername = new JLabel("Имя пользователя: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        lbPassword = new JLabel("Пароль: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        btnLogin = new JButton("Войти");
        btnCancel = new JButton("Отмена");

        JPanel bp = new JPanel();
        bp.add(btnLogin);
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientController.tryAuthorization(tfUsername.getText(), String.valueOf(pfPassword.getPassword()));
            }
        });

        bp.add(btnCancel);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public void tryCloseLoginDialog (Throwable cause) {
        if (cause instanceof AuthException) {
            JOptionPane.showMessageDialog(LoginDialog.this,
                      "Ошибка авторизации",
                      "Авторизация",
                      JOptionPane.ERROR_MESSAGE);
            return;
        }else if (cause instanceof Exception){
            JOptionPane.showMessageDialog(LoginDialog.this,
                      "Ошибка сети",
                      "Авторизация",
                      JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (connected){
            dispose();
        }
    }

    public void setConnected (boolean result) {
        this.connected = result;
    }

    public boolean isConnected() {
        return connected;
    }
}
