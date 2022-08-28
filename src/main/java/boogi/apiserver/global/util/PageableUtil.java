package boogi.apiserver.global.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

public abstract class PageableUtil {

    public static <T> Slice getSlice(List<T> contents, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        boolean hasNext = false;

        if (contents.size() > pageSize) {
            contents.remove(pageSize);
            hasNext = true;
        }

        return new SliceImpl(contents, pageable, hasNext);
    }
}
