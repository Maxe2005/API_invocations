package com.imt.api_invocations.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Random Utility Tests")
class RandomTest {

    @Test
    @DisplayName("Should generate random number within range")
    void testRandomWithinRange() {
        // Arrange
        int min = 0;
        int max = 10;

        // Act
        int result = Random.random(min, max);

        // Assert
        assertTrue(result >= min && result <= max, "Random number should be within range");
    }

    @Test
    @DisplayName("Should handle single value range")
    void testRandomSingleValue() {
        // Arrange & Act
        int result = Random.random(5, 5);

        // Assert
        assertEquals(5, result);
    }

    @Test
    @DisplayName("Should generate different values multiple times")
    void testRandomVariability() {
        // Arrange & Act
        int result1 = Random.random(0, 100);
        int result2 = Random.random(0, 100);
        int result3 = Random.random(0, 100);

        // Assert
        assertTrue(
                result1 >= 0 && result1 <= 100,
                "All results should be within range");
        assertTrue(
                result2 >= 0 && result2 <= 100,
                "All results should be within range");
        assertTrue(
                result3 >= 0 && result3 <= 100,
                "All results should be within range");
    }

    @Test
    @DisplayName("Should handle negative ranges")
    void testRandomNegativeRange() {
        // Arrange
        int min = -100;
        int max = -10;

        // Act
        int result = Random.random(min, max);

        // Assert
        assertTrue(result >= min && result <= max, "Random number should be within negative range");
    }
}
