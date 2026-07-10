package br.com.ofisy.application.serviceorder.submitquoteforapproval;

import br.com.ofisy.application.serviceorder.exceptions.QuoteNotFoundForServiceOrderException;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SubmitQuoteForApprovalService implements SubmitQuoteForApprovalUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final QuoteRepository quoteRepository;

    public SubmitQuoteForApprovalService(ServiceOrderRepository serviceOrderRepository,
                                         QuoteRepository quoteRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.quoteRepository = quoteRepository;
    }

    @Override
    public ServiceOrder execute(UUID serviceOrderId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        List<Quote> quotes = quoteRepository.findByServiceOrderId(serviceOrderId);
        if (quotes.isEmpty()) {
            throw new QuoteNotFoundForServiceOrderException(serviceOrderId);
        }

        serviceOrder.sendToApproval();
        return serviceOrderRepository.save(serviceOrder);
    }
}