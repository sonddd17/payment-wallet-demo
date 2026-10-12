package com.sonddd.paywallet.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Vỏ bọc phân trang gọn gàng để trả cho client thay vì cả đối tượng Page của Spring. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    /** Chuyển một Page<E> của Spring Data sang PageResponse<T> qua hàm mapper. */
    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
