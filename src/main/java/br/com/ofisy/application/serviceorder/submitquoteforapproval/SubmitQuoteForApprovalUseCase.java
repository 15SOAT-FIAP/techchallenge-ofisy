package br.com.ofisy.application.serviceorder.submitquoteforapproval;

import br.com.ofisy.domain.serviceorder.ServiceOrder;

import java.util.UUID;

public interface SubmitQuoteForApprovalUseCase {

    ServiceOrder execute(UUID serviceOrderId);
}