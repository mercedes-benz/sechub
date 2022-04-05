// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

/**
 * Special configuration for H2 testing.<br>
 * <br>
 * Only available for H2 profile usage. Also developers must enable the
 * dedicated variant (TCP connection or web server variant). Can be done by
 * setting system properties: either "h2.web.enabled" or "h2.tcp.enabled"
 *
 * @author Albert Tregnaghi
 *
 */
@Configuration
@Profile(Profiles.H2)
public class H2ServerConfiguration {

    // TCP port for remote connections, default 9092
    @Value("${h2.tcp.port:9092}")
    private String h2TcpPort;

    // Web port, default 8082
    @Value("${h2.web.port:8082}")
    private String h2WebPort;

    /**
     * TCP connection to connect with SQL clients to the embedded h2 database.
     *
     * Connect to "jdbc:h2:tcp://localhost:9092/mem:db", userid "sa", password
     * empty. Connect to "jdbc:h2:tcp://localhost:9092/mem:testdb", userid "sa",
     * password empty.
     */
    @Bean
    @ConditionalOnExpression("${h2.tcp.enabled:false}")
    public Server h2TcpServer() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", h2TcpPort).start();
    }

    /**
     * Web console for the embedded h2 database.
     *
     * Go to http://localhost:8082 and connect to the database "jdbc:h2:mem:db",
     * userid "sa", password empty. Go to http://localhost:8082 and connect to the
     * database "jdbc:h2:mem:testdb", userid "sa", password empty.
     */
    @Bean
    @ConditionalOnExpression("${h2.web.enabled:false}")
    public Server h2WebServer() throws SQLException {
        return Server.createWebServer("-web", "-webAllowOthers", "-webPort", h2WebPort).start();
    }

}