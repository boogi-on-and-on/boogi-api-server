package boogi.apiserver.domain.hashtag.community.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
@NoArgsConstructor
public class CommunityHashtags {

    private static final int MAX_LENGTH = 5;

    private static final String MESSAGE = "커뮤니티 해시태그는 최대 " + MAX_LENGTH + "개 까지만 추가 가능합니다.";

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityHashtag> values = new ArrayList<>();

    public CommunityHashtags(final List<CommunityHashtag> values) {
        this.values = values;
    }

    public void addTags(List<String> tags, Community community) {
        tags = resolveNullPointException(tags);
        validate(tags);
        this.values.addAll(CommunityHashtag.listOf(tags, community));
    }

    public void updateTags(List<String> tags, Community community) {
        tags = resolveNullPointException(tags);
        validate(tags);
        this.values.removeAll(exclusiveHashtags(tags));
        tags.removeAll(inclusiveTags(tags));
        this.values.addAll(CommunityHashtag.listOf(tags, community));
    }

    private List<String> resolveNullPointException(List<String> tags) {
        return (tags == null) ? new ArrayList<>() : tags;
    }

    private void validate(List<String> tags) {
        if (tags.size() > MAX_LENGTH) {
            throw new IllegalArgumentException(MESSAGE);
        }
    }

    private List<CommunityHashtag> exclusiveHashtags(List<String> tags) {
        return this.values.stream()
                .filter(tag -> !tags.contains(tag))
                .collect(Collectors.toList());
    }

    private List<String> inclusiveTags(List<String> tags) {
        return this.values.stream()
                .filter(tag -> tags.contains(tag))
                .map(CommunityHashtag::getTag)
                .collect(Collectors.toList());
    }

    public List<CommunityHashtag> getValues() {
        return Collections.unmodifiableList(this.values);
    }
}
