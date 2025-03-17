package theory.jdbc.exception.translator;

import static theory.jdbc.connection.ConnectionConst.PASSWORD;
import static theory.jdbc.connection.ConnectionConst.URL;
import static theory.jdbc.connection.ConnectionConst.USERNAME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringExceptionTranslatorTest {
    DataSource dataSource;

    @BeforeEach
    void beforeEach() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void exceptionTranslator() {
        String sql = "select bad grammar";

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.executeQuery();
        } catch (SQLException e) {
            SQLExceptionTranslator translator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            DataAccessException resultEx = translator.translate("select", sql, e);
            log.info("resultEx", resultEx);
            Assertions.assertThat(resultEx).isInstanceOf(BadSqlGrammarException.class);
        }
    }
}
