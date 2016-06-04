package org.age.hz.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.MembershipListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class HazelcastConfiguration {

    @Value("#{'${network.tcp.members:127.0.0.1}'.split(',')}")
    private List<String> tcpMembers;

    @Value("${network.use.multicast.discovery:false}")
    private boolean useMulticastDiscovery;

    @Bean
    public Config hazelcastConfig(MembershipListener membershipListener) {
        Config config = new Config();

        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(useMulticastDiscovery);

        if (!useMulticastDiscovery) {
            TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
            tcpIpConfig.setEnabled(true);
            tcpMembers.forEach(tcpIpConfig::addMember);
        }

        config.getProperties().put("hazelcast.logging.type", "slf4j");
        config.getProperties().put("hazelcast.shutdownhook.enabled", "false");

        config.addListenerConfig(new ListenerConfig(membershipListener));

        return config;
    }

}
