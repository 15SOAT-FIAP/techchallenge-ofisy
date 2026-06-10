package br.com.ofisy.application.serviceorder.reprovequote;

import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReproveServiceOrderQuoteService implements ReproveServiceOrderQuoteUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final QuoteService quoteService;

    @Override
    @Transactional
    public QuoteResponseDTO execute(ReproveQuoteCommand cmd) {
        QuoteResponseDTO quoteResponseDTO = quoteService.reprove(cmd.quoteId(), cmd.requestDTO());
        ServiceOrder serviceOrder = serviceOrderRepository.findById(quoteResponseDTO.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(quoteResponseDTO.serviceOrderId()));
        serviceOrder.cancel();
        serviceOrderRepository.save(serviceOrder);
        return quoteResponseDTO;
    }
}