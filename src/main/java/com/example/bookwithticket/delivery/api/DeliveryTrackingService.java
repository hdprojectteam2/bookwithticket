package com.example.bookwithticket.delivery.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DeliveryTrackingService {

	@Value("${sweettracker_API_KEY}")
	private String apiKey;

	public String getTrackingInfo(String courier, String invoice) {

		String companyCode = getCompanyCode(courier);

		if (companyCode == null) {
			throw new IllegalArgumentException("지원하지 않는 택배사입니다.");
		}

		if (invoice == null || invoice.isBlank()) {
			throw new IllegalArgumentException("운송장 번호가 없습니다.");
		}

		String url = UriComponentsBuilder.fromHttpUrl("https://info.sweettracker.co.kr/api/v1/trackingInfo")
				.queryParam("t_key", apiKey).queryParam("t_code", companyCode).queryParam("t_invoice", invoice)
				.toUriString();

		RestTemplate restTemplate = new RestTemplate();

		return restTemplate.getForObject(url, String.class);
	}

	private String getCompanyCode(String courier) {

		if (courier == null) {
			return null;
		}

		courier = courier.trim();

		return switch (courier) {

		case "우체국택배" -> "01";
		case "CJ대한통운" -> "04";
		case "한진택배" -> "05";
		case "로젠택배" -> "06";
		case "롯데택배" -> "08";
		case "일양로지스" -> "11";
		case "한의사랑택배" -> "16";
		case "천일택배" -> "17";
		case "건영택배" -> "18";
		case "한진택배B2B" -> "20";
		case "대신택배" -> "22";
		case "경동택배" -> "23";
		case "GS Postbox 택배" -> "24";
		case "합동택배" -> "32";
		case "애니트랙" -> "43";
		case "SLX택배" -> "44";
		case "우리택배(구호남택배)" -> "45";
		case "CU편의점택배" -> "46";
		case "농협택배" -> "53";
		case "홈픽택배" -> "54";
		case "IK물류" -> "71";
		case "성훈물류" -> "72";
		case "CR로지텍" -> "73";
		case "용마로지스" -> "74";
		case "원더스퀵" -> "75";
		case "컬리넥스트마일" -> "82";
		case "풀앳홈" -> "85";
		case "두발히어로" -> "89";
		case "위니아딤채" -> "90";
		case "지니고 당일배송" -> "92";
		case "카카오 T 당일배송" -> "94";
		case "트랙스로지스" -> "95";
		case "나은물류" -> "100";
		case "한샘서비스" -> "101";
		case "도도플렉스(dodoflex)" -> "104";
		case "LG전자" -> "107";
		case "썬더히어로" -> "113";
		case "롯데칠성" -> "118";
		case "핑퐁" -> "119";
		case "발렉스 특수물류" -> "120";
		case "엔티엘피스" -> "123";
		case "지케이글로벌" -> "125";
		case "로지스팟" -> "127";
		case "홈픽 오늘도착" -> "129";
		case "로지스파트너" -> "130";
		case "딜리래빗" -> "131";
		case "지오피" -> "132";
		case "에이치케이홀딩스" -> "134";
		case "HTNS" -> "135";
		case "케이제이티" -> "136";
		case "더함" -> "137";
		case "라스트마일" -> "138";
		case "탱고앤고" -> "142";
		case "투데이" -> "143";
		case "ARGO(테크타카)" -> "148";
		case "HY" -> "155";
		case "유피로지스(제주)" -> "156";
		case "반얀로지스틱스" -> "157";
		case "프리즘코리아" -> "160";
		case "위니온" -> "163";
		case "딜리박스" -> "167";
		case "자하" -> "168";
		case "올인닷컴" -> "171";
		case "물류대장(택배)" -> "173";
		case "풀무원샘물㈜" -> "175";
		case "SLO(모든)" -> "179";
		case "바로스" -> "183";
		case "레터스" -> "186";
		case "벤더피아" -> "187";
		case "세븐일레븐 착한택배" -> "189";
		case "물류대장(설치)" -> "190";
		case "BoxN" -> "191";
		case "리터니즈" -> "192";

		default -> null;
		};
	}
}