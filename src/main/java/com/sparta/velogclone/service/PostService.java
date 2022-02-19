package com.sparta.velogclone.service;

import com.sparta.velogclone.domain.*;
import com.sparta.velogclone.dto.requestdto.PostRequestDto;
import com.sparta.velogclone.dto.responsedto.CommentResponseDto;
import com.sparta.velogclone.dto.responsedto.PostDetailResponseDto;
import com.sparta.velogclone.dto.responsedto.PostResponseDto;
import com.sparta.velogclone.repository.CommentRepository;
import com.sparta.velogclone.repository.LikesRepository;
import com.sparta.velogclone.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikesRepository likesRepository;
    private final ImageFileService imageFileService;

    // 게시글 작성
    @Transactional
    public void savePost(MultipartFile multipartFile, PostRequestDto postRequestDto, User user) throws IOException {
        Post post = new Post(postRequestDto, user);
        postRepository.save(post);

        ImageFile imageFile = imageFileService.saveFile(multipartFile);

        postRequestDto.setImageFile(imageFile);
        imageFile.addPost(post);
    }

    // 게시글 전체 조회
    public List<PostResponseDto> viewPost() {
        List<PostResponseDto> postList = new ArrayList<>();
        List<Post> posts = postRepository.findAllByOrderByModifiedAtDesc();

        for (Post post : posts) {
            List<Comment> comments = commentRepository.findAllByPostIdOrderByModifiedAtDesc(post.getId());
            int commentCnt = comments.size();

            List<Likes> likes = likesRepository.findAllByPostId(post.getId());
            int likeCnt = likes.size();

            String postModifiedAt = post.getModifiedAt().toString();
            String year = postModifiedAt.substring(0,4) + "년";
            String month = postModifiedAt.substring(5,7) + "월";
            String day = postModifiedAt.substring(8) + "일";
            postModifiedAt = year + month + day;

            PostResponseDto postResponseDto = new PostResponseDto(post, commentCnt, likeCnt, postModifiedAt);
            postList.add(postResponseDto);
        }
        return postList;
    }

    // 게시글 상세 조회
    public PostDetailResponseDto viewPostDetail(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(IllegalAccessError::new);
        List<CommentResponseDto> commentList = commentRepository.findAllByPostIdOrderByModifiedAtDesc(postId).stream()
                .map(comment -> comment.toResponseDto()).collect(Collectors.toList());

        List<Comment> comments = commentRepository.findAllByPostIdOrderByModifiedAtDesc(post.getId());
        for (CommentResponseDto commentResponseDto : commentList) {
            String commentModifiedAt = commentResponseDto.getCommentModifiedAt();
            String year = commentModifiedAt.substring(0,4) + "년";
            String month = commentModifiedAt.substring(5,7) + "월";
            String day = commentModifiedAt.substring(8) + "일";
            String time = commentModifiedAt.substring(11,19);
            commentModifiedAt = year + month + day + time;
            commentResponseDto.setCommentModifiedAt(commentModifiedAt);
        }
        int commentCnt = comments.size();

        List<Likes> likes = likesRepository.findAllByPostId(post.getId());
        int likeCnt = likes.size();

        String postModifiedAt = post.getModifiedAt().toString();
        String year = postModifiedAt.substring(0,4) + "년";
        String month = postModifiedAt.substring(5,7) + "월";
        String day = postModifiedAt.substring(8) + "일";
        String time = postModifiedAt.substring(11,19);
        postModifiedAt = year + month + day + time;

        return new PostDetailResponseDto(
                post, commentCnt, likeCnt, postModifiedAt, commentList);
    }
}
