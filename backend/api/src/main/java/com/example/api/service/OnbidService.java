package com.example.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OnbidService {

    private static final Logger logger = LoggerFactory.getLogger(OnbidService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${serviceKey}")
    private String serviceKey;

    public String getOnbidData() {
        logger.info("Using Service Key: {}", serviceKey);
        
        StringBuilder allData = new StringBuilder();
        allData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><body><items>");
        
        // 5페이지까지 가져오기 (최대 500건)
        for (int page = 1; page <= 5; page++) {
            String url = "http://openapi.onbid.co.kr/openapi/services/KamcoPblsalThingInquireSvc/getKamcoPbctCltrList" +
                    "?serviceKey=" + serviceKey +
                    "&numOfRows=100&pageNo=" + page + "&DPSL_MTD_CD=0001&CTGR_HIRK_ID=10000&CTGR_HIRK_ID_MID=10100&SIDO=경상북도";
            
            logger.info("OnBid API URL (Page {}): {}", page, url);
            
            try {
                String response = restTemplate.getForObject(url, String.class);
                logger.info("API Response (first 500 chars): {}", response != null ? response.substring(0, Math.min(500, response.length())) : "null");
                
                // XML에서 item 데이터만 추출
                if (response != null && response.contains("<item>")) {
                    int startIdx = response.indexOf("<item>");
                    int endIdx = response.lastIndexOf("</item>") + 7;
                    if (startIdx != -1 && endIdx > startIdx) {
                        String items = response.substring(startIdx, endIdx);
                        allData.append(items);
                    }
                } else {
                    logger.info("Page {} has no more data", page);
                    break;
                }
            } catch (Exception e) {
                logger.error("OnBid API Error on page {}: {}", page, e.getMessage());
                break;
            }
        }
        
        allData.append("</items></body></response>");
        String result = allData.toString();
        logger.info("Total combined data length: {}", result.length());
        return result;
    }
}