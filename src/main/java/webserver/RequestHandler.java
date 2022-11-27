package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import model.HttpRequest;
import model.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    private Map<String, Controller> requestMap = new HashMap();

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;

        requestMap.put("/user/create", new CreateUserController());
        requestMap.put("/user/login", new LoginController());
        requestMap.put("/user/list", new ListUserController());
    }

    private Controller getRequestController(String url) {
        Controller controller = requestMap.get(url);
        return controller == null ? new AbstractController() : controller;
    }


    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            HttpRequest httpRequest = new HttpRequest(in);
            log.info(httpRequest.toString());

            Controller controller = getRequestController(httpRequest.getPath());
            controller.service(httpRequest, new HttpResponse(out));

        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
