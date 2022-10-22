package com.example.itmonster.module.board.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TmpApiController extends TmpAbstract{

	@GetMapping(value = "/boards", headers = HEADER)
	public void getBoards(){

	}
}
