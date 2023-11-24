# SENG969-Project-Team1
// This repository contains the source code for Assignment 3 in the SENG696 course.  

// In this document, we provide a detailed description of our implementation for the Multi Agent MCRS, including the sequence for starting the agents:

1. **Data Manager Agent:**<br>
Name: dataManager. This agent listens for the Provider (VenueAgent) to provide the upcoming concert details (location, ticketPrice, and genre).

2. **VenueAgent:** <br>
   - Name: Any name is acceptable, for example, Provider1.
   - This agent should receive three arguments in the following format: location, ticketPrice, genre (e.g., Downtown, 50, Classic).
   - The dataManager receives these arguments and updates the concerts table accordingly.

3. **The admin agent:**<br>
   - Create profile for the new user, and insert the proper user information into the users table, and handling the errors in the data fields if provided. <br>
   - Verifying if the any user, who is interacting with the MCRS, is a new user or already existing user.
     
6. **ConcertSeeker Agent:** <br>
   - Seek concert:send to the recommender a concert seeking request by providing the following arguments: email, location preferences, Ticket price, and genre.<br>
   - Find friends: If the concert seeker is eligible for seeking concert service (this happened if he is a registered user), and he provided a concert preferences that are matched with an upcoming concert, Find friends service will be enabled for the seeker. <br>
   
7. **Recommender Agent:**<br>
    receives a message with the user's email, location preference, ticket price, and genre, from the concert seeker agent. Then, send a message to the AdminAgent to check if the email exists.
If the email exists, RecommenderAgent will:
Insert the preferences into the preferences table.
Proceed with finding a matching concert.
If the email does not exist, it will prompt the user to register.

9. The invitation request will come through the ConcertSeeker gui. After the concert seeker receives a concert recommendation, a new button will appear to provide “Find friends'' service. Once the Concert user clicks this button, the preferences of this seeker will be sent to the invitation agent, which in turn, will come back with any potential friends (names and emails) as this information is associated with similar preferences. Then, updating the friends table accordingly.
