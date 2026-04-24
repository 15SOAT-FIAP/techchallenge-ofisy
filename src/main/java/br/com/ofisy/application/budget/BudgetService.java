package br.com.ofisy.application.budget;

import br.com.ofisy.application.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final NotificationService notificationService;

    @Transactional
    public UUID generateBudget(String budgetNumber, String customerName, Double totalValue) {
        // Aqui teria a lógica de salvar o orçamento no banco
        // UUID budgetId = budgetRepository.save(...);

        // Cria notificação de orçamento gerado
        notificationService.createBudgetNotification(budgetNumber, customerName, totalValue);

        return UUID.randomUUID(); // retornaria o ID do orçamento criado
    }
}
