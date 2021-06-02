import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class JHTTP {
    private static final Logger logger = Logger.getLogger(JHTTP.class.getCanonicalName());
    private static final int NUM_THREADS = 50;
    private static final String INDEX_FILE = "index.html";

    private final File rootDirectory;
    private final int port;

    public JHTTP(File rootDirectory, int port) throws IOException {
        if(!rootDirectory.isDirectory()) {
            throw new IOException(rootDirectory+" does not exists as a directory");
        }
        this.rootDirectory = rootDirectory;
        this.port = port;
    }

    public void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("Accepting connections on port "+server.getLocalPort());
            logger.info("Document Root: "+rootDirectory);

            while (true) {
                try {
                    Socket request = server.accept();
                    Runnable r = new RequestProcessor(rootDirectory, INDEX_FILE, request);
                    pool.submit(r);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Error accepting connection", ex);
                }
            }
        }
    }

    public static void main(String... args) {
        //도큐먼트 루트를 구한다
        File docroot;
        try {
            docroot = new File("./");
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Usage: java JHTTP docroot port");
            return;
        }
        //대기할 포트 설정
        int port;
        try {
            port = 8080;
            if(port<0 || port > 65535) port=80;
        }
        catch (RuntimeException ex) {
            port=80;
        }

        try {
            JHTTP webserver = new JHTTP(docroot, port);
            webserver.start();
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Server could not start", ex);
        }
    }
}
