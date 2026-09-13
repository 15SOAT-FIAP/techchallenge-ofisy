package br.com.ofisy.application.serviceorder.reprovequote;

import br.com.ofisy.application.quote.reprove.ReproveQuoteUseCase;
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

@Service
@RequiredArgsConstructor
public class ReproveServiceOrderQuoteService implements ReproveServiceOrderQuoteUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ReproveQuoteUseCase reproveQuoteUseCase;
    private final ServiceOrderMetrics serviceOrderMetrics;

    @Override
    @Transactional
    public Quote execute(ReproveServiceOrderQuoteCommand cmd) {
        Quote quote = reproveQuoteUseCase.execute(new ReproveQuoteUseCase.ReproveQuoteCommand(cmd.quoteId(), cmd.reason()));
        ServiceOrder serviceOrder = serviceOrderRepository.findById(quote.getServiceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(quote.getServiceOrderId()));
        ServiceOrderStatus previousStatus = serviceOrder.getStatus();
        LocalDateTime enteredFromStatusAt = serviceOrder.getUpdatedAt();
        serviceOrder.cancel();
        ServiceOrder saved = serviceOrderRepository.save(serviceOrder);
        serviceOrderMetrics.recordTransition(previousStatus, saved.getStatus(), enteredFromStatusAt);
        return quote;
    }
}