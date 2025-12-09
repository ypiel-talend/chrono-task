package com.github.ypiel.chronotask;

import com.github.ypiel.chronotask.model.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ChronoTaskTest {

    @Test
    void testFormatDuration() {
        assertEquals("00:00:00", ChronoTask.formatDuration(Duration.ZERO));
        assertEquals("01:00:00", ChronoTask.formatDuration(Duration.ofHours(1)));
        assertEquals("00:01:00", ChronoTask.formatDuration(Duration.ofMinutes(1)));
        assertEquals("00:00:01", ChronoTask.formatDuration(Duration.ofSeconds(1)));
        assertEquals("01:01:01", ChronoTask.formatDuration(Duration.ofHours(1).plusMinutes(1).plusSeconds(1)));
        assertEquals("25:00:00", ChronoTask.formatDuration(Duration.ofHours(25)));
    }

    @Test
    void testConstants() {
        assertNotNull(ChronoTask.DONE_STATUS);
        assertTrue(ChronoTask.mainTopics.length > 0);
    }
}
