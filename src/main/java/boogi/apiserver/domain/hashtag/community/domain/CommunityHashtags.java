package boogi.apiserver.domain.hashtag.community.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityHashtags {

    private static final int MAX_LENGTH = 5;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityHashtag> values = new ArrayList<>();

    public CommunityHashtags(final List<CommunityHashtag> values) {
        this.values = values;
    }

    public void addTags(List<String> tags, Community community) {
    }

    public List<CommunityHashtag> getValues() {
        return Collections.unmodifiableList(this.values);
    }
}
