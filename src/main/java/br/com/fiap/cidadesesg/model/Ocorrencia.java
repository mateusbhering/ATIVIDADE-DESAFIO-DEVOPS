package br.com.fiap.cidadesesg.model;

import br.com.fiap.cidadesesg.exception.ObservacaoObrigatoriaException;
import br.com.fiap.cidadesesg.exception.TransicaoInvalidaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "ocorrencia")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String protocolo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOcorrencia tipo;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false, length = 100)
    private String bairro;

    @Column(nullable = false, length = 200)
    private String endereco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOcorrencia status;

    @Column(nullable = false)
    private Instant abertaEm;

    private Instant atendimentoIniciadoEm;

    private Instant encerradaEm;

    @Column(length = 500)
    private String observacao;

    protected Ocorrencia() {
        // exigido pelo JPA
    }

    public Ocorrencia(String protocolo, TipoOcorrencia tipo, String descricao,
                      String bairro, String endereco, Instant abertaEm) {
        this.protocolo = protocolo;
        this.tipo = tipo;
        this.descricao = descricao;
        this.bairro = bairro;
        this.endereco = endereco;
        this.abertaEm = abertaEm;
        this.status = StatusOcorrencia.ABERTA;
    }

    /** Avanca o fluxo de atendimento validando as regras de negocio. */
    public void mudarStatus(StatusOcorrencia novo, String observacao, Instant agora) {
        if (!status.podeMudarPara(novo)) {
            throw new TransicaoInvalidaException(status, novo);
        }
        boolean temObservacao = observacao != null && !observacao.isBlank();
        if (novo.isEncerrada() && !temObservacao) {
            throw new ObservacaoObrigatoriaException(novo);
        }

        if (novo == StatusOcorrencia.EM_ATENDIMENTO) {
            atendimentoIniciadoEm = agora;
        } else {
            encerradaEm = agora;
        }
        if (temObservacao) {
            this.observacao = observacao.trim();
        }
        status = novo;
    }

    public Instant getPrazoLimite() {
        return abertaEm.plus(tipo.getPrazoHoras(), ChronoUnit.HOURS);
    }

    /** Pendente (ABERTA ou EM_ATENDIMENTO) com o prazo ja vencido. */
    public boolean isAtrasada(Instant agora) {
        return status.isPendente() && agora.isAfter(getPrazoLimite());
    }

    public boolean isResolvidaNoPrazo() {
        return status == StatusOcorrencia.RESOLVIDA && !encerradaEm.isAfter(getPrazoLimite());
    }

    /** Tempo entre abertura e resolucao; vazio se a ocorrencia nao foi resolvida. */
    public Duration getTempoResolucao() {
        return status == StatusOcorrencia.RESOLVIDA ? Duration.between(abertaEm, encerradaEm) : null;
    }

    public Secretaria getSecretaria() {
        return tipo.getSecretaria();
    }

    public Long getId() {
        return id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public TipoOcorrencia getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getBairro() {
        return bairro;
    }

    public String getEndereco() {
        return endereco;
    }

    public StatusOcorrencia getStatus() {
        return status;
    }

    public Instant getAbertaEm() {
        return abertaEm;
    }

    public Instant getAtendimentoIniciadoEm() {
        return atendimentoIniciadoEm;
    }

    public Instant getEncerradaEm() {
        return encerradaEm;
    }

    public String getObservacao() {
        return observacao;
    }
}
