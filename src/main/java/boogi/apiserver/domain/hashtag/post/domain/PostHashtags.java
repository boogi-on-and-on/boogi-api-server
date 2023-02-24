package boogi.apiserver.domain.hashtag.post.domain;

import boogi.apiserver.domain.post.post.domain.Post;
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
        this.values.removeAll(exclusiveHashtags(tags));
        tags.removeAll(inclusiveTags(tags));
        this.values.addAll(PostHashtag.listOf(tags, post));
    }

    private void validate(List<String> tags) {
        if (tags.size() > MAX_LENGTH) {
            throw new IllegalArgumentException(MESSAGE);
        }
    }

    private List<String> resolveNullPointException(List<String> tags) {
        return (tags == null) ? new ArrayList<>() : tags;
    }

    private List<PostHashtag> exclusiveHashtags(List<String> tags) {
        return this.values.stream()
                .filter(tag -> !tags.contains(tag))
                .collect(Collectors.toList());
    }

    private List<String> inclusiveTags(List<String> tags) {
        return this.values.stream()
                .filter(tag -> tags.contains(tag))
                .map(PostHashtag::getTag)
                .collect(Collectors.toList());
    }

    public List<PostHashtag> getValues() {
        return Collections.unmodifiableList(this.values);
    }
}
