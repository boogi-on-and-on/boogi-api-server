package boogi.apiserver.domain.community.community.domain;


import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotManagerException;
import boogi.apiserver.domain.member.vo.NullMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommunityTest {

    @Test
    @DisplayName("태그 추가하기")
    void addTags() {
        final Community community = TestCommunity.builder().build();

        community.addTags(List.of("태그1", "태그2"));

        assertThat(community.getHashtags()).extracting("tag").containsExactly("태그1", "태그2");
    }

    @Test
    @DisplayName("커뮤니티 업데이트하기")
    void updateCommunity() {
        final Community community = TestCommunity.builder().build();

        community.updateCommunity("커뮤니티 소개란 입니다.", List.of("태그1", "태그2"));

        assertThat(community.getHashtags()).extracting("tag").containsExactly("태그1", "태그2");
        assertThat(community.getDescription()).isEqualTo("커뮤니티 소개란 입니다.");
    }

    @DisplayName("비공개 커뮤니티로 전환하기")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ConvertPrivateTest {
        Stream<Arguments> memberTypeEnum() {
            return Stream.of(
                    Arguments.of(MemberType.NORMAL),
                    Arguments.of(MemberType.SUB_MANAGER)
            );
        }

        @DisplayName("매니저 권한이 없어서 실패")
        @ParameterizedTest(name = "{0}로 시도할 경우 권한 없음")
        @MethodSource("memberTypeEnum")
        void throwNotManagerException(MemberType memberType) {
            final Community community = TestCommunity.builder().build();

            assertThatThrownBy(() -> {
                community.switchPrivate(true, memberType);
            }).isInstanceOf(NotManagerException.class);
        }

        @Test
        @DisplayName("성공")
        void success() {
            final Community community = TestCommunity.builder()
                    .isPrivate(false)
                    .build();

            community.switchPrivate(true, MemberType.MANAGER);

            assertThat(community.isPrivate()).isTrue();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("가입요청 자동승인 변환 테스트")
    class AutoApprovalTest {
        Stream<Arguments> memberTypeEnum() {
            return Stream.of(
                    Arguments.of(MemberType.NORMAL),
                    Arguments.of(MemberType.SUB_MANAGER)
            );
        }

        @DisplayName("매니저 권한이 없어서 실패")
        @ParameterizedTest(name = "{0}로 시도할 경우 권한이 없음")
        @MethodSource("memberTypeEnum")
        void throwNotManagerException(MemberType memberType) {
            final Community community = TestCommunity.builder().build();

            assertThatThrownBy(() -> {
                community.switchAutoApproval(true, memberType);
            }).isInstanceOf(NotManagerException.class);
        }

        @DisplayName("자동승인 변환 성공")
        @Test
        void success() {
            final Community community = TestCommunity.builder()
                    .autoApproval(false)
                    .build();

            community.switchAutoApproval(true, MemberType.MANAGER);

            assertThat(community.isAutoApproval()).isTrue();
        }
    }

    @Nested
    @DisplayName("canViewMember 테스트")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CanViewMemberTest {
        Stream<Arguments> canView() {
            final Community c1 = TestCommunity.builder()
                    .isPrivate(false)
                    .build();

            final Community c2 = TestCommunity.builder()
                    .isPrivate(true)
                    .build();

            return Stream.of(
                    Arguments.of(c1, new NullMember()),
                    Arguments.of(c2, TestMember.builder().build())
            );
        }

        @ParameterizedTest
        @DisplayName("공개커뮤니티 || 가입한 멤버 --> true 리턴")
        @MethodSource("canView")
        void returnTrue(Community community, Member member) {
            assertThat(community.canViewMember(member)).isTrue();
        }

        @Test
        @DisplayName("비공개커뮤니티 && 미가입 --> false 리턴")
        void returnFalse() {
            final Community c1 = TestCommunity.builder()
                    .isPrivate(true)
                    .build();

            final NullMember member = new NullMember();

            assertThat(c1.canViewMember(member)).isFalse();
        }
    }
}