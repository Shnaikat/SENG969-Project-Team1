# SENG969-Project-Team1

**This repository contains the source code for Assignment 3 in the SENG696 course. It includes SQL files for the database tables and an updated version of assignment 2 with inter-agent messages in XML. This document provides a detailed description of our implementation for the Multi-Agent MCRS, along with the sequence for starting the agents.**

## Agent Initialization Sequence

1. **Data Manager Agent:**
   - **Agent Name:** `dataManager`
   - **Role:** Listens for the VenueAgent to provide upcoming concert details such as location, ticketPrice, and genre.
   - **Expected Arguments:** None
   - **Screenshot Placeholder:**
    ![Data Manager GUI](https://raw.githubusercontent.com/Shnaikat/SENG969-Project-Team1/main/screenShots/DM-Agent.jpg?token=GHSAT0AAAAAACKLAKD7MP3MNN3VRHRI4BAEZLSDJTA)


2. **VenueAgent:**
   - **Agent Names:** Any name is acceptable, e.g., `Provider1`.
   - **Role:** Sends concert details to the Data Manager Agent.
   - **Expected Arguments:** location, ticketPrice, genre
   - **Screenshot Placeholder:**
     ![VenueAgent GUI](https://raw.githubusercontent.com/Shnaikat/SENG969-Project-Team1/main/screenShots/VenueAgent.png?token=GHSAT0AAAAAACKLAKD6T5OYDAVHTZN4FLWMZLSDNDQ)

3. **Admin Agent:**
   - **Agent Name:** `AdminAgent`
   - **Role:** Creates user profiles and verifies user information.
   - **Expected Arguments:** None
   - **Screenshot of the Admin GUI:**
     ![Admin GUI](https://raw.githubusercontent.com/Shnaikat/SENG969-Project-Team1/main/screenShots/AdminGUI.png)

4. **ConcertSeeker Agent:**
   - **Agent Names:** Any name is acceptable.
   - **Role:** Sends concert seeking requests and enables the 'Find friends' service.
   - **Expected Arguments:** email, location preferences, ticketPrice, genre
   - **Screenshot Placeholder:**
   - ![ConcertSeeker GUI](https://raw.githubusercontent.com/Shnaikat/SENG969-Project-Team1/main/screenShots/Seeker-positiveResponse.png?token=GHSAT0AAAAAACKLAKD7QYUSWX7VBF3TR4JKZLSDMVQ)

5. **Recommender Agent:**
   - **Agent Name:** `RecommenderAgent`
   - **Role:** Processes user preferences and finds matching concerts.
   - **Expected Arguments:** None
   - **Screenshot Placeholder:**
   - ![Recommender GUI](https://raw.githubusercontent.com/Shnaikat/SENG969-Project-Team1/main/screenShots/RecommenderLog.jpg?token=GHSAT0AAAAAACKLAKD6B4ROQ6KGU5VWED2MZLSDL6A)

6. **InvitationAgent:**
   - **Agent Name:** `InvitationAgent`
   - **Role:** Provides 'Find friends' service and updates the friends table.
   - **Expected Arguments:** None
   - **Screenshot Placeholder:**
   - ![InvitationAgent GUI](https://raw.githubusercontent.com/Shnaikat/SENG969-Project-Team1/main/screenShots/InvitationAgent.jpg?token=GHSAT0AAAAAACKLAKD6R65QEGHN5AJA3SRAZLSDLRA)

## Additional Notes
- The arguments provided are examples. Replace them with actual values as needed.

---

For any issues during the initialization, please refer to our troubleshooting guide or contact the development team.
