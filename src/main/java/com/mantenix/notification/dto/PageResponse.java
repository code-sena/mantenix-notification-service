package com.mantenix.notification.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Paginated list: {data, meta} (PaginatedMeta in 07-api/contracts/openapi/_shared.yaml). */
public record PageResponse<T>(List<T> data, Meta meta) {

    /** page starts at 1, as in the contract. */
    public record Meta(int page, int limit, long total, int totalPages) {
    }

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                new Meta(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }
}
