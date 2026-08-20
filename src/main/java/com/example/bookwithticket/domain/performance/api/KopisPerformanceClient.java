package com.example.bookwithticket.domain.performance.api;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class KopisPerformanceClient {

    @Value("${KOPIS_API_KEY:${kopis.api.key:354c7ba7822340b6ad2ca4bd25d429f2}}")
    private String apiKey;

    public List<KopisPerformanceResponse.Item> search(String keyword, int maxResults) {
        List<KopisPerformanceResponse.Item> items = new ArrayList<>();

        if (apiKey != null && !apiKey.isBlank() && !"test_kopis_service_key".equalsIgnoreCase(apiKey.trim())) {
            try {
                LocalDate today = LocalDate.now();
                String stdate = today.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String eddate = today.plusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                StringBuilder urlBuilder = new StringBuilder("http://www.kopis.or.kr/openApi/restful/pblprfr");
                urlBuilder.append("?service=").append(URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8));
                urlBuilder.append("&stdate=").append(stdate);
                urlBuilder.append("&eddate=").append(eddate);
                urlBuilder.append("&cpage=1");
                urlBuilder.append("&rows=").append(Math.min(maxResults, 30));

                if (keyword != null && !keyword.isBlank()) {
                    urlBuilder.append("&shprfnm=").append(URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8));
                }

                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                if (conn.getResponseCode() == 200) {
                    try (InputStream is = conn.getInputStream()) {
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(is);
                        doc.getDocumentElement().normalize();

                        NodeList nList = doc.getElementsByTagName("db");
                        for (int temp = 0; temp < nList.getLength(); temp++) {
                            Node nNode = nList.item(temp);
                            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element eElement = (Element) nNode;
                                String id = getTagValue("mt20id", eElement);
                                String title = getTagValue("prfnm", eElement);
                                String venue = getTagValue("fcltynm", eElement);
                                String poster = getTagValue("poster", eElement);
                                String genre = getTagValue("genrenm", eElement);

                                // KOPIS 상세 연동:
                                // 1) /pblprfr/{mt20id} 호출 ➔ mt10id (공연시설ID), prfpdfrom(시작일), dtguidance(시간안내) 파싱
                                // 2) /prfplc/{mt10id} 호출 ➔ seatscale (객석수) 실시간 파싱
                                PerformanceDetailInfo detailInfo = fetchPerformanceDetailInfo(id);

                                if (title != null && !title.isBlank()) {
                                    items.add(new KopisPerformanceResponse.Item(
                                            id, title, venue, poster, genre,
                                            detailInfo.seatscale,
                                            detailInfo.prfpdfrom,
                                            detailInfo.dtguidance
                                    ));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[KOPIS API 통신 경고] 실시간 KOPIS Open API 연결 실패: " + e.getMessage());
            }
        }

        if (items.isEmpty()) {
            items.addAll(getFallbackKopisItems());
            if (keyword != null && !keyword.isBlank()) {
                items = items.stream()
                        .filter(i -> i.getPrfnm().contains(keyword) || i.getGenrenm().contains(keyword))
                        .toList();
            }
        }

        return items.stream().limit(maxResults).toList();
    }

    private static class PerformanceDetailInfo {
        String mt10id;
        String prfpdfrom;
        String dtguidance;
        Integer seatscale;
    }

    private PerformanceDetailInfo fetchPerformanceDetailInfo(String mt20id) {
        PerformanceDetailInfo info = new PerformanceDetailInfo();
        if (mt20id == null || mt20id.isBlank()) return info;
        try {
            // 1단계: 공연 상세조회 -> mt10id, prfpdfrom, dtguidance 구하기
            String perfDetailUrl = "http://www.kopis.or.kr/openApi/restful/pblprfr/" + mt20id + "?service=" + URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8);

            URL url1 = new URL(perfDetailUrl);
            HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
            conn1.setRequestMethod("GET");
            conn1.setConnectTimeout(2000);
            conn1.setReadTimeout(2000);

            if (conn1.getResponseCode() == 200) {
                try (InputStream is = conn1.getInputStream()) {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(is);
                    doc.getDocumentElement().normalize();

                    NodeList nList = doc.getElementsByTagName("db");
                    if (nList.getLength() > 0) {
                        Element eElement = (Element) nList.item(0);
                        info.mt10id = getTagValue("mt10id", eElement);
                        info.prfpdfrom = getTagValue("prfpdfrom", eElement);
                        info.dtguidance = getTagValue("dtguidance", eElement);
                    }
                }
            }

            // 2단계: 공연시설(공연장) 상세조회 -> <seatscale> 객석수 파싱
            if (info.mt10id != null && !info.mt10id.isBlank()) {
                String venueDetailUrl = "http://www.kopis.or.kr/openApi/restful/prfplc/" + info.mt10id + "?service=" + URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8);
                URL url2 = new URL(venueDetailUrl);
                HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                conn2.setRequestMethod("GET");
                conn2.setConnectTimeout(2000);
                conn2.setReadTimeout(2000);

                if (conn2.getResponseCode() == 200) {
                    try (InputStream is = conn2.getInputStream()) {
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(is);
                        doc.getDocumentElement().normalize();

                        NodeList nList = doc.getElementsByTagName("db");
                        if (nList.getLength() > 0) {
                            Element eElement = (Element) nList.item(0);
                            String scaleStr = getTagValue("seatscale", eElement);
                            info.seatscale = parseSeatScale(scaleStr);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return info;
    }

    private Integer parseSeatScale(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            String cleaned = str.replaceAll("[^0-9]", "");
            return cleaned.isEmpty() ? null : Integer.parseInt(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    private String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag);
        if (nlList != null && nlList.getLength() > 0) {
            Node nValue = nlList.item(0);
            if (nValue != null) return nValue.getTextContent();
        }
        return null;
    }

    private List<KopisPerformanceResponse.Item> getFallbackKopisItems() {
        return new ArrayList<>();
    }
}
