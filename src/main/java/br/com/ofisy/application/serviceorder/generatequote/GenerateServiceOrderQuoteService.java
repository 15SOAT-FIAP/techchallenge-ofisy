package br.com.ofisy.application.serviceorder.generatequote;

import br.com.ofisy.application.notification.createquote.CreateQuoteNotificationUseCase;
import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
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
public class GenerateServiceOrderQuoteService implements GenerateServiceOrderQuoteUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final CreateQuoteUseCase createQuoteUseCase;
    private final CreateQuoteNotificationUseCase createQuoteNotificationUseCase;
    private final ServiceOrderMetrics serviceOrderMetrics;

    @Override
    @Transactional
    public Quote execute(GenerateQuoteCommand cmd) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(cmd.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(cmd.serviceOrderId()));

        Quote quote = createQuoteUseCase.execute(
                new CreateQuoteUseCase.CreateQuoteCommand(
                        cmd.serviceOrderId(), cmd.stockItems(), cmd.serviceItems()));

        ServiceOrderStatus previousStatus = serviceOrder.getStatus();
        LocalDateTime enteredFromStatusAt = serviceOrder.getUpdatedAt();
        serviceOrder.sendToApproval();
        ServiceOrder saved = serviceOrderRepository.save(serviceOrder);
        serviceOrderMetrics.recordTransition(previousStatus, saved.getStatus(), enteredFromStatusAt);

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