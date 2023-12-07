/*Admin GUI  let the user insert personal info to create profile
In other words, to make a profile, a new user needs to fill out the form that includes three fields: (1)Name, (2)Password, (3)Email.
Then the registration will be done through the Admin GUI.*/


package agents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminGui extends JFrame {
    private AdminAgent myAgent;//instance from the Admin agent

    private JTextField nameTextField, emailField, passwordField;

    public AdminGui(AdminAgent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();// instance of JPanel to create neat GUI
        //  p.setLayout(new GridLayout(8, 4));
        p.setLayout(new GridLayout(4, 2));
//creating the fields so the user can insert his personal info to create profile ->register
        p.add(new JLabel("Name:"));
        
       // nameField = new JTextField(20);
        nameTextField = new JTextField(15);
        p.add(nameTextField);

        p.add(new JLabel("Password:"));
        passwordField = new JTextField(15);
        p.add(passwordField);

        p.add(new JLabel("Email:"));
        emailField = new JTextField(15);
        p.add(emailField);

        getContentPane().add(p, BorderLayout.CENTER);

        JButton createUserProfileButton = new JButton("Create Profile");
        createUserProfileButton.addActionListener(new ActionListener() {
        	
        	    public void actionPerformed(ActionEvent ev) {
        	        String name = nameTextField.getText().trim();//trimming any spaces
        	        String password = passwordField.getText().trim();
        	        String email = emailField.getText().trim();

        	        // validate the user
        	        String validationResult = myAgent.validateProfileDetails(name, email);
        	        if (!"VALID".equals(validationResult)) {
        	            JOptionPane.showMessageDialog(AdminGui.this, validationResult, "Validation errorss", JOptionPane.ERROR_MESSAGE);
        	        } else {
        	            // user pass the validation -> continue to register
        	            myAgent.setUserProfileDetails(name, password, email);
        	        }
        	    }
        	});

        p = new JPanel();
        p.add(createUserProfileButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });

        setResizable(false);
    }

    public void showGui() {
       pack();// show the window properly when running
        this.setTitle("MCRS registration window");//giving title to the window
        this.setResizable(true);
   //   this.getContentPane().setBackground(Color.BLUE);
        this.setVisible(true);//make frame visible
    }
}
