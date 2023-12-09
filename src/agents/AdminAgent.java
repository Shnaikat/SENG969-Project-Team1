/*Admin Agent code:
* Admin Agent can make a profile for the new user.
* Admin Agent can add the user information to the users table
* If an error occurs during the profile creation process, the Admin agent can handle that error and provide the proper message.
* Admin Agent can authenticate (verify) a registered user. 
* A registered User has access to the seeking concert and finding friends seeking concert services based on their preferences. 
*/

 package agents;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.*;
import jade.lang.acl.MessageTemplate;

public class AdminAgent extends Agent {

    // DB credentials and URL
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mcrs-db";
    private static final String USER = "root";
    private static final String PASS = "kk"; 

    private AdminGui gui;

    protected void setup() {
        System.out.println("starting the Admin agent...");
        gui = new AdminGui(this);
        SwingUtilities.invokeLater(gui::showGui);
        addBehaviour(new CheckUserBehaviour());
    }
 // This method is checking what the user have inserted thru the GUI  fields --> data validation
 // Error handling : Checking if a name contains numbers
 // Error handling : Checking Email format
    public String validateProfileDetails(String name, String email) {
        if (name.matches(".*\\d.*")) {
            return "Name can't contain numbers.";
        }
        if (!email.contains("@")) {
            return "Email is not in a valid";
        }
        return "VALID"; // Return VALID if no errors
    }

    // Creating profile by inserting the users' data to the users table
    public void setUserProfileDetails(String name, String password, String email) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            
            String sql = "INSERT INTO users (name, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, password);
                pstmt.setString(3, email);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int newUserId = rs.getInt(1);
                    System.out.println("Successfully inserted user: " + name + " with ID: " + newUserId);
                    JOptionPane.showMessageDialog(gui, "Profile created successfully and the inserted user has the ID: " + newUserId);
                }
            }
         //Error handling: when creating a profile failed
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui, "insert user failed ... " + e.getMessage(), " creating profile was not successful", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    
 // this class is implementing the user checking requests --> if the users email is already in the users table
 // For example, in Recommender Agent, we need to have a registered user. This process will be done by the CheckUserBehaviour class  
    private class CheckUserBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String[] content = msg.getContent().split("#");
                if ("CHECK_USER".equals(content[0])) {
                    String userEmail = content[1];
                    handleVerifyUser(userEmail, msg);
                }
            } else {
                block();
            }
        }
//The handleVerifyUser check if user exits or doesn't exit. And it gives the message accordingly
        private void handleVerifyUser(String userEmail, ACLMessage msg) {
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String query = "SELECT ID FROM users WHERE email = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, userEmail);
                    ResultSet rs = pstmt.executeQuery();
                    ACLMessage reply = msg.createReply();
                    if (rs.next()) {
                        int userID = rs.getInt("ID");
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("USER_EXISTS#" + userID);
                        System.out.println("User exists with ID: " + userID);
                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("USER_NOT_FOUND");
                        System.out.println("No user for this email: " + userEmail + "/n You should create profile before trying seek concert service");
                    }
                    myAgent.send(reply);
                }
            } catch (Exception e) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                reply.setContent("DATABASE_ERROR");
                myAgent.send(reply);
                e.printStackTrace();
            }
        }
    }}
