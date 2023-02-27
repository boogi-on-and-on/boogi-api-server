package boogi.apiserver.domain.hashtag.post.domain;

import boogi.apiserver.domain.post.post.domain.Post;
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
public class PostHashtags {

    private static final int MAX_LENGTH = 5;
    private static final String MESSAGE = "게시글 해시태그는 최대 " + MAX_LENGTH + "개 까지만 추가 가능합니다.";

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> values = new ArrayList<>();

    public PostHashtags(final List<PostHashtag> values) {
        this.values = values;
    }

    public void addTags(List<String> tags, Post post) {
        tags = resolveNullPointException(tags);
        validate(tags);
        this.values.addAll(PostHashtag.listOf(tags, post));
    }

    public void updateTags(List<String> tags, Post post) {
        tags = resolveNullPointException(tags);
        validate(tags);
        removePreviousExclusiveHashTags(tags);
        List<String> newTags = OneToManyUpdateOptimizer.inputsToBeInserted(this.values, PostHashtag::getTag, tags);
        this.values.addAll(PostHashtag.listOf(newTags, post));
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
        List<PostHashtag> deleteHashtags =
                OneToManyUpdateOptimizer.entityToBeDeleted(this.values, PostHashtag::getTag, tags);
        this.values.removeAll(deleteHashtags);
    }

    public List<PostHashtag> getValues() {
        return Collections.unmodifiableList(this.values);
    }
}
