package com.uc.framework.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JDBC {
    @Value(value = "${spring.datasource.url}")
    private String jdbcUrl;
    @Value(value = "${spring.datasource.username}")
    private String user;
    @Value(value = "${spring.datasource.password}")
    private String password;
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    public static void free(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (conn != null)
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    public int exec(String sql) throws SQLException {
        Connection conn = this.getConnection();
        PreparedStatement p;
        p = (PreparedStatement) conn.prepareStatement(sql);
        int rs = p.executeUpdate(sql);
        free(conn, p, null);
        return rs;
    }

}
