# SENG969-Project-Team1
// This repo represents the src code for Assignmet3/ SENG696 course
// The implementation consists the following:
//In this document we are providing the detailed description for what we have implemented to get the Multi Agent MCRS working:
1. VenueAgent and Data Manager Agent:
2. 
3. As we mentioned that our system will recommend an upcoming music concert based on the user's preferences, We added a new agent that is called "VenueAgent". This agent acts as a service provider, meaning he is the one that knows what are the upcoming concerts, and these concerts information will be inserted to our DB through the dataManager agent.So, the dataManager agent is the one that is responsible for getting this information and inserting them to the concerts table.
2.The admin agent is responsible for registering new users in the users table, and handling the errors in the data fields if provided, and verifying if the registered user is a new user or already existing user.
4. Having a concert seeker agent (a representation for the user GUI).This agent will send to the recommender a concert seeking request by providing the following arguments: email, location preferences, Ticket price, and genre.

5. The Recommender agent receives a message with the user's email, location preference, ticket price, and genre, from the concert seeker agent. Then, send a message to the AdminAgent to check if the email exists.
If the email exists, RecommenderAgent will:
Insert the preferences into the preferences table.
Proceed with finding a matching concert.
If the email does not exist, it will prompt the user to register.

6. The invitation request will come through the ConcertSeeker gui. After the concert seeker receives a concert recommendation, a new button will appear to provide “Find friends'' service. Once the Concert user clicks this button, the preferences of this seeker will be sent to the invitation agent, which in turn, will come back with any potential friends (names and emails) as this information is associated with similar preferences. Then, updating the friends table accordingly.
