import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Node {
    private String serverIpAddress;
    private String nextHopIpAddress;
    private int nextHopPort;

    private boolean acceptingConnections = true;

    private ServerSocket nodeServerSocket;
    private Socket nextNodeSocket;

    public Node(String serverIpAddress, int serverPort, String nextHopIpAddress, int nextHopPort) {
        this.serverIpAddress = serverIpAddress;
        this.nextHopIpAddress = nextHopIpAddress;
        this.nextHopPort = nextHopPort;

        try {
            nodeServerSocket = new ServerSocket(serverPort, 10, InetAddress.getByName(serverIpAddress));
            System.out.println("Server is listening on " + serverIpAddress + ":" + serverPort);

            new Thread(() -> {
               while (acceptingConnections) {
                   try {
                       Socket incomingConnection = nodeServerSocket.accept();
                       handleIncomingConnections(incomingConnection);
                   } catch (IOException exception) {
                       exception.printStackTrace();
                   }
               }
            }).start();

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void handleIncomingConnections(Socket incomingConnection) {
        try {
            InputStream inputStream = incomingConnection.getInputStream();
            ByteBuffer buffer = ByteBuffer.allocate(4);

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer.array())) != -1) {
                int receivedValue = buffer.getInt(0);

                System.out.println(serverIpAddress + " received " + receivedValue + " from " + incomingConnection.getInetAddress());

                if (receivedValue >= 100) {
                    closeConnections();
                    return;
                }

                // Forward the received data to the next node
                sendData(++receivedValue);
                buffer.clear();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void sendData(int value) {
        try {
            if (nextNodeSocket == null || nextNodeSocket.isClosed() || !nextNodeSocket.isConnected()) {
                nextNodeSocket = new Socket(nextHopIpAddress, nextHopPort, InetAddress.getByName(serverIpAddress), 0);
                System.out.println(serverIpAddress + " connected to " + nextHopIpAddress);
            }

            OutputStream outputStream = nextNodeSocket.getOutputStream();
            System.out.println(serverIpAddress + " sent " + value + " to " + nextHopIpAddress);

            outputStream.write(ByteBuffer.allocate(4).putInt(value).array());

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void closeConnections() {
        try {
            acceptingConnections = false;

            if (nextNodeSocket != null && !nextNodeSocket.isClosed()) {
                nextNodeSocket.close();
            }

            if (nodeServerSocket != null && !nodeServerSocket.isClosed()) {
                nodeServerSocket.close();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}