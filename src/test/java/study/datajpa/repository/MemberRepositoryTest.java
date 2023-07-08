package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {
    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //find 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //count 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //delete 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        //count 검증
        count = memberRepository.count();
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void findByUserNameAndAgeGraterThen() {
        Member member = new Member("A", 10);
        Member member2 = new Member("A", 20);

        memberRepository.save(member);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("A", 15);

        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member = new Member("AA", 10);
        member.setTeam(team);
        memberRepository.save(member);


        List<MemberDto> memberDtos = memberRepository.findMemberDto();

        for (MemberDto memberDto : memberDtos) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findByNames() {
        Member member = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);

        memberRepository.save(member);
        memberRepository.save(member2);


        List<Member> members = memberRepository.findByNames(Arrays.asList("A", "B"));
        assertThat(members.size()).isEqualTo(2);

    }

    @Test
    public void returnType() {
        Member member = new Member("AAAA", 10);
        Member member2 = new Member("BBBB", 20);

        memberRepository.save(member);
        memberRepository.save(member2);

        Optional<Member> foundMember = memberRepository.findOptionalByUsername("AAAA");
        System.out.println("foundMember = " + foundMember.get());
    }


    @Test
    public void pagingTest() {
        //given
        memberRepository.save(new Member("member1", 100));
        memberRepository.save(new Member("member2", 100));
        memberRepository.save(new Member("member3", 100));
        memberRepository.save(new Member("member4", 100));
        memberRepository.save(new Member("member5", 100));
        memberRepository.save(new Member("member6", 100));

        int age = 100;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        //entity를 dto로 변환 (entity를 controller return value로 사용하지 말것)
        Page<MemberDto> pageDto = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        //then
        List<Member> contents = page.getContent();
        long count = page.getTotalElements();
        assertThat(contents.size()).isEqualTo(3);
        assertThat(count).isEqualTo(6);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

    }

    @Test
    public void sliceTest() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

//        //when
//        Slice<Member> page = memberRepository.findByAge(age, pageRequest);
//
//        //then
//        List<Member> contents = page.getContent();
//        assertThat(contents.size()).isEqualTo(3);
//        assertThat(page.getNumber()).isEqualTo(0);
//        assertThat(page.isFirst()).isTrue();
//        assertThat(page.hasNext()).isTrue();

    }

    @Test
    void bulkUpdate() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 20));
        memberRepository.save(new Member("member3", 30));
        memberRepository.save(new Member("member4", 40));

        //when
        int count = memberRepository.bulkAgePlus(25);

        //then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void fetchJoin() {
        //given
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");
        teamRepository.save(team1);
        teamRepository.save(team2);
        memberRepository.save(new Member("member1", 10, team1));
        memberRepository.save(new Member("member2", 20, team2));

        em.flush();
        em.clear();

        //when
        List<Member> members = memberRepository.findAll();

        //then
        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("team = " + member.getTeam().getName());
        }
    }

    @Test
    void callCustom() {
        List<Member> members = memberRepository.findMemberCustom();
    }
}
