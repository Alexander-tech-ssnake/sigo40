import java.io.*;
import java.net.*;

public class Servidor {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Servidor central listo en puerto 8080...");
        Socket socket = serverSocket.accept();
        BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String mensaje = entrada.readLine();
        System.out.println("Mensaje recibido de la obra: " + mensaje);
        socket.close();
        serverSocket.close();
    }
}
// Revision final - Constructora Andes Sur 4.0
