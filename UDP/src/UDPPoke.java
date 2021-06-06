import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;


public class UDPPoke {
    private int bufferSize;
    private int timeout;
    private InetAddress host;
    private int port;

    public UDPPoke(InetAddress host, int port, int bufferSize, int timeout) {
        this.bufferSize = bufferSize;
        this.host = host;
        if(port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port out of range");
        }

        this.port = port;
        this.timeout = timeout;
    }

    public UDPPoke(InetAddress host, int port, int bufferSize) {
        this(host, port, bufferSize, 30000);
    }

    public UDPPoke(InetAddress host, int port) {
        this(host, port, 8192, 30000);
    }

    public byte[] poke() {
        try (DatagramSocket socket = new DatagramSocket(0)) {
            DatagramPacket outgoing = new DatagramPacket(new byte[1],1, host, port);
            socket.connect(host, port);
            socket.send(outgoing);
            DatagramPacket incoming = new DatagramPacket(new byte[bufferSize], bufferSize);
            //다음 라인은 응답이 도착할 때까지 블록된다.
            socket.receive(incoming);
            int numBytes = incoming.getLength();
            byte[] response = new byte[numBytes];
            System.arraycopy(incoming.getData(), 0, response, 0, numBytes);
            return response;
        }
        catch (IOException ex) {
            return null;
        }
    }

    public static void main(String... args) {
        InetAddress host;
        try {
            host = InetAddress.getByName("rama.poly.edu");
        }catch (RuntimeException | UnknownHostException ex) {
            return;
        }
        int port =13;
        port = 8080;

        try {
            UDPPoke poker = new UDPPoke(host, port);
            byte[] response = poker.poke();
            if(response==null) {
                System.out.println("No response within allotted time");
                return;
            }

            String result = new String(response, "US-ASCII");
            System.out.println(result);
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }
}

