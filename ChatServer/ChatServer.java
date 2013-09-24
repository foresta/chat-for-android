import java.io.*;
import java.net.*;
import java.util.*;

//チャットサーバー
public class ChatServer {

    //開始
    public void start(int port) {
	ServerSocket     server; //サーバーソケット
	Socket           socket; //ソケット
	ChatServerThread thread; //スレッド

	try {
	    server = new ServerSocket(port);
	    System.err.println("ChatServer started \n" + 
			       "IP Address:" + InetAddress.getLocalHost().getHostAddress() + "\n" +
			       "Port:" + port);
	    while(true) {
		try {
		    //接続待機
		    socket = server.accept();

		    //チャットサーバースレッド開始
		    thread = new ChatServerThread(socket);
		    thread.start();

		} catch (IOException e) {
		}

	    }
	} catch (IOException e) {
	    System.err.println(e);
	}
    }

    //メイン
    public static void main(String[] args) {
	ChatServer server = new ChatServer();
	server.start(8080);
    }
}