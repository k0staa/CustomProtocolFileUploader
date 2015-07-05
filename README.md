This document presents the following:
1. The list of features contained in the program.
2. Brief description of the project and user manual.
3. The list of commands used for communication in the program.

1. The program includes the following functions:
The program operates in multihost (at least three hosts).
The program works using TCP network protocol.
Clients exchange files list with MD5 checksums.
The application supports PUSH and PULL.

2. The server runs by default on port 10500 and this port is also enabled by default in client programs. The application also uses port 10100, and is opened between clients only when performing operations PUSH and PULL. The IP address to connect to the server by default is set to 127.0.0.1, but you can set it yourself by typing the address in the box located on the left side of the "CONNECT" button. In the program there is a simple login implemented. The program does not allow to connect to server more than three users, as well as connection of user with the same login than currently logged.
After entering IP address and press "CONNECT" server should send a message "Connection established, you are Client # ...", you can see all messages in log located on the bottom of program. After a successful connection, you can choose a working directory by pressing the "CHOOSE FOLDER." Files in this directory are displayed in the box labeled "Client" on the left side of the window. Note: Early select a directory with no connection to the server is disabled. Selecting a folder with lots of data can resulting in a slow program working, blame for this is reading MD5 checksums for all the files in dircectory.
At the time when we already have a connection to the server and select our working directory, list of files (from the working directory) is automatically sent to the server. If other users also have chosen catalogs, their contents will appear on the right side of the application window tabs "Remote1", "Remote2", "Remote3". Pressing remote list tabs automatically refresh lists. By checking the file name in the list of another remote client there is a MD5 checksum showed in log.
PULL - you can download file that you previously checked in the tab displays remote clients files. We can observe log to check the progress of the operation.
PUSH - you can upload any file shared by our directory to another client, including the customer toyou can chose to which client you want to send data by  select the remote client tab. We can observe log to check the progress of the operation.
In order to disconnect from the server, press the "DISCONNECT" button.

3. The list of messages used in protocol of the application:
111 - request the customer to send a list of files -> server response: 110 - denial, 112 - consent.
221 - customer request for downloading a list of other clients -> Server answer: 220 - denial, 222 - consent.
331 - ask the remote client for permision to download a file  client -> server response: available 1-, 0-not available -> answer of remote client 330 - refusal / issue, 332 - the consent / sending.
441 - client request to push a file to another client -> server response: available 1-, 0-not available -> answer of remote client 440 - refusal / issue 442 - consent / downloading.
999 - Statement of client that is about to  disconnect from the server -> after receiving, the server closes the connection.