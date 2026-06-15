package br.com.ofisy.application.serviceorder.generatequote;

import br.com.ofisy.application.notification.createquote.CreateQuoteNotificationUseCase;
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
public class GenerateServiceOrderQuoteService implements GenerateServiceOrderQuoteUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final QuoteService quoteService;
    private final CreateQuoteNotificationUseCase createQuoteNotificationUseCase;

    @Override
    @Transactional
    public QuoteResponseDTO execute(GenerateQuoteCommand cmd) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(cmd.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(cmd.serviceOrderId()));
        QuoteResponseDTO quote = quoteService.create(cmd.serviceOrderId(), cmd.request());
        serviceOrder.sendToApproval();
        serviceOrderRepository.save(serviceOrder);
        createQuoteNotificationUseCase.execute(
                new CreateQuoteNotificationUseCase.CreateQuoteCommand(
                        quote.id(),
                        cmd.serviceOrderId(),
                        quote.totalPrice()
                )
        );
        return quote;
    }
}