## `NotificationController` 리팩토링

### 1. 관심사 분리
- **외부 API 호출 후 동기식 처리**  
  동기식으로 이어지는 로직이 많아지면 코드가 복잡해집니다.  
  이벤트-핸들러 구조를 사용하면 흐름을 분리해 깔끔하게 유지할 수 있습니다.
- **`externalSenderService` 변경 시 전파 범위**  
  서비스가 바뀌면 컨트롤러까지 영향을 받습니다.
- **컨트롤러마다 다른 반환값 처리**  
  - 컨트롤러별로 결과 처리 방식이 다르면 리스너를 여러 개 둘 수도 있습니다.  
  - 하지만 서비스가 바뀔 때마다 **리스너도 전부 수정**해야 합니다.  
  - _“이럴 거면 굳이?”_ 라는 의문이 생깁니다.
> 사실 리스너를 여러개 둘 필요없이, `ExternalSenderService`을 변경하면 위 문제는 해결됌

#### 이벤트-핸들러의 단점
- 코드의 흐름을 따라가기 어렵다.  
- 의존성 분리가 필수적이다.

---

### 2. 성능?
- 이벤트-핸들러를 사용했을 때의 성능 이슈는?
- 
#### 이벤트-핸들러의 장점
- 비동기와 결합해서 사용했을 때 응답 속도가 개선될 수 있다.
- @transactionaleventlistener를 사용하면 트랜잭션 상태에 따라 이벤트 실행


# AOP랑 이벤트핸들러 중 어떤걸로 구현할 것인가?
1. AOP는 적용 범위가 큰 경우
2. 이벤트핸들러는 적용범위가 더 작은 경우
3. 전처리 후처리 요구사항이 많으면 `Aop`가 이벤트 핸들러보다 더 많다.



# 이벤트 핸들러 사용 사례

## 김성현 - 쇼핑시스템 구축
- '상의' 카데고리에서 가장 저렴한 상품 조회
  - 매번 DB에서 가장 낮은 가격을 조회하면 성능이 떨어짐 (만약 '상의' 카데고리 상품이 100,0000개 라면?)
> 위 문제를 해결하기 위해 캐시 도입

- 상품을 CRUD 할 때, 캐시에 반영 되어야함
  - 상품 create, update, delete 시, 캐시 반영 로직 넣어야함
 
> 만약 캐시 관련 로직이 많아진다면?
> 예를 들어, '브랜드' 카데고리에서 가장 저렴한 상품 조회, 브랜드의 전체 상품 가격을 합쳤을 떄 가장 저렴한 브랜드 등 상품 관련 캐싱이 필요한 기능들이 추가된다면?

- 상품을 CRUD 할 때 여러 캐시에 반영되어야한다.
  - 레디스로 가정하면, 상품 등록시 3개의 키에 대한 로직을 구성해야함

> 상품 CRUD 시, DB에 넣어야 하는 로직 뿐만 아니라, 캐싱 관련 로직이 계속해서 추가된다.
> 관심사 분리하기 위해, 이벤트 핸들러로 캐시 로직에 대한 의존성을 분리하였다.

### before

```
	@Override
	@Transactional
	public Product createProduct(String brandName, String categoryName, long priceValue) {
		Category category = selectCategoryPort.findByName(categoryName);
		Product product = saveProductPort.saveProduct(brandName, categoryName, priceValue);

    // 해당 카데고리 최저가 캐시 업데이트
		Product minPriceProduct = productCachePort.getMinPrice(category);
		// 비교
		if (minPriceProduct == null || product.getId() == minPriceProduct.getId() || product.getPriceValue() < minPriceProduct.getPriceValue()) {
			productCachePort.putMinPrice(category.getName(), product);
		}

    // 해당카데고리 최고가 캐시 업데이트
		Product maxPriceProduct = productCachePort.getMaxPrice(category);
		// 비교
		if (maxPriceProduct == null || product.getId() == maxPriceProduct.getId() || product.getPriceValue() > maxPriceProduct.getPriceValue()) {
			productCachePort.putMaxPrice(category.getName(), product);
		}

		// 브랜드 총합 캐시 업데이트
		Long oldTotal = brandCachePort.getBrandTotal(brandName);
		long newTotal = (oldTotal == null ? 0 : oldTotal) + priceValue;
		brandCachePort.putBrandTotal(brandName, newTotal);

    // 상품 관련 캐시에 추가될 수록 아래 코드 늘어남

		return product;
	}
```

