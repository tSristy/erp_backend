package org.enterprise.pos.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.pos.restaurant.entity.KitchenOrderTicket;
import org.enterprise.pos.restaurant.entity.RestaurantOrderDetail;
import org.enterprise.pos.restaurant.entity.RestaurantOrder;
import org.enterprise.pos.restaurant.repository.KitchenOrderTicketRepository;
import org.enterprise.pos.restaurant.repository.RestaurantOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KitchenOrderTicketService {

    private final KitchenOrderTicketRepository kotRepository;
    private final RestaurantOrderRepository orderRepository;

    public List<KitchenOrderTicket> findAllPendingKots() {
        // Return all pending and preparing KOTs
        return kotRepository.findAll().stream()
                .filter(kot -> kot.getStatus() == KitchenOrderTicket.KotStatus.PENDING || kot.getStatus() == KitchenOrderTicket.KotStatus.PREPARING)
                .toList();
    }

    @Transactional
    public KitchenOrderTicket markAsServed(Long kotId) {
        KitchenOrderTicket kot = kotRepository.findById(kotId)
                .orElseThrow(() -> new IllegalArgumentException("KOT not found"));

        kot.setStatus(KitchenOrderTicket.KotStatus.SERVED);
        
        // Mark all associated details as served
        for (RestaurantOrderDetail detail : kot.getDetails()) {
            detail.setStatus(RestaurantOrderDetail.DetailStatus.SERVED);
        }

        // Check if all KOTs for the parent order are served
        RestaurantOrder order = kot.getOrder();
        boolean allServed = order.getKots().stream()
                .allMatch(k -> k.getStatus() == KitchenOrderTicket.KotStatus.SERVED || k.getStatus() == KitchenOrderTicket.KotStatus.CANCELLED);
        
        if (allServed) {
            order.setStatus(RestaurantOrder.RestaurantOrderStatus.SERVED);
            orderRepository.save(order);
        }

        return kotRepository.save(kot);
    }
}
