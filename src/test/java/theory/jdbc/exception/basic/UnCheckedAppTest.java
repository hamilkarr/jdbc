package theory.jdbc.exception.basic;

import java.net.ConnectException;
import java.sql.SQLException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnCheckedAppTest {

    @Test
    void checked() {
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(() -> controller.request())
            .isInstanceOf(Exception.class);
    }   

    @Test
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            log.error("ex", e);
        }
    }
    

    static class Controller {
        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            networkClient.call();
            repository.call();
        }
    }

    static class Repository {
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        }

        public void runSQL () throws SQLException {
            throw new SQLException("DB 예외 발생");
        }
    }

    static class NetworkClient {
        public void call() {
            try {
                runConnect();
            } catch (ConnectException e) {
                throw new RuntimeConnectException(e);
            }
        }

        public void runConnect() throws ConnectException {
            throw new ConnectException("연결 실패");
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(Throwable cause) {
            super(cause);
        }
    }
}
