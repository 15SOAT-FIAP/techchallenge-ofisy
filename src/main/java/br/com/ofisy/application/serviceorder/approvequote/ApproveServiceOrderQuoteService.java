package br.com.ofisy.application.serviceorder.approvequote;

import br.com.ofisy.application.quote.approve.ApproveQuoteUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApproveServiceOrderQuoteService implements ApproveServiceOrderQuoteUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ApproveQuoteUseCase approveQuoteUseCase;

    @Override
    @Transactional
    public Quote execute(UUID quoteId) {
        Quote quote = approveQuoteUseCase.execute(quoteId);
        ServiceOrder serviceOrder = serviceOrderRepository.findById(quote.getServiceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(quote.getServiceOrderId()));
        serviceOrder.approve();
        serviceOrderRepository.save(serviceOrder);
        return quote;
    }
}