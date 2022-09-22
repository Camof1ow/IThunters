package com.example.itmonster.service;

import com.example.itmonster.domain.Member;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.repository.MemberRepository;
import com.example.itmonster.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class SmsService {

    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;

    String hostNameUrl = "https://sens.apigw.ntruss.com";     		// 호스트 URL
    String requestUrl= "/sms/v2/services/";                   		// 요청 URL
    String requestUrlType = "/messages";                           // 요청 URL
    @Value("${spring.naver.accessKey}")
    String accessKey;                                               // 개인 인증키
    @Value("${spring.naver.secretKey}")
    String secretKey;                                               // 2차 인증을 위해 서비스마다 할당되는 service secret
    @Value("${spring.naver.from}")
    String from;                                                   // naver cloud service에 등록된 발송번호
    @Value("${spring.naver.serviceId}")
    String serviceId;        									// 프로젝트에 할당된 SMS 서비스 ID
    String method = "POST";											// 요청 method


    @Transactional
    public String sendSms(String to) throws NoSuchAlgorithmException, InvalidKeyException {
        if(redisUtil.getData(to) != null) return "60초 후 재시도하여 주십시오";
        if(to.length() != 11) throw new CustomException(ErrorCode.PHONENUMBER_LENGTH);
        String timestamp = Long.toString(System.currentTimeMillis());

        //난수생성
        int authNo = (int)(Math.random() * (9999 - 1000 + 1)) + 1000;
        redisUtil.setDataExpire(to,String.valueOf(authNo),60L);

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json;charset=utf-8");
        headers.add("x-ncp-apigw-timestamp",timestamp);
        headers.add("x-ncp-iam-access-key", accessKey);
        headers.add("x-ncp-apigw-signature-v2", makeSignature(requestUrl+serviceId+requestUrlType, timestamp, method, accessKey, secretKey));


        // Json Body 생성
        JSONObject bodyJson = new JSONObject();
        JSONObject toJson = new JSONObject();
        JSONArray  toArr = new JSONArray();

        toJson.put("to",to);						// Mandatory(필수), messages.to	수신번호, -를 제외한 숫자만 입력 가능
        toArr.put(toJson);
        bodyJson.put("type","SMS");				    // Madantory, 메시지 Type (SMS | LMS | MMS), (소문자 가능)
        bodyJson.put("from",from);					// Mandatory, 발신번호, 사전 등록된 발신번호만 사용 가능
        bodyJson.put("content","ITmonster 인증번호 ["+authNo+"]\n 60초간 유효합니다.");	// Mandatory(필수), 기본 메시지 내용, SMS: 최대 80byte, LMS, MMS: 최대 2000byte
        bodyJson.put("messages", toArr);

        //Naver api 요청발송
        HttpEntity<String> entity = new HttpEntity<>(bodyJson.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<String> response = restTemplate.exchange(
                "https://sens.apigw.ntruss.com/sms/v2/services/"+serviceId+"/messages",
                HttpMethod.POST,
                entity,
                String.class
        );

        String responseBody = response.getBody();
        //발송 실패시 로직 구현필요
        System.out.println(responseBody);
        System.out.println(bodyJson);
        System.out.println(to + "메시지 발송 완료");
        return responseBody;

    }





    public static String makeSignature(String url, String timestamp,
                                        String method, String accessKey, String secretKey)
        throws NoSuchAlgorithmException, InvalidKeyException {

        String space = " ";
        String newLine = "\n";

        String message = method
            + space
            + url
            + newLine
            + timestamp
            + newLine
            + accessKey;

        SecretKeySpec signingKey;
        String encodeBase64String;

        try {
            signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);
        } catch (UnsupportedEncodingException e) {
            encodeBase64String = e.toString();
        }


        return encodeBase64String;
    }
}

