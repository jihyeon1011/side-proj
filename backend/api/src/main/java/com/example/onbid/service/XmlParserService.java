package com.example.onbid.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

@Service
public class XmlParserService {

    public List<Map<String, String>> parseOnbidXmlGroupedByItem(String xmlData) {
        List<Map<String, String>> items = parseOnbidXml(xmlData);
        Map<String, Map<String, Object>> groupedItems = new LinkedHashMap<>();
        
        for (Map<String, String> item : items) {
            String 물건명 = item.get("물건명");
            if (물건명 != null && !물건명.isEmpty()) {
                if (!groupedItems.containsKey(물건명)) {
                    Map<String, Object> groupedItem = new HashMap<>();
                    groupedItem.putAll(item);
                    groupedItem.put("공매번호들", new ArrayList<String>());
                    groupedItem.put("이력번호들", new ArrayList<String>());
                    groupedItems.put(물건명, groupedItem);
                } else {
                    // 가장 최신 입찰마감일시로 업데이트
                    Map<String, Object> existingItem = groupedItems.get(물건명);
                    String currentDate = item.get("입찰마감일시");
                    String existingDate = (String) existingItem.get("입찰마감일시");
                    
                    if (currentDate != null && (existingDate == null || currentDate.compareTo(existingDate) > 0)) {
                        existingItem.put("입찰시작일시", item.get("입찰시작일시"));
                        existingItem.put("입찰마감일시", item.get("입찰마감일시"));
                    }
                }
                
                Map<String, Object> existingItem = groupedItems.get(물건명);
                ((List<String>) existingItem.get("공매번호들")).add(item.get("공매번호"));
                ((List<String>) existingItem.get("이력번호들")).add(item.get("물건이력번호"));
            }
        }
        
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, Object> groupedItem : groupedItems.values()) {
            Map<String, String> resultItem = new HashMap<>();
            for (Map.Entry<String, Object> entry : groupedItem.entrySet()) {
                if (entry.getValue() instanceof List) {
                    List<String> list = (List<String>) entry.getValue();
                    resultItem.put(entry.getKey(), String.join(", ", list));
                } else {
                    resultItem.put(entry.getKey(), (String) entry.getValue());
                }
            }
            resultItem.put("공고건수", String.valueOf(((List<String>) groupedItem.get("공매번호들")).size()));
            result.add(resultItem);
        }
        
        return result;
    }
    
    public List<Map<String, String>> parseOnbidXml(String xmlData) {
        List<Map<String, String>> items = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlData.getBytes("UTF-8")));
            
            NodeList itemNodes = doc.getElementsByTagName("item");
            
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element item = (Element) itemNode;
                    Map<String, String> itemData = new HashMap<>();
                    
                    itemData.put("공고번호", getElementValue(item, "PLNM_NO"));
                    itemData.put("공매번호", getElementValue(item, "PBCT_NO"));
                    itemData.put("물건번호", getElementValue(item, "CLTR_NO"));
                    itemData.put("물건이력번호", getElementValue(item, "CLTR_HSTR_NO"));
                    itemData.put("물건명", getElementValue(item, "CLTR_NM"));
                    itemData.put("물건소재지", getElementValue(item, "LDNM_ADRS"));
                    itemData.put("도로명주소", getElementValue(item, "NMRD_ADRS"));
                    itemData.put("처분방식", getElementValue(item, "DPSL_MTD_NM"));
                    itemData.put("입찰방식", getElementValue(item, "BID_MTD_NM"));
                    itemData.put("최저입찰가", formatPrice(getElementValue(item, "MIN_BID_PRC")));
                    itemData.put("감정가", formatPrice(getElementValue(item, "APSL_ASES_AVG_AMT")));
                    itemData.put("최저입찰가율", getElementValue(item, "FEE_RATE"));
                    itemData.put("입찰시작일시", formatDateTime(getElementValue(item, "PBCT_BEGN_DTM")));
                    itemData.put("입찰마감일시", formatDateTime(getElementValue(item, "PBCT_CLS_DTM")));
                    itemData.put("물건상태", getElementValue(item, "PBCT_CLTR_STAT_NM"));
                    itemData.put("유찰횟수", getElementValue(item, "USCBD_CNT"));
                    itemData.put("조회수", getElementValue(item, "IQRY_CNT"));
                    itemData.put("물건상세정보", getElementValue(item, "GOODS_NM"));
                    
                    items.add(itemData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return items;
    }
    
    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node != null ? node.getTextContent() : "";
        }
        return "";
    }
    
    private String formatPrice(String price) {
        if (price == null || price.isEmpty()) return "";
        try {
            long amount = Long.parseLong(price);
            return String.format("%,d원", amount);
        } catch (NumberFormatException e) {
            return price;
        }
    }
    
    private String formatDateTime(String dateTime) {
        if (dateTime == null || dateTime.length() != 14) return dateTime;
        try {
            String year = dateTime.substring(0, 4);
            String month = dateTime.substring(4, 6);
            String day = dateTime.substring(6, 8);
            String hour = dateTime.substring(8, 10);
            String minute = dateTime.substring(10, 12);
            return String.format("%s년 %s월 %s일 %s:%s", year, month, day, hour, minute);
        } catch (Exception e) {
            return dateTime;
        }
    }
}