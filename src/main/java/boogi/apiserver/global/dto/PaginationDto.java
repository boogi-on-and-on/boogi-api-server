package boogi.apiserver.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PaginationDto {
    protected int nextPage;
    protected boolean hasNext;

    public PaginationDto(Slice page) {
        this.nextPage = page.getNumber() + 1;
        this.hasNext = page.hasNext();
    }

    public static PaginationDto of(Slice page) {
        return new PaginationDto(page);
    }
}
