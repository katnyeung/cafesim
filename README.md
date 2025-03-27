# Café Simulation Game

A real-time, web-based café simulation where users can log in, take seats in virtual café rooms, and chat with an AI bartender and other users. The application features a 2D front-facing perspective showing the café wall, bartender, counter, and customer chairs.

## Overview

This application simulates a virtual café environment where users can:

- Create customized avatars based on text descriptions
- Join different café rooms with multiple seats
- Chat with an AI-powered bartender
- Interact with other users in real-time
- Move between different café rooms

![Café Simulation Preview](https://via.placeholder.com/800x400?text=Caf%C3%A9+Simulation+Preview)

## System Architecture

The application follows a client-server architecture with real-time communication capabilities:

```
┌───────────────────┐     WebSocket     ┌───────────────────────────────┐
│                   │<----------------->│                               │
│   Frontend        │     HTTP/REST     │   Spring Boot Backend         │
│   (Phaser.js)     │<----------------->│                               │
│                   │                   │                               │
└───────────────────┘                   └───────────────┬───────────────┘
                                                        │
                                                        │
                                        ┌───────────────▼───────────────┐
                                        │                               │
                                        │   Redis Cache                 │
                                        │   - Chat Messages             │
                                        │   - User Avatars              │
                                        │   - User Profiles             │
                                        │                               │
                                        └───────────────┬───────────────┘
                                                        │
                                                        │
                    ┌────────────────────┐    ┌─────────▼──────────┐
                    │                    │    │                    │
                    │  OpenAI API        │    │  Database          │
                    │  - GPT for Chat    │    │  - Users           │
                    │  - DALL-E for      │    │  - Rooms           │
                    │    Avatars         │    │  - Seats           │
                    │                    │    │  - Chat History    │
                    └────────────────────┘    └────────────────────┘
```

## Technology Stack

### Backend
- **Spring Boot**: Core framework for the web application
- **Spring WebSocket**: For real-time bidirectional communication
- **Spring Data JPA**: Database abstraction and ORM
- **Redis**: In-memory data structure store for caching
- **Hibernate**: ORM for database operations
- **H2/MySQL**: Database options (H2 for development, MySQL for production)

### Frontend
- **Phaser.js**: 2D game framework for the café environment
- **HTML5/CSS3/JavaScript**: Web technologies for UI components
- **SockJS & STOMP**: WebSocket clients for real-time communication

### AI Integration
- **OpenAI GPT-4**: Powers the AI bartender's conversational abilities
- **OpenAI DALL-E**: Generates user avatars based on text descriptions

## Core Features

### User Authentication
- Secure login/registration system
- User profile management with avatar customization

### Room System
- Multiple café rooms with different themes and capacities
- Room creation and management
- Room-specific chat channels

### AI Bartender
- Context-aware conversation using recent chat history
- Remembers users and their preferences
- Responds to group conversations intelligently

### Avatar Generation
- Text-to-image avatar creation using DALL-E
- Avatar caching in Redis for performance

### Real-time Interaction
- WebSocket-based communication between users
- Live updates for user movements and chat bubbles
- Multi-user support with presence indicators

### Caching System
- Redis-based caching for improved performance
- Cached components:
    - User profiles
    - Chat history
    - Generated avatars
    - Seat occupancy status

## Database Schema

The application uses the following data model:

### User Table
Stores user credentials and profile information:
- `id`: Unique identifier
- `username`: User's display name
- `password`: Encrypted password
- `avatar_description`: Text description for avatar generation
- `avatar_url`: URL to the generated avatar image
- `created_at`: Account creation timestamp
- `last_login`: Last login timestamp

### Room Table
Represents different café environments:
- `id`: Unique identifier
- `name`: Display name for the room
- `description`: Room description
- `capacity`: Maximum number of seats/users
- `created_at`: Room creation timestamp
- `active`: Whether the room is available

### Seat Table
Represents seating positions within rooms:
- `id`: Unique identifier
- `room_id`: Reference to the room
- `position`: Numerical position within the room
- `occupied`: Whether the seat is currently taken
- `user_id`: Reference to the seated user (if occupied)

### Chat Message Table
Stores conversation history:
- `id`: Unique identifier
- `room_id`: Reference to the room where the message was sent
- `sender_id`: Reference to the user who sent the message
- `content`: Message text
- `is_ai`: Whether the message was sent by the AI bartender
- `timestamp`: When the message was sent

## Setup and Installation

### Prerequisites
- Java 11 or higher
- Maven
- Redis server
- MySQL (for production)
- OpenAI API key

### Configuration
1. Clone the repository
2. Set up environment variables:
   ```
   OPENAI_API_KEY=your_openai_api_key
   ```

3. Configure application.properties for your environment:
   ```properties
   # Database configuration
   spring.datasource.url=jdbc:h2:mem:cafedb
   spring.datasource.username=sa
   spring.datasource.password=

   # Redis configuration
   spring.redis.host=localhost
   spring.redis.port=6379
   
   # OpenAI configuration
   openai.api.key=${OPENAI_API_KEY}
   ```

### Building and Running
1. Build the application:
   ```
   mvn clean package
   ```

2. Run the application:
   ```
   java -jar target/cafe-simulation-0.1.0.jar
   ```

3. Access the application at `http://localhost:8080`

## User Guide

### Registration and Login
1. Visit the application URL
2. Click "Register" to create a new account
3. Enter a username, password, and avatar description
4. After registration, log in with your credentials

### Joining a Room
1. Select a room from the available rooms list
2. Click "Join Room" to enter the selected café

### Interacting in the Café
1. Click on an empty seat to sit down
2. Use the chat input to converse with the AI bartender and other users
3. Your messages will appear in chat bubbles above your avatar
4. The AI bartender will respond to your messages in context

### Changing Rooms
1. Click "Leave Room" to exit your current room
2. Select a new room from the rooms list
3. Click "Join Room" to enter the new café

## Development

### Adding New Features
- **New Room Themes**: Add new background images and update the Room entity
- **Custom Drinks**: Extend the AI service to handle drink orders and visuals
- **Mini-games**: Implement simple games that users can play in the café

### Extending the AI Bartender
The AI bartender uses OpenAI's GPT model with a context window of the 5 most recent messages. To extend its capabilities:

1. Modify the prompt in `AIService.java`
2. Adjust the context window size by changing `CONTEXT_MESSAGE_COUNT`
3. Add specialized responses for certain user inputs

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Credits
- OpenAI for GPT and DALL-E APIs
- Phaser.js for the game framework
- Spring Boot for the backend framework