package org.age.hz.cluster;

import com.google.common.eventbus.EventBus;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.age.hz.core.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ClusterMembershipListener implements MembershipListener {

    private static final Logger log = LoggerFactory.getLogger(ClusterMembershipListener.class);

    private final EventBus eventBus;

    @Inject
    public ClusterMembershipListener(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        String uuid = membershipEvent.getMember().getUuid();
        log("ADDED", uuid);
        MemberUpdatedMessage memberUpdated = new MemberUpdatedMessage(MemberUpdatedMessage.State.ADDED, uuid);
        eventBus.post(memberUpdated);
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        String uuid = membershipEvent.getMember().getUuid();
        log("REMOVED", uuid);
        MemberUpdatedMessage memberUpdated = new MemberUpdatedMessage(MemberUpdatedMessage.State.REMOVED, uuid);
        eventBus.post(memberUpdated);
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
    }

    private void log(String action, String uuid) {
        long timestamp = System.currentTimeMillis();

        log.info("\n" +
                        "++++++++++++++++++++++++++++++++++++++++++++\n" +
                        "Member {} {}\n" +
                        "at     {} [{}]\n" +
                        "++++++++++++++++++++++++++++++++++++++++++++\n",
                uuid,
                action,
                TimeUtils.toString(timestamp),
                timestamp);
    }

}
