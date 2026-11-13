import java.io.*;
import java.net.*;

public class Cliente {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 8080);
        PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
        salida.println("Parte diario registrado - Obra Chorrillos");
        socket.close();
    }
}
