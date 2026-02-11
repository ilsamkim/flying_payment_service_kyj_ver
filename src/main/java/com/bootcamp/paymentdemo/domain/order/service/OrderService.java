package com.bootcamp.paymentdemo.domain.order.service;

import com.bootcamp.paymentdemo.common.exception.ErrorEnum;
import com.bootcamp.paymentdemo.common.exception.ServiceErrorException;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.member.repository.MemberRepository;
import com.bootcamp.paymentdemo.domain.order.dto.CreateOrderRequest;
import com.bootcamp.paymentdemo.domain.order.dto.CreateOrderResponse;
import com.bootcamp.paymentdemo.domain.order.dto.OrderProduct;
import com.bootcamp.paymentdemo.domain.order.dto.SearchOrderResponse;
import com.bootcamp.paymentdemo.domain.order.entity.Order;
import com.bootcamp.paymentdemo.domain.order.entity.OrderNoSeq;
import com.bootcamp.paymentdemo.domain.order.entity.ProductOrder;
import com.bootcamp.paymentdemo.domain.order.repository.OrderNoSeqRepository;
import com.bootcamp.paymentdemo.domain.order.repository.OrderRepository;
import com.bootcamp.paymentdemo.domain.order.repository.ProductOrderRepository;
import com.bootcamp.paymentdemo.domain.product.entity.Product;
import com.bootcamp.paymentdemo.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ProductOrderRepository productOrderRepository;
    private final OrderNoSeqRepository orderNoSeqRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, String email) {
        Member member = memberRepository.findByEmailAndDeletedFalse(email).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));
        String orderNo = getNextOrderNoSeq();

        int totalOrderPrice = 0;

        // 주문 총 가격
        for (OrderProduct orderProduct : request.items()) {
            Product product = productRepository.findByProductIdAndDeletedFalse(orderProduct.productId()).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_ORDER_PRODUCT));
            totalOrderPrice += (int) (product.getPrice() * orderProduct.quantity());
        }

        // 초기 주문 셋업
        Order savedOrder = Order.register(member, orderNo, totalOrderPrice, totalOrderPrice, 0, (int) Math.floor(totalOrderPrice * member.getGrade().getPointRate() * 0.01));
        orderRepository.save(savedOrder);

        // 주문 상품 입력
        for (OrderProduct orderProduct : request.items()) {
            createProductOrder(orderProduct, savedOrder);
        }

        return new CreateOrderResponse(String.valueOf(savedOrder.getOrderId()), savedOrder.getTotalActualPrice(), savedOrder.getOrderNo());
    }

    private void createProductOrder(OrderProduct orderProduct, Order order) {
        Product product = productRepository.findByProductIdAndDeletedFalse(orderProduct.productId()).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_ORDER_PRODUCT));

        ProductOrder savedProductOrder = ProductOrder.register(product, order, product.getName(), product.getPrice(), orderProduct.quantity());
        productOrderRepository.save(savedProductOrder);
    }

    private String getNextOrderNoSeq() {
        // 순번 데이터가 없을 경우 생성 후 저장
        OrderNoSeq orderNoSeq = orderNoSeqRepository.findByOrderNoSeq(LocalDate.now())
                .orElseGet(() -> {
                    OrderNoSeq newSeq = OrderNoSeq.register(LocalDate.now());
                    return orderNoSeqRepository.save(newSeq);
                });

        String yyyyMMddStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 현 순번을 끌어와 저장
        String orderNo = String.format("%s-%s-%06d", "ORDER", yyyyMMddStr, orderNoSeq.getOrderNoSeq());

        // 다음 주문을 위해 순번 증가 후 저장
        orderNoSeq.nextSequence();
        orderNoSeqRepository.save(orderNoSeq);

        return orderNo;
    }

    @Transactional(readOnly = true)
    public List<SearchOrderResponse> searchOrderList(String email) {
        Member member = memberRepository.findByEmailAndDeletedFalse(email).orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_MEMBER));

        List<Order> orderList = orderRepository.findByMember(member);

        return orderList.stream().map(order ->
                new SearchOrderResponse(
                        order.getOrderNo()
                        , order.getOrderId().toString()
                        , order.getTotalOrderPrice()
                        , order.getUsePoint()
                        , order.getTotalActualPrice()
                        , order.getSavePoint()
                        , order.getCurrency().name()
                        , order.getStatus().name()
                        , order.getCreatedAt().toString()
                )
        ).toList();
    }

    @Transactional(readOnly = true)
    public Order getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(()-> new ServiceErrorException(ErrorEnum.ERR_NOT_FOUND_ORDER));
    }

    @Transactional
    public void completeOrder(String orderNo) {
        Order order = getOrderByOrderNo(orderNo);
        log.info("주문 완료 처리됨: {}", orderNo);
    }

}
