package study.datajpa.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import java.time.LocalDateTime;


@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item implements Persistable<String> {
    //@Generated 를 사용하지 않으면 이미 식별자가 들어있어서, save 호출시 em.persist()가 아닌 em.merge()를 호출해서 find가 한번 발생하게 되는 비효율 발생함
    @Id
    String id;

    @CreatedDate
    LocalDateTime localDateTime;

    public Item(String id) {
        this.id = id;
    }

    //새로 생성된 객체인지 확인하는 메소드를 추가함
    public boolean isNew() {
        return localDateTime == null;
    }
}
