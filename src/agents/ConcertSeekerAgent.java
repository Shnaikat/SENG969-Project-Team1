

/* ConcertSeeker Agent code:
Through a GUI, concert seeker inserts these arguments: 
(1) Email, 
(2) Location 
(3) Ticket price
(4) Genre
Moreover, a "Seek Concert" button is defined. By clicking on this button, the ConcertSeeker Agent sends the request to the recommender agent.
*/

   /* According to our assumption, the concert seeker is a registered user, so we implemented the following:
 * 1. When the seeker clicks seek concert and his email is not in the users table, the Admin agent will verify that, 
 * and respond to the seeker to go and register (The seek concert is not allowed).
 * 2. If his/her email is within the users table, this means the seeker is a registered user, and the seek concert service is allowed for him.
 * 3. If the Recommender replied positively to this seeker, meaning there is an upcoming concert that matched the seeker's preferences
 * The seeker will be able to use Find friends service (the button will be enabled).
 * */

// Note that this file contains both the agent code as well as the GUI code. 

package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.*;

// Agent class
public class ConcertSeekerAgent extends Agent {
    private ConcertSeekerGui gui;

    protected void setup() {
        // For the sake of debugging aid
        System.out.println("ConcertSeekerAgent " + getAID().getName() + " is ready.");
        // Trigger the GUI
        gui = new ConcertSeekerGui(this);
        SwingUtilities.invokeLater(gui::showGui);
        // Add the behaviour to invoke proper inter-agent messages
        addBehaviour(new HandleRecommenderResponseBehaviour());
        addBehaviour(new HandleInvitationsResponseBehaviour());
        
    }

    public void seekConcert(String email, String location, int price, String genre) {
        // Debugging aid
        System.out.println("Seeking concert with preferences: Email: " + email + ", Location: " + location + ", Price: " + price + ", Genre: " + genre);
        // Invoke the function to send the actual message
        addBehaviour(new ConcertSeekingBehaviour(email, location, price, genre));
    }

    private class ConcertSeekingBehaviour extends OneShotBehaviour {
        private final String email;
        private final String location;
        private final int price;
        private final String genre;

        ConcertSeekingBehaviour(String email, String location, int price, String genre) {
            // Set the parameters in the constructor
            this.email = email;
            this.location = location;
            this.price = price;
            this.genre = genre;
        }

