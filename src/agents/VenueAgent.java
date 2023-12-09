/*
 * This agent is acting like the service provider. 
 * If there is an upcoming concert, the Venue Agent will notify/send the information about this concert to the data manager agent. 
 * This information includes Concert Location, Ticket Price, and Genre.
 * Therefore, we can have this concert information.
 * We provide the concert information through arguments when starting this agent, so we are having different ways (variations) in our implementation.
 * For providing the arguments, we need to consider the order of the data and the number of the provided arguments. 
 */
package agents;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class VenueAgent extends Agent {
    private String concertLocation;
    private int ticketPrice;
    private String genre;

    protected void setup() {
        System.out.println(getLocalName() + ": ready .. I am an instance of the Venue agent"); //when right click on the Main-Container and start new Agent (Venue Agent), this message will be showed in the Concole

       /* Object[] args = getArguments();
        if (args != null && args.length == 3) {
            concertLocation = args[0];
            ticketPrice = Integer.parseInt((String) args[1]);
            genre =args[2];*/

// when "start new agent" (VenueAgent with an arbitarary name), we fill out the Argument box to enter the concert infor providing by that specific Venue (for example provider 1)        
// for example, if the number of the arguments is less than three, it gives error       
        Object[] args = getArguments();
        if (args != null && args.length == 3) {
            concertLocation = (String) args[0];
            ticketPrice = Integer.parseInt((String) args[1]);
            genre = (String) args[2];

            // For the sake of debugging and knowing what's going on we are printing out the received arguments
            System.out.println(getLocalName() + ": received the arguments - Location: " + concertLocation + ", Price: " + ticketPrice + ", Genre: " + genre);

            
            /*ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("DataManager", AID.ISLOCALNAME));//the data manager will be the receiver 
            msg.setContent(concertLocation + "#" + ticketPrice + "#" + genre);//splitting the data using #
            send(msg);*/
            
            // Sending these details to  dataManager Agent -> should be dataManager
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("dataManager", AID.ISLOCALNAME));//the data manager will be the receiver 
            msg.setContent(concertLocation + "#" + ticketPrice + "#" + genre);//splitting the data using #
            send(msg);
        } else {
            System.out.println(getLocalName() + ": The provided No. of arguments is incorrect or Wrong separator used (Please use comma (',') as the separator)");
            doDelete();
        }
    }
}

/* This is an earlier version of the code in which we experimented with a different agent architecture, with GUI and a different inter-agent communication structure. 
    public class Venue extends Agent {
	// Communication target
	String dataManager = "DataManager";
	JTextField txtLocation = new JTextField(15);
	JTextField txtTicketPrice = new JTextField(15);
	JTextField txtGenre = new JTextField(15);
	
	private String createMessage (String location, String ticketPrice, String genre) {
		return location + "," + ticketPrice + "," + genre;
	}
	
	private class addBtnMouseListenerClass implements MouseListener {
		public void mouseEntered(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseClicked(MouseEvent e) {
			//JOptionPane.showMessageDialog(null, "Haha you clicked");
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setContent(createMessage(txtLocation.getText(), txtTicketPrice.getText(), txtGenre.getText()));
			msg.addReceiver(new AID(dataManager, AID.ISLOCALNAME));
			send(msg);
		}
		public void mouseReleased(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
	}
	
	void createGui() {
		JFrame frame = new JFrame("Add Concert");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  // Hehe
		frame.setSize(300, 180);
		
		JPanel pnlConcert = new JPanel();
		pnlConcert.setLayout(new BoxLayout(pnlConcert, BoxLayout.PAGE_AXIS));
		
		JLabel lblLocation = new JLabel("Location: ");
		JPanel pnlLocation = new JPanel();
		pnlLocation.add(lblLocation);
		pnlLocation.add(txtLocation);
		
		JLabel lblTicketPrice = new JLabel("Ticket Price: ");
		JPanel pnlTicketPrice = new JPanel();
		pnlTicketPrice.add(lblTicketPrice);
		pnlTicketPrice.add(txtTicketPrice);
		
		JLabel lblGenre = new JLabel("Genre: ");
		JPanel pnlGenre = new JPanel();
		pnlGenre.add(lblGenre);
		pnlGenre.add(txtGenre);
		
		JButton btnAdd = new JButton("Add");
		JPanel pnlAdd = new JPanel();
		addBtnMouseListenerClass addBtnMouseListener = new addBtnMouseListenerClass();
		btnAdd.addMouseListener(addBtnMouseListener);
		pnlAdd.add(btnAdd);
		
		pnlConcert.add(pnlLocation);
		pnlConcert.add(pnlTicketPrice);
		pnlConcert.add(pnlGenre);
		pnlConcert.add(pnlAdd);
		
		frame.add(pnlConcert);
		frame.setVisible(true);
	}
	@Override
	protected void setup() {
		createGui();
		addBehaviour (new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					String senderName = msg.getSender().getLocalName();
					System.out.println("Received from " + senderName + ": " + msg.getContent());
					if (senderName.matches(dataManager)) {
						String content = msg.getContent();
						if (content.matches("0"))
							JOptionPane.showMessageDialog(null, "An error occured while adding data. ");
						else if (content.matches("1"))
							JOptionPane.showMessageDialog(null, "Successfully added the record to the database. ");
						else
							JOptionPane.showMessageDialog(null, "Unknown respose: `" + content + "`.");
					}
				}
				else
					block();
			}
		});
	}
}
*/
