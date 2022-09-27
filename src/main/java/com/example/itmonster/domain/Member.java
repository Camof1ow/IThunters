package com.example.itmonster.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Formula;


@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Getter
public class Member extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String nickname;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	@JsonIgnore
	private String password;

	@Column
	private String profileImg;

	@Column(nullable = false)
	private String className;

	@Column(nullable = false, unique = true)
	private String phoneNumber;

	@Column(unique = true)
	private String socialId;

	@Column(nullable = false)
	@Enumerated(value = EnumType.STRING) //DB갈 때 올 때 값을 String으로 변환해줘야함
	private RoleEnum role;

	@Formula("(select count(*) from follow where follow.me_id=id)")
	private Long followCounter;

	@OneToMany(mappedBy = "member")
	private List<StackOfMember> stackOfMemberList;

	// 스택 추가

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
			return false;
		}
		Member member = (Member) o;
		return id != null && Objects.equals(id, member.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}


//	public void updatePhoneNumber(String phoneNum) {
//		this.phoneNumber = phoneNum;
//	}

	public void updateNickname(String nickname){
		this.nickname = nickname;
	}

	public void updateClassName(String className){
		this.className = className;
	}

	public void updateProfileImg(String profileImg){
		this.profileImg = profileImg;
	}



	public void updateStack(List<StackOfMember> stackOfMemberList) {
		this.stackOfMemberList = stackOfMemberList;
	}

}
