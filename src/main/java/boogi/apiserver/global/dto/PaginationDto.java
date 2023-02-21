package boogi.apiserver.global.dto;

import lombok.*;
import org.springframework.data.domain.Slice;

@Getter
@NoArgsConstructor
public class PaginationDto {
    protected int nextPage;
    protected boolean hasNext;

    public PaginationDto(int nextPage, boolean hasNext) {
        this.nextPage = nextPage;
        this.hasNext = hasNext;
    }

    public static PaginationDto of(Slice page) {
        return new PaginationDto(page.getNumber() + 1, page.hasNext());
    }
}
