package theory.jdbc.exception.translator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static theory.jdbc.connection.ConnectionConst.*;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import theory.jdbc.domain.Member;
import theory.jdbc.repository.ex.MyDbException;
import theory.jdbc.repository.ex.MyDuplicateKeyException;

@Slf4j
public class ExTranslatorV1Test {

    Repository repository;
    Service service;    

    @BeforeEach
    void beforeEach() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicateKeySave() {
        service.create("myId");
        service.create("myId");
    }

    @Slf4j
    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        public void create(String memberId) {
            try {
                repository.save(new Member(memberId, 0));
                log.info("saveId={}", memberId);
            } catch (MyDuplicateKeyException e) {
                log.info("키 중복, 별도 처리");
                String retryId = generateNewId(memberId);
                log.info("retryId={}", retryId);
                repository.save(new Member(retryId, 0));
            } catch (MyDbException e) {
                log.info("데이터 접근 계층 예외", e);
                throw e;
            }
        }


        private String generateNewId(String memberId) {
            return memberId + "_" + UUID.randomUUID().toString().substring(0, 4);
        }
    }

    @RequiredArgsConstructor
    static class Repository {

        private final DataSource dataSource;

        public Member save(Member member)  {
            String sql = "Insert into member(member_id, money) values(?, ?)";
            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
                return member;
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    throw new MyDuplicateKeyException(e);
                }
                throw new MyDbException(e);

            } finally {
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(con);
            }
        }
    }
}
