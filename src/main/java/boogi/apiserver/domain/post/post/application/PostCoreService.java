package boogi.apiserver.domain.post.post.application;


import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagCoreService;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.CreatePost;
import boogi.apiserver.domain.post.post.dto.PostDetail;
import boogi.apiserver.domain.post.post.dto.UpdatePost;
import boogi.apiserver.domain.post.post.dto.UserPostPage;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.webclient.push.MentionType;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostCoreService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final PostMediaRepository postMediaRepository;
    private final UserRepository userRepository;

    private final MemberValidationService memberValidationService;

    private final CommunityQueryService communityQueryService;
    private final PostMediaQueryService postMediaQueryService;

    private final PostHashtagCoreService postHashtagCoreService;
    private final LikeCoreService likeCoreService;

    private final SendPushNotification sendPushNotification;

    @Transactional
    public Post createPost(CreatePost createPost, Long userId) {
        Long communityId = createPost.getCommunityId();
        Community community = communityQueryService.getCommunity(communityId);
        Member member = memberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(NotJoinedMemberException::new);

        List<PostMedia> findPostMedias = postMediaQueryService
                .getUnmappedPostMediasByUUID(createPost.getPostMediaIds());

        Post savedPost = postRepository.save(
                Post.of(community, member, createPost.getContent()));

        postHashtagCoreService.addTags(savedPost.getId(), createPost.getHashtags());

        findPostMedias.stream()
                .forEach(pm -> pm.mapPost(savedPost));

        List<Long> mentionedUserIds = createPost.getMentionedUserIds();
        if (mentionedUserIds.isEmpty() == false) {
            sendPushNotification.mentionNotification(mentionedUserIds, savedPost.getId(), MentionType.POST);
        }

        return savedPost;
    }

    public PostDetail getPostDetail(Long postId, Long userId) {
        Post findPost = postRepository.getPostWithUserAndMemberAndCommunityByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 글이 존재하지 않거나, 해당 글이 작성된 커뮤니티가 존재하지 않습니다"));

        Community postedCommunity = findPost.getCommunity();
        Member member = memberRepository.findByUserIdAndCommunityId(userId, postedCommunity.getId())
                .orElse(null);

        if (postedCommunity.isPrivate() && member == null) {
            throw new NotJoinedMemberException();
        }

        List<PostMedia> findPostMedias = postMediaRepository.findByPostId(postId);

        Long findLikeId = null;
        if (member != null) {
            Optional<Like> possibleLike = likeRepository.
                    findPostLikeByPostIdAndMemberId(postId, member.getId());
            if (possibleLike.isPresent()) {
                findLikeId = possibleLike.get().getId();
            }
        }

        Long postUserId = findPost.getMember().getUser().getId();
        Boolean me = postUserId.equals(userId) ? Boolean.TRUE : Boolean.FALSE;

        return new PostDetail(findPost, findPostMedias, me, findLikeId);
    }

    @Transactional
    public Post updatePost(UpdatePost updatePost, Long postId, Long userId) {
        Post findPost = postRepository.findPostById(postId).orElseThrow(() -> {
            throw new EntityNotFoundException("해당 글이 존재하지 않습니다");
        });

        communityRepository.findCommunityById(findPost.getCommunity().getId())
                .orElseThrow(() -> {
                    throw new EntityNotFoundException("해당 커뮤니티가 존재하지 않습니다");
                });

        Long postedUserId = findPost.getMember().getUser().getId();
        if (postedUserId.equals(userId) == false) {
            throw new NotAuthorizedMemberException("해당 글의 수정 권한이 없습니다");
        }

        postHashtagCoreService.removeTagsByPostId(postId);
        List<PostHashtag> newPostHashtags = postHashtagCoreService.addTags(postId, updatePost.getHashtags());

        findPost.updatePost(updatePost.getContent(), newPostHashtags);

        List<PostMedia> findPostMedias = postMediaRepository.findByPostId(postId);
        List<String> newPostMediaIds = updatePost.getPostMediaIds();

        List<PostMedia> diffPostMedias = new ArrayList<>();

        for (PostMedia postMedia : findPostMedias) {
            String uid = postMedia.getUuid();
            if (newPostMediaIds.contains(uid)) {
                newPostMediaIds.remove(uid);
            } else {
                diffPostMedias.add(postMedia);
            }
        }
        postMediaRepository.deleteAll(diffPostMedias);

        List<PostMedia> newPostMedias = postMediaQueryService.getUnmappedPostMediasByUUID(newPostMediaIds);
        newPostMedias.stream()
                .forEach(pm -> pm.mapPost(findPost));

        return findPost;
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post findPost = postRepository.getPostWithCommunityAndMemberByPostId(postId)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException("해당 글이 존재하지 않습니다");
                });

        Long findPostId = findPost.getId();
        Long postedCommunityId = findPost.getCommunity().getId();
        Long postedUserId = findPost.getMember().getUser().getId();

        if (postedUserId.equals(userId) == false &&
                memberValidationService.hasAuthWithoutThrow(userId, postedCommunityId, MemberType.SUB_MANAGER) == false) {
            throw new NotAuthorizedMemberException();
        }

        postHashtagCoreService.removeTagsByPostId(findPostId);

        List<Comment> findComments = commentRepository.findAllByPostId(postId);
        findComments.stream().forEach(c -> c.deleteComment());

        likeCoreService.removeAllPostLikes(findPostId);

        List<PostMedia> postMedias = postMediaRepository.findByPostId(postId);
        postMediaRepository.deleteAllInBatch(postMedias);

        postRepository.delete(findPost);
    }

    public UserPostPage getUserPosts(Long userId, Long sessionUserId, Pageable pageable) {
        List<Long> findMemberIds;

        if (userId.equals(sessionUserId)) {
            findMemberIds = memberRepository
                    .findMemberIdsForQueryUserPostBySessionUserId(sessionUserId);
        } else {
            userRepository.findUserById(userId).orElseThrow(() -> {
                throw new EntityNotFoundException("해당 유저가 존재하지 않습니다.");
            });

            findMemberIds = memberRepository
                    .findMemberIdsForQueryUserPostByUserIdAndSessionUserId(userId, sessionUserId);
        }

        Page<Post> userPostPage = postRepository.getUserPostPageByMemberIds(findMemberIds, pageable);

        return UserPostPage.of(userPostPage);
    }
}