        public void action() {
            // Create the message
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            // Find the target agent by name
            cfp.addReceiver(new AID("Recommender", AID.ISLOCALNAME));
            // #-separated parameters
            cfp.setContent(email + "#" + location + "#" + price + "#" + genre);
            myAgent.send(cfp);    // Send the message
            System.out.println("CFP message sent to RecommenderAgent."); // Debug aid
        }
    }
    private class HandleRecommenderResponseBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mtProposal = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            MessageTemplate mtRefuse = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
            // We want to process either type of responses, PROPOSE for indicating successful search and REFUSE for indicating failure
            ACLMessage msg = myAgent.receive(MessageTemplate.or(mtProposal, mtRefuse));
            if (msg != null) {
                // If there has been a response: 
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    // Process and display the concert proposal
                    String concertDetails = msg.getContent();
                    gui.showResponse("Concert Found: " + concertDetails);
                } else if (msg.getPerformative() == ACLMessage.REFUSE) {
                    // Process the refusal (no concerts found or user needs to register)
                    String content = msg.getContent();
                    gui.showResponse(content);
                }
            } else {
                // Wait for an event before checking for the next message
                block();
            }
        }
    }

    public void sendInvitationAgentRequest(String preferences) {
        // Send the agent request (triggered when the "Find Friends" button is pressed)
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                // Find the message target by name
                msg.addReceiver(new AID("InvitationAgent", AID.ISLOCALNAME));
                // Send the query as an inter-agent message
                msg.setContent(preferences);
                send(msg);
                // Print to the console for debugging purposes
                System.out.println(getLocalName() + ": InvitationAgent request sent with preferences: " + preferences);
            }
        });
    }
    /* HandleInvitations:sets up message templates to listen for two types of ACL: Inform & Failure
     * If a message is received:
 *    a. INFORM case: The content is the friends list, it will be displayed on the GUI.
 * 
 *    b. FAILURE case: notifies the GUI that no friends were 
 *       found and prints a corresponding log to the console.
 * 
 *   * If no message is received, the behavior will wait until a new message arrives before executing again.
    */
    // We expect these messages to be received from the invitation agents, in reply to the friend search requests
    private class HandleInvitationsResponseBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mtInform = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            MessageTemplate mtFailure = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
            // We want to accept either type of message
            MessageTemplate mtCombined = MessageTemplate.or(mtInform, mtFailure);
            ACLMessage msg = myAgent.receive(mtCombined);
            if (msg != null) {
                // If there's a message for processing, react based on its type
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    // An INFORM message indicates success and a list of friends returned
                    String friendsList = msg.getContent();
                    gui.showFriendsList(friendsList);
                    System.out.println("Received friends list from InvitationAgent."); // print thru the console for debugging
                } else if (msg.getPerformative() == ACLMessage.FAILURE) {
                   // A FAILURE message indicates that there is no friend suggestion. 
                   gui.showFriendsList("No friends found.");
                   System.out.println("InvitationAgent could not find matching friends.");
                }
            } else {
                // If there's no message to process, wait for the next event
                block();
            }
        }
    }

    // GUI class
    private class ConcertSeekerGui extends JFrame {
        private final ConcertSeekerAgent myAgent;

        private final JTextField emailField, locationField, priceField, genreField;
        private final JButton findFriendsButton;
        private final JTextArea friendsListArea;
        
        ConcertSeekerGui(ConcertSeekerAgent a) {
            // The constructor takes the seeker agent class instance as input. 
            myAgent = a;
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit the program when the window is closed
            setLayout(new BorderLayout());                   // Window layout type

            JPanel panel = new JPanel(new GridLayout(6, 2)); // The panel containing all the buttons and text fields
            panel.add(new JLabel("Email:"));
            emailField = new JTextField(15);
            panel.add(emailField);

            panel.add(new JLabel("Preferred Location:"));
            locationField = new JTextField(15);
            panel.add(locationField);

            panel.add(new JLabel("Max Ticket Price:"));
            priceField = new JTextField(15);
            panel.add(priceField);

            panel.add(new JLabel("Preferred Genre:"));
            genreField = new JTextField(15);
            panel.add(genreField);
            
            setTitle("Concert Seeker - " + myAgent.getAID().getName()); // Include agent name to distinguish between different seeker scenarios while presenting
            
            JButton seekButton = new JButton("Seek Concert");
            // Event handler for handling button click and other forms of trigger (like the space key)
            seekButton.addActionListener(e -> {
                try {
                    // Trim all the inputs to get rid of extra whitespaces
                    String email = emailField.getText().trim();
                    String location = locationField.getText().trim();
                    String priceText = priceField.getText().trim();
                    String genre = genreField.getText().trim();

                    // Validation to ensure that none of the fields are empty
                    if (email.isEmpty() || location.isEmpty() || priceText.isEmpty() || genre.isEmpty()) {
                        JOptionPane.showMessageDialog(ConcertSeekerGui.this, "Please fill in all fields.");
                        return; // Stop further processing if any field is empty
                    }

                    // Validation to ensure that the price is a valid number
                    int price = Integer.parseInt(priceText); // This line throws NumberFormatException if price is not a valid number
                    myAgent.seekConcert(email, location, price, genre);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(ConcertSeekerGui.this, "Invalid price format.");  // Show an error dialog
                }
            });
            // Add the seek button to the same panel as the rest of the elements
            panel.add(seekButton);


            findFriendsButton = new JButton("Find Friends");
            findFriendsButton.addActionListener(e -> {
                // Get the seeker's email from the emailField text field
                String email = emailField.getText().trim();
                // Get the other preferences from their corresponding text fields
                String genre = genreField.getText().trim();
                String location = locationField.getText().trim();
                String price = priceField.getText().trim();
                
                // Construct the preferences string with the email included
                // We use a #-separated format for passing the messages
                String preferences = genre + "#" + location + "#" + price + "#" + email;
                // Send the actual inter-agent message
                myAgent.sendInvitationAgentRequest(preferences);
                findFriendsButton.setEnabled(false); // Optionally disable after sending request
            });
            findFriendsButton.setEnabled(false);
            // Add the button to the panel
            panel.add(findFriendsButton);

            // This text area is meant to display the friends found through the query, based on the response from the invitation agent
            friendsListArea = new JTextArea(5, 15);
            friendsListArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(friendsListArea);
            // First add the label to the panel
            panel.add(new JLabel("Potential friends, you can copy their emails and share with them the upcoming concert"));
            panel.add(scrollPane);

            // Prepare the panel (containing the GUI) for display
            add(panel, BorderLayout.CENTER);
            pack();
            setLocationRelativeTo(null);
        }

        public void showResponse(String message) {
            // Show the concert info if found
            JOptionPane.showMessageDialog(this, message);
            if (message.startsWith("Concert Found")) {
                enableFindFriendsButton(); // Enable the Find Friends button when a concert is found
            }
        }

        void enableFindFriendsButton() {
            // Triggered when there is a concert match
            SwingUtilities.invokeLater(() -> findFriendsButton.setEnabled(true));
        }
       
        public void showFriendsList(String friends) {
            SwingUtilities.invokeLater(() -> {
                friendsListArea.setText(""); // Clear the area before adding new content
                if (friends != null && !friends.trim().isEmpty()) {
                    friendsListArea.append(friends); // Add new content
                } else {
                    // Only display "No friends found" if the Find Friends button was pressed and disabled
                    if (!findFriendsButton.isEnabled()) {
                        friendsListArea.append("No friends found with similar preferences.");
                    }
                }
            });
        }

        void showGui() {
            setVisible(true);
        }
    }
}
