package com.refinedmods.refinedstorage2.api.core.registry;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class OrderedRegistryImplTest {
    OrderedRegistry<String, Integer> sut;

    @BeforeEach
    void setUp() {
        sut = new OrderedRegistryImpl<>("A", 10);
    }

    @Test
    void Test_default() {
        // Assert
        assertThat(sut.getDefault()).isEqualTo(10);
        assertThat(sut.getAll()).containsExactly(10);
        assertThat(sut.get("A")).get().isEqualTo(10);
        assertThat(sut.get("B")).isEmpty();
        assertThat(sut.getId(10)).get().isEqualTo("A");
        assertThat(sut.getId(20)).isEmpty();
        assertThat(sut.next(10)).isEqualTo(10);
        assertThat(sut.next(20)).isEqualTo(10);
        assertThat(sut.isEmpty()).isTrue();
    }

    @Test
    void Test_trying_to_modify_list() {
        // Arrange
        List<Integer> list = sut.getAll();

        // Act & assert
        assertThrows(UnsupportedOperationException.class, () -> list.add(1));
    }

    @Test
    void Test_registration() {
        // Act
        sut.register("B", 20);

        // Assert
        assertThat(sut.getDefault()).isEqualTo(10);
        assertThat(sut.getAll()).containsExactly(10, 20);
        assertThat(sut.get("A")).get().isEqualTo(10);
        assertThat(sut.get("B")).get().isEqualTo(20);
        assertThat(sut.getId(10)).get().isEqualTo("A");
        assertThat(sut.getId(20)).get().isEqualTo("B");
        assertThat(sut.next(10)).isEqualTo(20);
        assertThat(sut.next(20)).isEqualTo(10);
        assertThat(sut.isEmpty()).isFalse();
    }

    @Test
    void Test_duplicate_id_registration() {
        // Arrange
        sut.register("B", 20);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.register("B", 20));
        assertThat(sut.getAll()).containsExactly(10, 20);
    }

    @Test
    void Test_duplicate_value_registration() {
        // Arrange
        sut.register("B", 20);

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.register("C", 20));
        assertThat(sut.getAll()).containsExactly(10, 20);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void Test_invalid_registration() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> sut.register(null, 20));
        assertThrows(NullPointerException.class, () -> sut.register("B", null));
        assertThrows(NullPointerException.class, () -> sut.register(null, null));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void Test_invalid_default() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> new OrderedRegistryImpl<>(null, 20));
        assertThrows(NullPointerException.class, () -> new OrderedRegistryImpl<>("B", null));
        assertThrows(NullPointerException.class, () -> new OrderedRegistryImpl<>(null, null));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void Test_invalid_next_value() {
        assertThrows(NullPointerException.class, () -> sut.next(null));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void Test_invalid_getting_id_and_value() {
        assertThrows(NullPointerException.class, () -> sut.get(null));
        assertThrows(NullPointerException.class, () -> sut.getId(null));
    }
}