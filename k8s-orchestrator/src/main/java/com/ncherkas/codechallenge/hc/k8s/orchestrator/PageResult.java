package com.ncherkas.codechallenge.hc.k8s.orchestrator;

import java.util.List;

/**
 * POJO for the paginated listing results.
 * @param <T> type of the results item
 */
public class PageResult<T> {

    private final int page;
    private final int count;
    private final long total;
    private final List<T> items;

    private PageResult(int page, int count, long total, List<T> items) {
        this.page = page;
        this.count = count;
        this.total = total;
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public int getCount() {
        return count;
    }

    public long getTotal() {
        return total;
    }

    public List<T> getItems() {
        return items;
    }

    public static <T> PageResult<T> of(int page, int count, long total, List<T> results) {
        return new PageResult<>(page, count, total, results);
    }
}
