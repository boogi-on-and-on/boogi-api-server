package boogi.apiserver.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class OneToManyUpdateOptimizerTest {

    static Stream<Arguments> entityToBeDeleted() {
        return Stream.of(
                Arguments.of(List.of("a", "b", "c"), List.of("a", "c", "d"), List.of("b")),
                Arguments.of(List.of("a", "b", "c"), List.of("a"), List.of("b", "c")),
                Arguments.of(List.of("a", "b"), List.of("a", "b", "c"), List.of()),
                Arguments.of(List.of("a", "b", "c"), List.of("a", "b", "c"), List.of()),
                Arguments.of(List.of("a", "b", "c"), List.of("d", "e", "f"), List.of("a", "b", "c"))
        );
    }

    @DisplayName("저장된 엔티티 중 요청한 값과 겹치지 않는 부분을 반환한다.")
    @ParameterizedTest(name = "엔티티의 값 {0} 중 요청한 값이 {1}일때, {2}를 반환한다.")
    @MethodSource("entityToBeDeleted")
    void testEntityToBeDeleted(List<String> entityValue, List<String> inputValue, List<String> expected) {
        List<TestEntity> testEntities = TestEntity.listOf(entityValue);
        List<TestEntity> resultEntities =
                OneToManyUpdateOptimizer.entityToBeDeleted(testEntities, TestEntity::getValue, inputValue);

        assertThat(resultEntities).extracting(TestEntity::getValue).isEqualTo(expected);
    }

    static Stream<Arguments> inputsToBeDeleted() {
        return Stream.of(
                Arguments.of(List.of("a", "b", "c"), List.of("a", "c", "d"), List.of("d")),
                Arguments.of(List.of("a", "b", "c"), List.of("a"), List.of()),
                Arguments.of(List.of("a", "b"), List.of("a", "b", "c"), List.of("c")),
                Arguments.of(List.of("a", "b", "c"), List.of("a", "b", "c"), List.of()),
                Arguments.of(List.of("a", "b", "c"), List.of("d", "e", "f"), List.of("d", "e", "f"))
        );
    }

    @DisplayName("요청된 값 중 저장된 엔티티의 값과 겹치지 않는 부분을 반환한다.")
    @ParameterizedTest(name = "요청된 값 {0} 중 저장된 엔티티의 값이 {1}일때, {2}를 반환한다.")
    @MethodSource("inputsToBeDeleted")
    void testInputsToBeInserted(List<String> entityValue, List<String> inputValue, List<String> expected) {
        List<TestEntity> testEntities = TestEntity.listOf(entityValue);
        List<String> resultInputs =
                OneToManyUpdateOptimizer.inputsToBeInserted(testEntities, TestEntity::getValue, inputValue);

        assertThat(resultInputs).isEqualTo(expected);
    }

    static class TestEntity {
        private String value;

        public TestEntity(String value) {
            this.value = value;
        }

        public static List<TestEntity> listOf(List<String> values) {
            return values.stream()
                    .map(TestEntity::new)
                    .collect(Collectors.toList());
        }

        public String getValue() {
            return value;
        }
    }
}