package org.jujubeframework.jdbc.support.pagination;

import org.jujubeframework.jdbc.support.entity.BaseEntity;

/**
 * 分页请求
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class PageableRequest implements BaseEntity {
    private static final long serialVersionUID = -590137694303783744L;
    private int index;
    private int size;
    private int start;

    /**
     * 总条数。放置这个元素的目的是，如果在第一页查询出了totalElements，那么后面的页数中，就可以直接使用totalElements。
     * 而不用再次查询总条数
     */
    private long totalElements;

    public PageableRequest(int index, int size) {
        super();
        setIndex(index);
        setSize(size);
    }

    public PageableRequest() {
        super();
        this.index = 1;
        this.size = Pageable.DEFAULT_SIZE;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        if (index <= 0) {
            index = 1;
        }
        this.index = index;
    }

    public int getSize() {
        return size < 1 ? Pageable.DEFAULT_SIZE : size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getStart() {
        return start;
    }

    public <T> Pageable<T> newPageable() {
        Pageable<T> ts = new Pageable<>(index, size, start);
        ts.setTotalElements(this.getTotalElements());
        return ts;
    }

    /**
     * 构建分页请求
     *
     * @author John Li Email：jujubeframework@163.com
     */
    public static PageableRequest buildPageRequest(PageableRequest pageableRequest) {
        if (pageableRequest == null) {
            pageableRequest = new PageableRequest();
        }
        if (pageableRequest.getIndex() < 1) {
            pageableRequest.setIndex(1);
        }
        if (pageableRequest.getSize() < 1) {
            pageableRequest.setSize(Pageable.DEFAULT_SIZE);
        }
        return pageableRequest;
    }

    public static PageableRequest buildPageRequest() {
        return buildPageRequest(null);
    }
}
