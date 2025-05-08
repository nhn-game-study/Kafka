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

