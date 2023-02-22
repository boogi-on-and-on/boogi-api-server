package boogi.apiserver.domain.post.postmedia.domain;

import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Embeddable
@NoArgsConstructor
public class PostMedias {

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    List<PostMedia> values = new ArrayList<>();

    public List<PostMedia> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public PostMedias(final List<PostMedia> values) {
        this.values = values;
    }
}
