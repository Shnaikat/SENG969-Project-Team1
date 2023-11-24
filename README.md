# SENG969-Project-Team1
// This repository contains the source code for Assignment 3 in the SENG696 course.  

// In this document, we provide a detailed description of our implementation for the Multi Agent MCRS, including the sequence for starting the agents:

1. **Data Manager Agent:****
Name: dataManager. This agent listens for the Provider (VenueAgent) to provide the upcoming concert details (location, ticketPrice, and genre).

2. **VenueAgent:**  
- Name: Any name is acceptable, for example, Provider1.
- This agent should receive three arguments in the following format: location, ticketPrice, genre (e.g., Downtown, 50, Classic).
- The dataManager receives these arguments and updates the concerts table accordingly.

4. The admin agent:
   - is responsible for registering new users in the users table, and handling the errors in the data fields if provided, and verifying if the registered user is a new user or already existing user.
   - 
6. Having a concert seeker agent (a representation for the user GUI).This agent will send to the recommender a concert seeking request by providing the following arguments: email, location preferences, Ticket price, and genre.

7. The Recommender agent receives a message with the user's email, location preference, ticket price, and genre, from the concert seeker agent. Then, send a message to the AdminAgent to check if the email exists.
If the email exists, RecommenderAgent will:
Insert the preferences into the preferences table.
Proceed with finding a matching concert.
If the email does not exist, it will prompt the user to register.

8. The invitation request will come through the ConcertSeeker gui. After the concert seeker receives a concert recommendation, a new button will appear to provide “Find friends'' service. Once the Concert user clicks this button, the preferences of this seeker will be sent to the invitation agent, which in turn, will come back with any potential friends (names and emails) as this information is associated with similar preferences. Then, updating the friends table accordingly.
