package com.example.onbid.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
@Slf4j
public class OnbidApiService {

    private final RestTemplate restTemplate;
    
    public OnbidApiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);
    }
    
    @Value("${serviceKey}")
    private String serviceKey;

    public String fetchOnbidData() {
        if (serviceKey == null || serviceKey.trim().isEmpty() || "YOUR_SERVICE_KEY_HERE".equals(serviceKey)) {
            log.error("Service Key가 설정되지 않았습니다.");
            throw new RuntimeException("Service Key가 설정되지 않았습니다.");
        }
        
        log.info("Using Service Key: {}...", serviceKey.substring(0, Math.min(10, serviceKey.length())));
        
        StringBuilder allData = new StringBuilder();
        allData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><body><items>");
        
        for (int page = 1; page <= 5; page++) {
            String url = "http://openapi.onbid.co.kr/openapi/services/KamcoPblsalThingInquireSvc/getKamcoPbctCltrList" +
                    "?serviceKey=" + serviceKey +
                    "&numOfRows=100&pageNo=" + page + "&DPSL_MTD_CD=0001&CTGR_HIRK_ID=10000&CTGR_HIRK_ID_MID=10100&SIDO=경상북도";
            
            try {
                String response = restTemplate.getForObject(url, String.class);
                
                if (response != null && response.contains("<item>")) {
                    int startIdx = response.indexOf("<item>");
                    int endIdx = response.lastIndexOf("</item>") + 7;
                    if (startIdx != -1 && endIdx > startIdx) {
                        String items = response.substring(startIdx, endIdx);
                        allData.append(items);
                    }
                } else {
                    break;
                }
            } catch (ResourceAccessException e) {
                log.error("OnBid API 연결 타임아웃 on page {}: {}", page, e.getMessage());
                throw new RuntimeException("OnBid API 연결 타임아웃: " + e.getMessage());
            } catch (Exception e) {
                log.error("OnBid API Error on page {}: {}", page, e.getMessage());
                throw new RuntimeException("OnBid API 호출 실패: " + e.getMessage());
            }
        }
        
        allData.append("</items></body></response>");
        return allData.toString();
    }
}