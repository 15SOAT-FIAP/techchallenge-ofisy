package br.com.ofisy.application.serviceorder.approvequote;

import br.com.ofisy.application.quote.dto.QuoteResponseDTO;

import java.util.UUID;

public interface ApproveServiceOrderQuoteUseCase {

    QuoteResponseDTO execute(UUID quoteId);
}