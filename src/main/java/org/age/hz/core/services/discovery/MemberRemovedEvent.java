package org.age.hz.core.services.discovery;

import com.google.common.base.MoreObjects;
import com.hazelcast.core.EntryEvent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class MemberRemovedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    public MemberRemovedEvent(EntryEvent<String, NodeId> event) {
        id = event.getKey();
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MemberRemovedEvent that = (MemberRemovedEvent) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}
