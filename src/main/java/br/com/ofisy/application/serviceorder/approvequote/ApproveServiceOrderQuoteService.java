package br.com.ofisy.application.serviceorder.approvequote;

import br.com.ofisy.application.quote.approve.ApproveQuoteUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.config.metrics.ServiceOrderMetrics;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApproveServiceOrderQuoteService implements ApproveServiceOrderQuoteUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ApproveQuoteUseCase approveQuoteUseCase;
    private final ServiceOrderMetrics serviceOrderMetrics;

    @Override
    @Transactional
    public Quote execute(UUID quoteId) {
        Quote quote = approveQuoteUseCase.execute(quoteId);
        ServiceOrder serviceOrder = serviceOrderRepository.findById(quote.getServiceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(quote.getServiceOrderId()));
        ServiceOrderStatus previousStatus = serviceOrder.getStatus();
        LocalDateTime enteredFromStatusAt = serviceOrder.getUpdatedAt();
        serviceOrder.approve();
        ServiceOrder saved = serviceOrderRepository.save(serviceOrder);
        serviceOrderMetrics.recordTransition(previousStatus, saved.getStatus(), enteredFromStatusAt);
        return quote;
    }
}