package com.github.ypiel.chronotask.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void testEnumValues() {
        assertNotNull(Category.valueOf("Feature"));
        assertNotNull(Category.valueOf("Fix"));
        assertNotNull(Category.valueOf("Internal"));
        assertNotNull(Category.valueOf("Meeting"));
        assertNotNull(Category.valueOf("Support"));
        assertNotNull(Category.valueOf("Pause"));
    }

    @Test
    void testEnumCount() {
        assertEquals(6, Category.values().length);
    }
}