### After

```
	@Override
	@Transactional
	public Product createProduct(String brandName, String categoryName, long priceValue) {
		Category category = selectCategoryPort.findByName(categoryName);
		Product product = saveProductPort.saveProduct(brandName, categoryName, priceValue);

    eventPublisher.publishEvent(new ProductCreatedEvent(this, product));

		return product;
	}
```


`ProductEventListener.java`

```
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleProductCreated(ProductCreatedEvent event) {
		Product product = event.getProduct();

    // 해당 카데고리 최저가 캐시 업데이트
		Product minPriceProduct = productCachePort.getMinPrice(category);
		// 비교
		if (minPriceProduct == null || product.getId() == minPriceProduct.getId() || product.getPriceValue() < minPriceProduct.getPriceValue()) {
			productCachePort.putMinPrice(category.getName(), product);
		}

    // 해당카데고리 최고가 캐시 업데이트
		Product maxPriceProduct = productCachePort.getMaxPrice(category);
		// 비교
		if (maxPriceProduct == null || product.getId() == maxPriceProduct.getId() || product.getPriceValue() > maxPriceProduct.getPriceValue()) {
			productCachePort.putMaxPrice(category.getName(), product);
		}

		// 브랜드 총합 캐시 업데이트
		Long oldTotal = brandCachePort.getBrandTotal(brandName);
		long newTotal = (oldTotal == null ? 0 : oldTotal) + priceValue;
		brandCachePort.putBrandTotal(brandName, newTotal);
	}
```

### 의문

---

## 김성민 - 회원 + 쿠폰 + 메일 시나리오
- 신규 회원이 가입하면 가입 축하 쿠폰 지급과 쿠폰 지급 안내 이메일 전송이 필요한 가상의 서비스 (이커머스, 게임, 등 범용적으로 존재 가능)

#### 문제점
- 회원을 등록하는 작업과는 무관한 부수적인 비즈니스가 포함되 다른 도메인에 결합도가 강해짐
- 현재는 쿠폰, 메일만 지급하지만 다른 부수적인 요구사항이 생기면 결합도가 강해질 가능성이 있음
- 단일 책임 원칙(SRP)에 위반

### Before
```java
/***
 * 유저 등록에 쿠폰과, 메일 도메인이 강결합 된 상태
 */
public void register(MemberDto memberDto) {
    Member member = memberRepository.save(memberDto.toEntity());

    // 1. 가입 축하 쿠폰 지급
    couponService.issueSignupCoupon(member.getId());

    // 2. 가입 완료 이메일 전송
    mailService.sendCouponIssuedMail(member.getEmail());
}
```
### After

```java
/***
 * MemberService.java
 * 이벤트를 발행하여 관심사 분리 및 의존성 제거
 */
public void register(MemberDto memberDto) {
    Member member = memberRepository.save(memberDto.toEntity());
    eventPublisher.publishEvent(new MemberRegisteredEvent(member.getId(), member.getEmail()));
}

/***
 * CouponEventListener.java
 */
@EventListener
public void handle(MemberRegisteredEvent event) {
        couponService.issueSignupCoupon(event.getMemberId());
}

/***
 * MailEventListener.java
 */
@EventListener
public void handle(MemberRegisteredEvent event) {
        mailService.sendCouponIssuedMail(event.getEmail());
}
```

### 의문
-  이벤트 발행 자체가 실패하는 케이스를 대비하는 방법

---

## 정예림 - 메세지/파일 백업 후 로그


```java
/***
 * BackupService.java
 */
public void backupMsg(MessageDto messageDto) {
    // backup
    Message msg = messageRepository.save(messageDto.toEntity());
    eventPublisher.publishEvent(new BackupEvent(msg.getId()));
}

/***
 * BackupEventListener.java
 */
@EventListener
public void handle(BackupEvent event) {
    logService.post(event); // 로그 서버로 post 요청
}
```

---

@EventListener
- 단일 애플리케이션 내의 이벤트 처리에는 유용하지만,
- 마이크로서비스 환경에서는 이벤트 전파 범위, 메시지 신뢰성, 장애 복구, 확장성 측면에서 한계
- MSA 구조에서는 Kafka나 RabbitMQ 같은 외부 메시지 브로커 기반의 이벤트 시스템이 필수적!

