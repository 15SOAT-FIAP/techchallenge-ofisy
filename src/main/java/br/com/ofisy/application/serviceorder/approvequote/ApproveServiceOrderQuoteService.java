package br.com.ofisy.application.serviceorder.approvequote;

import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
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
    private final QuoteService quoteService;

    @Override
    @Transactional
    public QuoteResponseDTO execute(UUID quoteId) {
        QuoteResponseDTO quoteResponseDTO = quoteService.approve(quoteId);
        ServiceOrder serviceOrder = serviceOrderRepository.findById(quoteResponseDTO.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(quoteResponseDTO.serviceOrderId()));
        serviceOrder.approve();
        serviceOrderRepository.save(serviceOrder);
        return quoteResponseDTO;
    }
}