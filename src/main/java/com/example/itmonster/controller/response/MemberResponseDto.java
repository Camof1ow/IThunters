package com.example.itmonster.controller.response;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto implements Serializable {
  private static final long serialVersionUID = 5778203755690043226L;

  private Long id;
  private String nickname;
  private String profileImage;
  private String className;
  private List<StackDto> stacks;
  private Long followCnt;
  private String folioTitle;
  //추후 포트폴리오 이름
}
