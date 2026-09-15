package com.example.bookwithticket.cart.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.cart.dto.PerformanceCartItemDto;
import com.example.bookwithticket.cart.entity.CartEntity;
import com.example.bookwithticket.cart.entity.PerformanceCartItemEntity;
import com.example.bookwithticket.cart.repository.CartRepository;
import com.example.bookwithticket.cart.repository.PerformanceCartItemRepository;
import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.domain.performance.PerformanceScheduleRepository;

@Service
@Transactional
public class PerformanceCartServiceImpl
        implements PerformanceCartService {

    private final CartRepository cartRepository;

    private final PerformanceCartItemRepository performanceCartItemRepository;

    private final PerformanceScheduleRepository performanceScheduleRepository;

    public PerformanceCartServiceImpl(
    		CartRepository cartRepository,
            PerformanceCartItemRepository performanceCartItemRepository,
            PerformanceScheduleRepository performanceScheduleRepository
    ) {
        this.cartRepository = cartRepository;
        this.performanceCartItemRepository = performanceCartItemRepository;
        this.performanceScheduleRepository = performanceScheduleRepository;
    }

    @Override
    public void addCartItem(Long memberId, Long performanceScheduleId) {
        if (performanceScheduleId == null) {
            throw new IllegalArgumentException("공연 회차 정보가 없습니다.");
        }

        PerformanceSchedule schedule =
                performanceScheduleRepository
                        .findById(performanceScheduleId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("존재하지 않는 공연 회차입니다.")
                        );

        if (!LocalDateTime.now().isBefore(
                schedule.getPerformanceTime()
        )) {
            throw new IllegalArgumentException("예매 기간이 만료된 공연입니다.");
        }

        CartEntity cart =
                cartRepository
                        .findByMemberId(memberId)
                        .orElseGet(() ->
                                cartRepository.save(
                                        new CartEntity(memberId)
                                )
                        );

        boolean alreadyExists =
                performanceCartItemRepository
                        .existsByCartIdAndPerformanceScheduleId(
                                cart.getId(),
                                performanceScheduleId
                        );

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 장바구니에 담긴 공연 회차입니다.");
        }

        PerformanceCartItemEntity cartItem = new PerformanceCartItemEntity(cart, schedule);

        performanceCartItemRepository.save(cartItem);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PerformanceCartItemDto> getCartItems(Long memberId) {
        return performanceCartItemRepository
                .findAllByCartMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(PerformanceCartItemDto::new)
                .toList();
    }

    @Override
    public void deleteCartItem(Long memberId, Long cartItemId) {
        PerformanceCartItemEntity cartItem =
                performanceCartItemRepository
                        .findByIdAndCartMemberId(cartItemId, memberId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("삭제할 수 없는 공연 장바구니 항목입니다.")
                        );

        performanceCartItemRepository.delete(cartItem);
    }

    @Transactional
    @Override
    public void deleteAllItems(Long memberId) {
        performanceCartItemRepository.deleteByCartMemberId(memberId);
    }
}