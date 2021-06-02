import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;

public class RequestProcessor implements Runnable {
    private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());

    private File rootDirectory;
    private String indexFileName = "index.html";
    private Socket connection;

    public RequestProcessor(File rootDirectory, String indexFileName, Socket connection) {
        if(rootDirectory.isFile()) {
            //에러 타입에 맞는 예외를 출력한다.
            throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
        }
        try {
            rootDirectory = rootDirectory.getCanonicalFile();
        }
        catch (IOException ex) {

        }
        this.rootDirectory = rootDirectory;

        if(indexFileName!=null) this.indexFileName = indexFileName;
        this.connection = connection;
    }

    @Override
    public void run() {
        //보안 검사
        String root = rootDirectory.getPath();
        try {
            //출력 스트림 생성
            OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
            //출력
            Writer out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(
                    new BufferedInputStream(
                            connection.getInputStream()
                    ), "US-ASCII"
            );
            StringBuilder requestLine = new StringBuilder();
            while (true) {
                int c = in.read();
                if(c=='\r' || c =='\n') break;
                requestLine.append((char)c);
            }

            String get = requestLine.toString();

            logger.info(connection.getRemoteSocketAddress()+" "+get);
            String[] tokens = get.split("\\s+");
            String method = tokens[0];
            String version = "";
            if(method.equals("GET")) {
                String fileName = tokens[1];
                if(fileName.endsWith("/")) fileName+=indexFileName;
                String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
                if (tokens.length>2) {
                    version=tokens[2];
                }

                File theFile = new File(rootDirectory, fileName.substring(1, fileName.length()));

                if(theFile.canRead()
                    //클라이언트가 도큐멘트 루트를 벗어나지 못하도록 한다
                        && theFile.getCanonicalPath().startsWith(root)
                ) {
                    byte[] theData = Files.readAllBytes(theFile.toPath());
                    if(version.startsWith("HTTP/")) {
                        //MIME 헤더 전송
                        sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
                    }
                    //파일을 전송한다. 이미지나 다른 바이너리 데이터 전송을 위해
                    // writer 대신 내부 출력 스트림을 사용한다.
                    raw.write(theData);
                    raw.flush();
                } else {    // 파일을 찾을 수 없는 경우
                    String body = new StringBuilder("<HTML>\r\n")
                            .append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
                            .append("</HEAD>\r\n")
                            .append("<BODY>")
                            .append("<H1>HTTP Error 404: File Not Found</H1>")
                            .append("</BODY></HTML>\r\n").toString();
                    if(version.startsWith("HTTP/")) {   //MIME헤더 전송
                        sendHeader(out, "HTTP/1.0 404 File Not Found", "text/html; charset=utf-8", body.length());
                    }
                    out.write(body);
                    out.flush();
                }
            }
            else {  // "GET" 메소드가 아닌 경우
                String body = new StringBuilder("<HTML>\r\n")
                        .append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
                        .append("</HEAD>\r\n")
                        .append("<BODY>")
                        .append("<H1>HTTP Error 501: Not Implemented</H1>")
                        .append("</BODY></HTML>\r\n").toString();
                if(version.startsWith("HTTP/")) {   //MIME헤더 전송
                    sendHeader(out, "HTTP/1.0 501  Not Implemented", "text/html; charset=utf-8", body.length());
                }
                out.write(body);
                out.flush();

            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error talking to "+connection.getRemoteSocketAddress(), ex);
        }
        finally {
            try {
                connection.close();
            }
            catch (IOException ex) {}
        }
    }

    private void sendHeader(Writer out, String responseCode, String contentType, int length) throws IOException {
        out.write(responseCode+"\r\n");
        Date now = new Date();
        out.write("Date: "+now+"\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-length: "+length+"\r\n");
        out.write("Content-type: "+contentType+"\r\n\r\n");
        out.flush();
    }
}
