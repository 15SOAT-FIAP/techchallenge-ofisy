package br.com.ofisy.application.serviceorder.reprovequote;

import br.com.ofisy.application.quote.reprove.ReproveQuoteUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReproveServiceOrderQuoteService implements ReproveServiceOrderQuoteUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ReproveQuoteUseCase reproveQuoteUseCase;

    @Override
    @Transactional
    public Quote execute(ReproveServiceOrderQuoteCommand cmd) {
        Quote quote = reproveQuoteUseCase.execute(new ReproveQuoteUseCase.ReproveQuoteCommand(cmd.quoteId(), cmd.reason()));
        ServiceOrder serviceOrder = serviceOrderRepository.findById(quote.getServiceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(quote.getServiceOrderId()));
        serviceOrder.cancel();
        serviceOrderRepository.save(serviceOrder);
        return quote;
    }
}