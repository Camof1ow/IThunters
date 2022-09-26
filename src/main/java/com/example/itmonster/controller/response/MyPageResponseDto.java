package com.example.itmonster.controller.response;


import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageResponseDto {

	private Long memberId;
	private String profileUrl;
	private List<StackDto> stackList;
	private String title;
	private String notionUrl;
	private String githubUrl;
	private String blogUrl;
	private List<CompletedQuestDto> completedQuestList;
	private Boolean followStatus;
	private Boolean loginStatus;

}
