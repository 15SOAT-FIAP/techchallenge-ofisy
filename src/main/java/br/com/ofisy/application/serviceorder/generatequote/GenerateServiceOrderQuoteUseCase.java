package br.com.ofisy.application.serviceorder.generatequote;

import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;

import java.util.UUID;

public interface GenerateServiceOrderQuoteUseCase {

    QuoteResponseDTO execute(GenerateQuoteCommand cmd);

    record GenerateQuoteCommand(
            UUID serviceOrderId,
            CreateQuoteRequestDTO request
    ) {}
}