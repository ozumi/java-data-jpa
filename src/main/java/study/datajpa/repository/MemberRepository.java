package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    List<Member> findByUsernameAndAgeGreaterThan(String name, int age);

    List<Member> findTop3HelloBy();

    //named query
    List<Member> findByUsername(@Param("username") String username);

    //query
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    //DTO로 가져오기
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    //parameter binding
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    //various return type
    //return empty collection if data not exist
    List<Member> findListByUsername(String username);
    //return null if data not exist -> better use optional
    //error occurs if more than two data exist
    Member findMemberByUsername(String username);
    Optional<Member> findOptionalByUsername(String username);

    Page<Member> findByAge(int age, Pageable pageable);

    //요청한 limit 수보다 1개 더 요청함
    //Slice<Member> findByAge(int age, Pageable pageable);

    //bulk update : 벌크연산 후에는 영속성 컨텍스트를 날려주는게 안전하다 (벌크는 db에 바로 업뎃함)
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age+1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @Query("select m from Member m")
    @EntityGraph(attributePaths = {"team"})
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value="true"))
    Member findReadOnlyByUsername(@Param("username") String username);

    @Lock(LockModeType.OPTIMISTIC)
    List<Member> findLockByUsername(@Param("username") String username);
}

