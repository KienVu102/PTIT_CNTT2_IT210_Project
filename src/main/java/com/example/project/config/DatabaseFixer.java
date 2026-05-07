package com.example.project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DatabaseFixer {

    @Bean
    public CommandLineRunner dropUniqueIndexOnTicketSeat(DataSource dataSource) {
        return args -> {
            try (Connection c = dataSource.getConnection()) {
                List<String> uniqueIndexes = new ArrayList<>();
                try (PreparedStatement ps = c.prepareStatement(
                        "SHOW INDEX FROM tickets WHERE Column_name = 'seat_id' AND Non_unique = 0")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String indexName = rs.getString("Key_name");
                            if (indexName != null && !indexName.equalsIgnoreCase("PRIMARY")) {
                                uniqueIndexes.add(indexName);
                            }
                        }
                    }
                }

                for (String idx : uniqueIndexes) {
                    try (Statement st = c.createStatement()) {
                        st.execute("ALTER TABLE tickets DROP INDEX `" + idx + "`");
                        System.out.println("[DB] Dropped UNIQUE index on tickets.seat_id: " + idx);
                    }
                }
            } catch (Exception e) {
                System.err.println("[DB] Could not check/drop UNIQUE index on tickets.seat_id: " + e.getMessage());
            }
        };
    }
}

