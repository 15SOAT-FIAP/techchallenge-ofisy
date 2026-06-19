package br.com.ofisy.application.serviceorder.generatequote;

import br.com.ofisy.application.notification.createquote.CreateQuoteNotificationUseCase;
import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerateServiceOrderQuoteService implements GenerateServiceOrderQuoteUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final CreateQuoteUseCase createQuoteUseCase;
    private final CreateQuoteNotificationUseCase createQuoteNotificationUseCase;

    @Override
    @Transactional
    public Quote execute(GenerateQuoteCommand cmd) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(cmd.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(cmd.serviceOrderId()));

        Quote quote = createQuoteUseCase.execute(
                new CreateQuoteUseCase.CreateQuoteCommand(
                        cmd.serviceOrderId(), cmd.stockItems(), cmd.serviceItems()));

        serviceOrder.sendToApproval();
        serviceOrderRepository.save(serviceOrder);

        createQuoteNotificationUseCase.execute(
                new CreateQuoteNotificationUseCase.CreateQuoteCommand(
                        quote.getId(),
                        cmd.serviceOrderId(),
                        quote.getTotalPrice()
                )
        );

        return quote;
    }
}