package boogi.apiserver.domain.hashtag.post.domain;

import boogi.apiserver.domain.post.post.domain.Post;
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
public class PostHashtags {

    private static final int MAX_LENGTH = 5;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> values = new ArrayList<>();

    public PostHashtags(final List<PostHashtag> values) {
        this.values = values;
    }

    public void addTags(List<String> tags, Post post) {
    }


    public List<PostHashtag> getValues() {
        return Collections.unmodifiableList(this.values);
    }

}
