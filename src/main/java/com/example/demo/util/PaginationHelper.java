package com.example.demo.util;

import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

/**
 * Tiện ích phân trang danh sách đồng bộ cho toàn bộ hệ thống
 */
public final class PaginationHelper {

    private PaginationHelper() {}

    public static <T> List<T> paginate(List<T> sourceList, int page, int pageSize, Model model) {
        if (sourceList == null) {
            sourceList = Collections.emptyList();
        }
        int totalItems = sourceList.size();
        int validPageSize = (pageSize > 0) ? pageSize : 10;
        int totalPages = (int) Math.ceil((double) totalItems / validPageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }

        int validPage = Math.max(1, Math.min(page, totalPages));

        int fromIndex = (validPage - 1) * validPageSize;
        int toIndex = Math.min(fromIndex + validPageSize, totalItems);

        List<T> pagedList = (fromIndex < totalItems && fromIndex >= 0)
                ? sourceList.subList(fromIndex, toIndex)
                : Collections.emptyList();

        if (model != null) {
            model.addAttribute("currentPage", validPage);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("pageSize", validPageSize);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("fromIndex", totalItems > 0 ? fromIndex + 1 : 0);
            model.addAttribute("toIndex", toIndex);
        }

        return pagedList;
    }
}

