package com.example.demo.util;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginationHelperTest {

    @Test
    void testPaginateEmptyList() {
        Model model = new ConcurrentModel();
        List<String> result = PaginationHelper.paginate(Collections.emptyList(), 1, 10, model);

        assertTrue(result.isEmpty());
        assertEquals(1, model.getAttribute("currentPage"));
        assertEquals(1, model.getAttribute("totalPages"));
        assertEquals(0, model.getAttribute("totalItems"));
        assertEquals(0, model.getAttribute("fromIndex"));
        assertEquals(0, model.getAttribute("toIndex"));
    }

    @Test
    void testPaginateMultiplePages() {
        Model model = new ConcurrentModel();
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        List<Integer> page1 = PaginationHelper.paginate(numbers, 1, 5, model);

        assertEquals(5, page1.size());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), page1);
        assertEquals(1, model.getAttribute("currentPage"));
        assertEquals(3, model.getAttribute("totalPages"));
        assertEquals(12, model.getAttribute("totalItems"));
        assertEquals(1, model.getAttribute("fromIndex"));
        assertEquals(5, model.getAttribute("toIndex"));

        // Page 3 (last page with remaining 2 items)
        List<Integer> page3 = PaginationHelper.paginate(numbers, 3, 5, model);
        assertEquals(2, page3.size());
        assertEquals(Arrays.asList(11, 12), page3);
        assertEquals(3, model.getAttribute("currentPage"));
        assertEquals(11, model.getAttribute("fromIndex"));
        assertEquals(12, model.getAttribute("toIndex"));
    }

    @Test
    void testPageClamping() {
        Model model = new ConcurrentModel();
        List<Integer> numbers = Arrays.asList(1, 2, 3);

        // Page exceeds totalPages -> clamped to max page
        List<Integer> resultOver = PaginationHelper.paginate(numbers, 99, 2, model);
        assertEquals(2, model.getAttribute("currentPage"));
        assertEquals(Collections.singletonList(3), resultOver);

        // Page < 1 -> clamped to page 1
        List<Integer> resultUnder = PaginationHelper.paginate(numbers, -5, 2, model);
        assertEquals(1, model.getAttribute("currentPage"));
        assertEquals(Arrays.asList(1, 2), resultUnder);
    }
}

