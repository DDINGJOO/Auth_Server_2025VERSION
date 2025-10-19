package com.teambiund.bander.auth_server.entity.consentsname;

import com.teambiund.bander.auth_server.entity.Consent;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "consents_name")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentsTable {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "consent_name")
  private String consentName;

  @Column(name = "version")
  private String version;

  @Column(name = "consent_url")
  private String consentUrl;

  @Column(name = "required")
  private boolean required;
	
	/**
	 * 주어진 두 버전 문자열을 비교하여 v1이 v2보다 최신인지 판단한다.
	 * - "v" 또는 "V" 접두사는 무시한다.
	 * - "v1.2" 형태를 "1.2"로 바꿔 각 숫자 파트를 비교한다.
	 * - 숫자로 파싱할 수 없는 파트는 0으로 간주한다.
	 *
	 * @param v2 기존 버전 (예: "v1.1")
	 * @return v1이 v2보다 최신이면 true, 같거나 이전이면 false
	 */
	public boolean isNewerVersion(String v2) {
		String v1 = this.getVersion();
		if (Objects.equals(v1, v2)) return false;
		if (v1 == null) return false;
		if (v2 == null) return true;
		
		String n1 = stripVersionPrefix(v1);
		String n2 = stripVersionPrefix(v2);
		
		String[] p1 = n1.split("\\.");
		String[] p2 = n2.split("\\.");
		int max = Math.max(p1.length, p2.length);
		
		for (int i = 0; i < max; i++) {
			int a = i < p1.length ? parseNumberPart(p1[i]) : 0;
			int b = i < p2.length ? parseNumberPart(p2[i]) : 0;
			if (a != b) return a > b;
		}
		
		// 모든 숫자 파트가 동일한 경우, 더 많은 파트를 가진 쪽을 최신으로 간주
		if (p1.length != p2.length) return p1.length > p2.length;
		
		// 최후 수단: 사전적 문자열 비교 (드문 케이스)
		return n1.compareTo(n2) > 0;
	}
	
	private String stripVersionPrefix(String v) {
		if (v == null) return null;
		return v.replaceFirst("^[vV]", "");
	}
	
	private int parseNumberPart(String part) {
		if (part == null) return 0;
		String digits = part.replaceAll("[^0-9]", "");
		if (digits.isEmpty()) return 0;
		try {
			return Integer.parseInt(digits);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
 
}
