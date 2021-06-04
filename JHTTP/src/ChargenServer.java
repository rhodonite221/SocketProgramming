import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.io.IOException;


public class ChargenServer {
    public static int DEFAULT_PORT = 19;
    public static void main(String... args) {
        int port=8080;
        System.out.println("Listening for connections on port "+port);

        byte[] rotation = new byte[95*2];
        for(byte i=' '; i<= '~'; i++) {
            rotation[i - ' '] = i;
            rotation[i+95-' '] = i;
        }

        ServerSocketChannel serverChannel;
        Selector selector;

        try {
            serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            ss.bind(address);
            serverChannel.configureBlocking(false);
            selector = Selector.open();
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        while (true) {
            try {
                selector.select();
            }
            catch (IOException ex) {
                ex.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if(key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("Accepted connection from "+client);
                        client.configureBlocking(false);
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
                        ByteBuffer buffer = ByteBuffer.allocate(74);
                        buffer.put(rotation, 0, 72);
                        buffer.put((byte) '\r');
                        buffer.put((byte)'\n');
                        buffer.flip();
                        key2.attach(buffer);
                    }
                    else if(key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        if(!buffer.hasRemaining()) {
                            // pos to 0
                            buffer.rewind();
                            int first = buffer.get();
                            buffer.rewind();
                            int position = first-' '+1;
                            //rotation에서 buffer로 데이터 복사
                            buffer.put(rotation, position, 72);
                            // 버퍼의 마지막에 라인 구분자를 저장한다.
                            buffer.put((byte)'\r');
                            buffer.put((byte)'\n');
                            buffer.flip();
                        }
                        client.write(buffer);
                    }
                }
                catch (IOException ex) {
                    key.channel();
                    try {
                        key.channel().close();
                    }
                    catch (IOException cex) {}
                }
            }
        }
    }
}
