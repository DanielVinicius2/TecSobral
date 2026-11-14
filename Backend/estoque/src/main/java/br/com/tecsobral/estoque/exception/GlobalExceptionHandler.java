package br.com.tecsobral.estoque.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.tecsobral.estoque.dto.ServiceDtos.SaldoNegativoItem;

@RestControllerAdvice
public class GlobalExceptionHandler {
	public record ErroApi(String codigo, String mensagem, List<String> detalhes) {}
	public record ConfirmacaoApi(String codigo, String mensagem, List<SaldoNegativoItem> itens) {}

	@ExceptionHandler(RecursoNaoEncontrado.class)
	public ResponseEntity<ErroApi> naoEncontrado(RecursoNaoEncontrado ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErroApi("NAO_ENCONTRADO", ex.getMessage(), List.of()));
	}

	@ExceptionHandler(ConfirmacaoSaldoNegativo.class)
	public ResponseEntity<ConfirmacaoApi> saldoNegativo(ConfirmacaoSaldoNegativo ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ConfirmacaoApi("CONFIRMACAO_SALDO_NEGATIVO", ex.getMessage(), ex.getItens()));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErroApi> credenciais(BadCredentialsException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ErroApi("CREDENCIAIS_INVALIDAS", "Login ou senha inválidos.", List.of()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroApi> validacao(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();
		return ResponseEntity.badRequest().body(new ErroApi("DADOS_INVALIDOS", "Revise os campos informados.", details));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroApi> conflito(DataIntegrityViolationException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ErroApi("CONFLITO_DE_DADOS", "A operação conflita com um registro ou regra do banco.", List.of()));
	}

	@ExceptionHandler(RegraNegocioException.class)
	public ResponseEntity<ErroApi> regra(RegraNegocioException ex) {
		return ResponseEntity.badRequest().body(new ErroApi("REGRA_DE_NEGOCIO", ex.getMessage(), List.of()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErroApi> argumento(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(new ErroApi("ARGUMENTO_INVALIDO", ex.getMessage(), List.of()));
	}

	@ExceptionHandler(ArithmeticException.class)
	public ResponseEntity<ErroApi> quantidade(ArithmeticException ex) {
		return ResponseEntity.badRequest().body(new ErroApi("QUANTIDADE_FORA_DO_LIMITE", "A quantidade informada ultrapassa o limite suportado.", List.of()));
	}
}
