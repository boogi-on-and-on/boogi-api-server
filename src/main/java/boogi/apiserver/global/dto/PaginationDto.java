package boogi.apiserver.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PaginationDto {
    protected int nextPage;
    protected boolean hasNext;

    public PaginationDto(Page page) {
        this.nextPage = page.getNumber() + 1;
        this.hasNext = page.hasNext();
    }

    public static PaginationDto of(Page page) {
        return new PaginationDto(page);
    }
}
