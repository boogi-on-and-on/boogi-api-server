package boogi.apiserver.global.dto;

import lombok.*;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PagnationDto {
    protected int nextPage;
    protected int totalCount;
    protected boolean hasNext;

    public PagnationDto(Page page) {
        this.nextPage = page.getNumber() + 1;
        this.totalCount = (int) page.getTotalElements();
        this.hasNext = page.hasNext();
    }

    public static PagnationDto of(Page page) {
        return new PagnationDto(page);
    }
}