| **상황** | **사용 도구** | **이유** |
| --- | --- | --- |
| 같은 애플리케이션 내에서 후속 처리 | @EventListener | 간단, 코드로 관리 가능 |
| 비동기 후처리 (예: 이메일 발송) | @Async + @EventListener | 비동기 처리 간편 |
| 서비스 간 통신이 필요한 경우 | Kafka | 분산 환경에 적합 |
| 대용량 로그/이벤트 처리 | Kafka | 안정적이고 확장 가능 |
| 트랜잭션 이후 이벤트 발생 필요 | @TransactionalEventListener | DB commit 이후 실행 보장 |

## 김영현 - 주문 결제 시나리오 (AOP vs 이벤트 핸들러 고민)

### 시나리오
#### 사용자가 상품을 주문하면, 다음 작업들이 필요하다:
1. 재고 차감
2. 결제 처리
3. 주문 내역 저장
4. 포인트 적립
5. 구매확정 알림톡 또는 이메일 발송
6. 마케팅 팀에 구매 정보 전달 (이벤트 알림, 분석용)

⸻

### 문제점
* 주문 처리 도메인(OrderService)에 다양한 부수 로직이 혼합되어 있음.
* 기능이 늘어나면, 코드가 복잡해지고 유지보수가 어려워짐.
* SRP (단일 책임 원칙) 위배.
* 각 작업의 성격이 다름 (트랜잭션 내 작업, 비동기 작업 등).

⸻

### Before
```java
@Transactional
public void placeOrder(OrderRequest orderRequest) {
    // 1. 주문 저장
    Order order = orderRepository.save(orderRequest.toEntity());

    // 2. 재고 차감
    inventoryService.decrease(order.getProductId(), order.getQuantity());

    // 3. 결제 처리
    paymentService.processPayment(order.getId(), orderRequest.getPaymentInfo());

    // 4. 포인트 적립
    pointService.addPoints(order.getUserId(), order.calculateRewardPoints());

    // 5. 알림톡 또는 이메일 발송
    notificationService.sendOrderConfirmation(order.getUserEmail());

    // 6. 마케팅용 주문 데이터 전송
    marketingService.sendPurchaseEvent(order);
}
```

⸻

### After (이벤트 기반 리팩토링)
```java
@Transactional
public void placeOrder(OrderRequest orderRequest) {
    Order order = orderRepository.save(orderRequest.toEntity());
    inventoryService.decrease(order.getProductId(), order.getQuantity());
    paymentService.processPayment(order.getId(), orderRequest.getPaymentInfo());

    // 핵심 도메인 처리 후 이벤트 발행
    eventPublisher.publishEvent(new OrderCompletedEvent(order));
}

// OrderEventListener.java
@EventListener
public void handle(OrderCompletedEvent event) {
    Order order = event.getOrder();

    // 포인트 적립
    pointService.addPoints(order.getUserId(), order.calculateRewardPoints());

    // 알림 발송
    notificationService.sendOrderConfirmation(order.getUserEmail());

    // 마케팅 시스템에 이벤트 전달
    marketingService.sendPurchaseEvent(order);
}
```

⸻

### AOP 도입 고민 포인트

만약 다음과 같은 요구사항이 추가된다면 AOP가 적합할 수도 있음:
* 모든 서비스 메서드에 대해 공통적으로 이벤트 로그 저장, 보안 체크, 트랜잭션 커밋 후 감사 로그 기록 등 횡단 관심사 처리가 필요할 때
* 예: @AfterReturning 으로 특정 서비스에서 결제가 성공한 후 감사 로그 기록

```java
@AfterReturning(pointcut = "execution(* com.example.service.PaymentService.processPayment(..))", returning = "result")
public void auditLogPayment(JoinPoint joinPoint, Object result) {
        // 감사 로그 처리
}
```
---

## 노혜주 - 이벤트 핸들러가 왜 필요한가??

