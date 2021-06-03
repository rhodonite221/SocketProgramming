import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.IOException;

public class ChargenClient {
    public static int DEFAULT_PORT = 19;
    public static void main(String... args) {

        int port;
        try {
            port = 8080;
        }
        catch (RuntimeException ex) {
            port = DEFAULT_PORT;
        }

        try {
            SocketAddress address = new InetSocketAddress("127.0.0.1", port);
            // 블록 모드로 실행됨. 연결이 성립되기 전까지 실행될 수 없다. 채널을 사용하여 채널 자체에 직접 데이터를 쓸 수 있다.
            SocketChannel client = SocketChannel.open(address);

            // 74바이트 용량의 버퍼 생성
            ByteBuffer buffer = ByteBuffer.allocate(74);
            // 버퍼에 데이터가 있을 경우 System.out으로 출력할 수 있다. 데이터를 읽어서 System.out으로 연결된 출력 채널로 쓸 수 있다.

            WritableByteChannel out = Channels.newChannel(System.out);

            //쓰기 전에 출력 채널이 버퍼 데이터의 끝 위치가 아닌 처음 위치에서 읽을 수 있도록 해당 버퍼에 대해 먼저 flip() 메소드를 호출한다.
            while(client.read(buffer)!=-1) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
