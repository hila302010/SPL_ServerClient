# TFTP Server-Client Implementation

A multi-threaded Java implementation of a TFTP (Trivial File Transfer Protocol) server and client system supporting file upload, download, directory listing, and user management.

## 📋 Project Overview

This project implements a custom TFTP protocol with the following features:

- **Multi-threaded server** handling multiple concurrent clients
- **User authentication system** with login/logout functionality
- **File transfer operations** (upload/download) with chunked data transmission
- **Directory listing** and file management capabilities
- **File deletion** with broadcast notifications to all connected users
- **Custom packet encoding/decoding** for network communication

## 🏗️ Architecture

The project is divided into two main components:

### Server (`/server`)

- **TftpServer.java** - Main server application running on port 7777
- **TftpProtocol.java** - Implements the TFTP protocol logic
- **TftpEncoderDecoder.java** - Handles packet serialization/deserialization
- **TftpConnections.java** - Manages client connections and user sessions
- **Files/** - Directory containing server files available for transfer

### Client (`/client`)

- **TftpClient.java** - Main client application with multi-threaded architecture
- **TftpProtocol.java** - Client-side protocol implementation
- **KeyBoard.java** - Handles user input in separate thread
- **Listening.java** - Manages server communication in separate thread

## 🚀 Supported Operations

| Operation | Code | Description                              |
| --------- | ---- | ---------------------------------------- |
| **RRQ**   | 1    | Read Request - Download file from server |
| **WRQ**   | 2    | Write Request - Upload file to server    |
| **DATA**  | 3    | Data transmission with 512-byte chunks   |
| **ACK**   | 4    | Acknowledgment packets                   |
| **ERROR** | 5    | Error reporting                          |
| **DIRQ**  | 6    | Directory listing request                |
| **LOGRQ** | 7    | User login request                       |
| **DELRQ** | 8    | File deletion request                    |
| **BCAST** | 9    | Broadcast notifications                  |
| **DISC**  | 10   | Disconnect from server                   |

## 🛠️ Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6+

### Building the Project

#### Server

```bash
cd server
mvn clean compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.tftp.TftpServer"
```

#### Client

```bash
cd client
mvn clean compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.tftp.TftpClient"
```

### Running the Application

1. **Start the Server**

   ```bash
   cd server
   java -cp target/classes bgu.spl.net.impl.tftp.TftpServer
   ```

   Server will start on port 7777

2. **Connect with Client**
   ```bash
   cd client
   java -cp target/classes bgu.spl.net.impl.tftp.TftpClient
   ```

### Client Commands

After connecting to the server, use these commands:

```bash
LOGRQ <username>          # Login with username
DELRQ <filename>          # Delete file from server
RRQ <filename>            # Download file from server
WRQ <filename>            # Upload file to server
DIRQ                      # List server directory
DISC                      # Disconnect from server
```

## 🗂️ File Structure

```
SPL_ServerClient/
├── server/
│   ├── src/main/java/bgu/spl/net/
│   │   ├── api/              # Protocol interfaces
│   │   ├── impl/tftp/        # TFTP implementation
│   │   └── srv/              # Server infrastructure
│   ├── Files/                # Server file storage
│   └── pom.xml
├── client/
│   ├── src/main/java/bgu/spl/net/impl/tftp/
│   └── pom.xml
└── README.md
```

## 🔧 Technical Details

- **Protocol**: Custom TFTP implementation over TCP
- **Threading**: Thread-per-client model for server
- **Packet Size**: Maximum 512 bytes per data packet
- **Encoding**: Binary packet format with custom encoder/decoder
- **File Storage**: Server maintains files in dedicated `Files/` directory
- **User Management**: Concurrent user sessions with username-based authentication

## 🎯 Key Features

- **Concurrent File Transfers**: Multiple clients can upload/download simultaneously
- **User Session Management**: Login system prevents unauthorized access
- **Broadcast Notifications**: File deletion events are broadcast to all connected users
- **Error Handling**: Comprehensive error reporting and graceful connection handling
- **Chunked Transfer**: Large files are automatically split into manageable chunks

## 👨‍💻 Development

This project was developed as part of the SPL (Systems Programming Laboratory) course, demonstrating:

- Network programming with Java sockets
- Multi-threading and concurrent programming
- Protocol design and implementation
- Client-server architecture patterns

---

**Course**: Systems Programming Laboratory (SPL)  
**Language**: Java 8+  
**Build Tool**: Maven  
**Protocol**: Custom TFTP over TCP
