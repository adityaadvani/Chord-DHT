# Chord- distributed hash table for file storage and access

## How to compile-
Open the terminal, go to the directory where the files are stored and run the following command: 'javac *.java'
this will compile all the java source files.

## How to run-
Open the terminal and after compilation,
to run the node instance, run the command 'java node'
to run the bootstrap server instance, run the command 'java bootstrap'

## How to use-
After running the bootstrap server instance, it will give out a public ip address that the nodes can use to connect to it.
Enter this ip in the node instances' terminal when asked for the bootstrap server ip at the beginning.
Once you have done this, you will then have entered the system.

## Commands-
The system accepts 6 commands:
1. join - 
  * connects the node to the network
2. leave - 
  * removes the node from the network
3. view - 
  * displays the details of the node
4. insert [file name] - 
  * inserts the entered file into the system via the optimal path and displays the selected track
5. search [file name] - 
  * searches for the entered file in the system via the optimal path and displays the selected track
6. exit - 
  * leave the system

Any command apart from the above mentioned will result in an invalid input.

## How it works-
When a node joins or leaves the network, the files are redistributed automatically between itself and its successor node. When the last node leaves the network, all the files are lost.
