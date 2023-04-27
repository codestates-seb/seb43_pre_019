package backend.com.backend.member.controller;

import backend.com.backend.exception.BusinessLogicException;
import backend.com.backend.exception.ExceptionCode;
import backend.com.backend.member.dto.MemberInfoDto;
import backend.com.backend.member.dto.MemberPatchDto;
import backend.com.backend.member.dto.MemberPostDto;
import backend.com.backend.member.entity.Member;
import backend.com.backend.member.mapper.MemberMapper;
import backend.com.backend.member.repository.MemberRepository;
import backend.com.backend.member.service.MemberService;
import backend.com.backend.utils.UriCreator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@Validated
@CrossOrigin(origins = "*")
public class MemberController {
    private final static String MEMBER_DEFAULT_URL = "/api/members";
    private final MemberMapper memberMapper;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    public MemberController(MemberMapper memberMapper, MemberService memberService, MemberRepository memberRepository) {
        this.memberMapper = memberMapper;
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/sign")
    public ResponseEntity postMember(@Valid @RequestBody MemberPostDto memberPostDto){// 회원가입
        Member member = memberMapper.MemberPostDtoToMember(memberPostDto);
        Member data= memberService.createMember(member);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, member.getId());//데이터베이스에 저장된 리소스의 위치를 알려주는 위치 정보
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{member-id}")
    public ResponseEntity patchMember(@PathVariable("member-id") @Positive Long MemberId,
                                      @Valid @RequestBody MemberPatchDto memberPatchDto,
                                      Authentication authentication){
        memberPatchDto.setId(MemberId);
        Member member = memberMapper.MemberPatchDtoToMember(memberPatchDto);
        Member data = memberService.updateMember(member, authentication);

        return new ResponseEntity<>(memberMapper.MemberToResponseDto(data),HttpStatus.OK);
    }
    @GetMapping("/{member-id}")
    public ResponseEntity getMember(@PathVariable("member-id") @Positive Long memberId){
        Member data = memberService.findMember(memberId);

        return new ResponseEntity<>(memberMapper.MemberToResponseDto(data),HttpStatus.OK);
    }
//    @GetMapping
//    public ResponseEntity getUsers(@Positive @RequestParam int page,
//                                   @Positive @RequestParam int size){
//        Page<User> pageusers = userService.findUsers(page -1, size);
//        List<User> users = pageusers.getContent();
//        return null;
//    }

    @GetMapping("/info")
    public ResponseEntity getMemberInfo(Authentication authentication) {
        String emailOfDemander = authentication.getName();
        Optional<Member> optionalMember = memberRepository.findByEmail(emailOfDemander);
        Member findMember = optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        long id = findMember.getId();
        String displayName = findMember.getDisplayName();
        return new ResponseEntity(new MemberInfoDto(id, displayName), HttpStatus.OK);
    }
    @DeleteMapping("/{member-id}")
    public ResponseEntity deleteMember(@PathVariable("member-id") @Positive Long memberID,
                                       Authentication authentication){
        Member data = memberService.deleteMember(memberID, authentication);

        return new ResponseEntity<>(memberMapper.MemberToResponseDto(data),HttpStatus.NO_CONTENT);
    }
}
