import java.io.*;
import javax.net.ssl.*;

public class HTTPSClient {

    public static void main(String... args) {
        int port=443;
        String host = "www.usps.com";

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = null;

        try {
            socket = (SSLSocket) factory.createSocket(host, port);

            //모든 암호화 조합을 사용하도록 설정
            String[] supported = socket.getSupportedCipherSuites();
            socket.setEnabledCipherSuites(supported);

            Writer out = new OutputStreamWriter(socket.getOutputStream(), "utf-8");
            //https는 GET요청 시 전체 URL을 사용해야 한다.
            out.write("GET http://" + host + "/ HTTP/1.1\r\n");
            out.write("Host: " + host + "\r\n");
            out.write("\r\n");
            out.flush();

            //응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //헤더 읽기
            String s;
            while(!(s=in.readLine()).equals("")) {
                System.out.println(s);
            }
            System.out.println();

            //길이 읽기
            String contentLength = in.readLine();
            int length = Integer.MAX_VALUE;
            try {
                length = Integer.parseInt(contentLength.trim(), 16);
            }
            catch (NumberFormatException ex) {
                //서버가 응답 본문의 첫째 줄에 content-length를 보내지 않은 경우
            }
            System.out.println(contentLength);

            int c;
            int i=0;
            while((c=in.read())!=-1 && i++ < length) {
                System.out.write(c);
            }

            System.out.println();
        }
        catch (IOException ex) {
            System.err.println(ex);
        } finally {
            try {
                if(socket!=null) socket.close();
            }
            catch (IOException e) {

            }
        }
    }

}
