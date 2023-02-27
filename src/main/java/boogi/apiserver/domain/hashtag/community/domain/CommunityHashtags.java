package boogi.apiserver.domain.hashtag.community.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.global.util.OneToManyUpdateOptimizer;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        removePreviousExclusiveHashTags(tags);
        List<String> newTags = OneToManyUpdateOptimizer.inputsToBeInserted(this.values, CommunityHashtag::getTag, tags);
        this.values.addAll(CommunityHashtag.listOf(newTags, community));
    }

    private List<String> resolveNullPointException(List<String> tags) {
        return (tags == null) ? new ArrayList<>() : tags;
    }

    private void validate(List<String> tags) {
        if (tags.size() > MAX_LENGTH) {
            throw new IllegalArgumentException(MESSAGE);
        }
    }

    private void removePreviousExclusiveHashTags(List<String> tags) {
        List<CommunityHashtag> deleteHashtags =
                OneToManyUpdateOptimizer.entityToBeDeleted(this.values, CommunityHashtag::getTag, tags);
        this.values.removeAll(deleteHashtags);
    }

    public List<CommunityHashtag> getValues() {
        return Collections.unmodifiableList(this.values);
    }
}
