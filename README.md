# Kafka


# 코드 리팩토링: 관심사 분리와 이벤트 핸들러 구조 도입

프로젝트가 커질수록 한 메서드나 클래스에 너무 많은 로직이 몰리면 유지보수성이 떨어지고, 변경 시 파급 범위가 커집니다.  
아래에서는 외부 API 호출 이후 동기식 처리 로직을 이벤트 핸들러(Event Handler) 구조로 분리했을 때 얻을 수 있는 장점과, 그에 따른 단점 및 성능 이슈를 정리합니다.

---

## 1. 관심사(Concern) 분리

### 1.1 기존 방식의 문제점
- **동기식 처리 로직의 복잡도 증가**  
  ```java
  // Controller 내에서 외부 API 호출 → 후속 로직 처리
  public ResponseEntity<?> sendSomething(RequestDto dto) {
      ExternalResponse res = externalSenderService.send(dto);
      // TODO: API 호출 후 여러 후속 작업이 여기에 몰리면…
      notificationService.notify(res);
      auditService.log(res);
      return ResponseEntity.ok(res);
  }
