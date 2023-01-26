package boogi.apiserver.domain.post.postmedia.vo;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
public class PostMedias {

    private final List<PostMedia> postMedias;

    public PostMedias(List<PostMedia> postMedias) {
        if (postMedias == null) {
            throw new IllegalArgumentException("postMedias는 null일 수 없습니다.");
        }
        this.postMedias = postMedias;
    }

    public List<PostMedia> excludedPostMedia(List<String> postMediaIds) {
        return postMedias.stream()
                .filter(pm -> !postMediaIds.contains(pm.getUuid()))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<String> newPostMediaIds(List<String> postMediaIds) {
        List<String> newPostMediaIds = new ArrayList<>(postMediaIds);
        newPostMediaIds.removeAll(convertToUUIDList());
        return newPostMediaIds;
    }

    private List<String> convertToUUIDList() {
        return postMedias.stream()
                .map(PostMedia::getUuid)
                .collect(Collectors.toUnmodifiableList());
    }
}
