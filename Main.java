
public class Main {
    public static void main(String[] args) {
        OpenConnNode node1 = new OpenConnNode("127.0.0.1", 2345, "127.0.0.2", 3456);
        new OpenConnNode("127.0.0.2", 3456, "127.0.0.3", 4567);
        new OpenConnNode("127.0.0.3", 4567, "127.0.0.1", 2345);

        node1.sendData(1);
    }
}