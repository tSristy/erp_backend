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

    @Transactional(readOnly = true)
    public List<KitchenOrderTicket> findAllPendingKots() {
        List<KitchenOrderTicket> raw = kotRepository.findByStatusInWithDetails(
                List.of(KitchenOrderTicket.KotStatus.PENDING, KitchenOrderTicket.KotStatus.PREPARING)
        );
        java.util.Map<String, KitchenOrderTicket> dedup = new java.util.LinkedHashMap<>();
        for (KitchenOrderTicket kot : raw) {
            String key = kot.getKotNumber() != null ? kot.getKotNumber() : String.valueOf(kot.getId());
            if (!dedup.containsKey(key)) {
                dedup.put(key, kot);
            }
        }
        return new java.util.ArrayList<>(dedup.values());
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

    public org.springframework.data.domain.Page<KitchenOrderTicket> searchKitchenOrderTicketDtos(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        return kotRepository.findAll(pageable);
    }

}