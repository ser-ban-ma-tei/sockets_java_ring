import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ClosedConnNode {
    private String nextHopIpAddress;
    private int nextHopPort;

    private ServerSocket serverSocket;

    public ClosedConnNode(String serverIpAddress, int serverPort, String nextHopIpAddress, int nextHopPort) {
        this.nextHopIpAddress = nextHopIpAddress;
        this.nextHopPort = nextHopPort;

        try {
            serverSocket = new ServerSocket(serverPort, 1, InetAddress.getByName(serverIpAddress));
            System.out.println("Server is listening on " + serverIpAddress + ":" + serverPort);

            new Thread(() -> {
                while (true) {
                    try {
                        Socket incomingConnection = serverSocket.accept();
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
            int receivedValue = Integer.parseInt(new String(inputStream.readAllBytes()).trim());
            System.out.println("Received value: " + receivedValue + " from " + incomingConnection.getInetAddress());

            if (receivedValue == 100)
                return;

            sendData(++receivedValue);
            incomingConnection.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void sendData(int value) {
        try {
            Socket clientSocket = new Socket(nextHopIpAddress, nextHopPort);
            System.out.println("Connected to " + nextHopIpAddress + ":" + nextHopPort);

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(String.valueOf(value).getBytes());
            System.out.println("Sent data: " + value + " to " + nextHopIpAddress + ":" + nextHopPort);

            clientSocket.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}