package agents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminGui extends JFrame {
    private AdminAgent myAgent;

    private JTextField nameField, emailField, passwordField;

    public AdminGui(AdminAgent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(4, 2));

        p.add(new JLabel("Name:"));
        nameField = new JTextField(15);
        p.add(nameField);

        p.add(new JLabel("Password:"));
        passwordField = new JTextField(15);
        p.add(passwordField);

        p.add(new JLabel("Email:"));
        emailField = new JTextField(15);
        p.add(emailField);

        getContentPane().add(p, BorderLayout.CENTER);

        JButton createProfileButton = new JButton("Create Profile");
        createProfileButton.addActionListener(new ActionListener() {
        	
        	    public void actionPerformed(ActionEvent ev) {
        	        String name = nameField.getText().trim();
        	        String password = passwordField.getText().trim();
        	        String email = emailField.getText().trim();

        	        // Use the agent's method to validate
        	        String validationResult = myAgent.validateProfileDetails(name, email);
        	        if (!"VALID".equals(validationResult)) {
        	            JOptionPane.showMessageDialog(AdminGui.this, validationResult, "Validation Error", JOptionPane.ERROR_MESSAGE);
        	        } else {
        	            // If validation is successful, proceed with setting profile details
        	            myAgent.setProfileDetails(name, password, email);
        	        }
        	    }
        	});

        p = new JPanel();
        p.add(createProfileButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}