* 참고 문서 : https://techblog.woowahan.com/7835/
### 회원 시스템 이벤트 기반 아키텍처 구축
“회원의 본인인증이 초기화되는 경우 가족계정 서비스에서 탈퇴되어야 한다" 라는 정책
#### 강한 결합을 가진 구조
```java
pubic void initCerticationOwn(MemberNumber memberNumber) {
	member.initCerticationOwn(memberNumber);
	family.leave(memberNumber);
}
```
* 가족계정 서비스 탈퇴 로직은 회원의 본인인증 해제 로직에 깊게 관여되어 강한 결합
#### 하나의 시스템에 존재하던 두 도메인의 물리적인 분리
```java
pubic void initCerticationOwn(MemberNumber memberNumber) {
	member.initCerticationOwn(memberNumber);
	familyClient.leave(memberNumber);
}
```
* 마이크로서비스를 구성 -> 물리적인 시스템의 분리 -> 코드 레벨의 호출이 동기적인 HTTP 통신으로 변경
* 물리적인 시스템 분리만으로는 결합이 느슨해졌다고 볼 수는 없다. -> 대상 도메인을 호출해야한다는 의도가 남아있기 때문
#### 비동기 호출 : 물리적 결합 제거
```java
pubic void initCerticationOwn(MemberNumber memberNumber) {
	member.initCerticationOwn(memberNumber);
	familyClient.leave(memberNumber);
}

class FamilyClient {
	@Async
	void leave(MemberNumber memberNumber) {}
}
```
* 물리적인 의존을 제거하는 대표적인 방법 : 비동기  방식(대표적) -> 별도 스레드에서 진행되기 때문에 주 흐름과 직접적인 결합이 제거
* 시스템 관점에서는 결합이 느슨해졌다고 볼 수 없다 -> 여전히 별도 스레드에서 대상 도메인을 호출한다는 의도가 남아있기 때문
#### 메시지 발송 : 물리적 결합 제거
```java
pubic void initCerticationOwn(MemberNumber memberNumber) {
	member.initCerticationOwn(memberNumber);
	eventPublisher.familyLeave(memberNumber);
}
```
* 회원의 본인인증 해제가 발생할 때 가족계정 탈퇴 메시지를 발송 : 메시지 발송 = 물리적 결합 제거
* 결합이 느슨해져지 않았다 : 논리적인 의존관계가 남아있기 때문 = 물리적으로는 결합도가 높지 않지만 개념적으로는 결합도가 높은 상태
* 메시징 시스템으로 보낸 메시지가 대상 도메인에게 기대하는 목적을 담았다면, 이것은 이벤트 X. 이것은 메시징 시스템을 이용한 비동기 요청일 뿐

#### 논리적 결합 제거
```java
pubic void initCerticationOwn(MemberNumber memberNumber) {
	member.initCerticationOwn(memberNumber);
	eventPublisher.initCerticationOwn(memberNumber);
}

class Family {
	public void listenMemberOwnInitEvent(MemberNumber memberNumber) {
		family.leave(memberNumber);
	}
}
```
* 발행해야할 이벤트는 도메인 이벤트로 인해 달성하려는 목적이 아닌 도메인 이벤트 그 자체
* 회원시스템은 가족계정 시스템의 정책을 모른다
##### 이렇게 바뀜으로서의 장점?
* 확장 가능성 : 본인 인증 해제가 될 때, 다른 도메인에서 작업이 필요할 경우 동일하게 이벤트 핸들러로 처리 가능
* 회원시스템 코드도 깔끔해진다 : 논리적으로 회원시스템이 처리할 로직은 회원시스템에서 처리. 다른 도메인의 로직은 알 필요 X
⸻

정리: 어떤 상황에 어떤 방법이 적합할까?

| 구분 | 이벤트 핸들러 | AOP |
|--|--|--|
| 관심사 분리 목적 | 도메인 이벤트 분리 | 횡단 관심사 (로깅, 인증 등) |
| 강점 | 비동기/비결합 구조, 유연한 확장성 | 코드 외부에서 공통 로직 주입 |
| 단점 | 디버깅 어려움, 흐름 추적 어려움 | 비즈니스 흐름 개입에는 부적절 |
| 추천 시점 | 부수 기능(포인트, 알림, 통계 등)이 많아질 때 | 단일 행위에 공통 기능 삽입 시 |

### 내 생각
* 이 둘을 비교한다기 보다는 각 요청상황에 맞게 이벤트 핸들러와 AOP를 적절히 조합하여 사용하면 될 것 같음.
* ![공장 컨베이어 시스템](img.png)
* 생산라인을 생각하면 앞에서 뭐가 일어나든 나한테 온 상태만 중요함
* 공장 생산라인이 더 좋은 방법이 있다면 더 발전 했을텐데 해당 방식을 고수하는 것을 보면 현재는 이게 최선인 것 같음?ㅋ
