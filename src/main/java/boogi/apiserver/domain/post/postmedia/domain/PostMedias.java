package boogi.apiserver.domain.post.postmedia.domain;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.util.OneToManyUpdateOptimizer;
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
public class PostMedias {

    @OneToMany(mappedBy = "post", orphanRemoval = true, cascade = CascadeType.REMOVE)
    List<PostMedia> values = new ArrayList<>();

    public List<PostMedia> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public PostMedias(final List<PostMedia> values) {
        this.values = values;
    }

    public void updatePostMedias(List<PostMedia> updatePostMedias, Post post) {
        this.removePreviousPostMedias(updatePostMedias);
        List<PostMedia> postMediaForInsert
            = OneToManyUpdateOptimizer.inputsToBeInserted(updatePostMedias, this.values);
        this.addPostMedias(postMediaForInsert, post);
    }

    public void addPostMedias(List<PostMedia> newPostMedias, Post post) {
        newPostMedias.forEach(postMedia -> postMedia.mapPost(post));
        this.values.addAll(newPostMedias);
    }

    private void removePreviousPostMedias(List<PostMedia> updatePostMedias) {
        List<String> updatePostMediaUUIDs = convertToUUIDs(updatePostMedias);
        final List<PostMedia> postMediasForRemove =
                OneToManyUpdateOptimizer.entityToBeDeleted(this.values, PostMedia::getUuid, updatePostMediaUUIDs);
        this.values.removeAll(postMediasForRemove);
    }

    private List<String> convertToUUIDs(final List<PostMedia> postMedias) {
        return postMedias.stream()
                .map(PostMedia::getUuid)
                .collect(Collectors.toList());
    }
}
