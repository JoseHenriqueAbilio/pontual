package br.com.guiaestabelecimentos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;


@Entity
@Table(name = "estabelecimento", schema = "public")
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, columnDefinition = "text")
    private String categoria;

    @Column(columnDefinition = "text")
    private String telefone;

    @Column(columnDefinition = "text")
    private String endereco;


    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(nullable = false, columnDefinition = "geography(Point,4326)")
    @JsonIgnore
    private Point localizacao;


    @Transient
    private Double latitude;

    @Transient
    private Double longitude;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public Point getLocalizacao() { return localizacao; }
    public void setLocalizacao(Point localizacao) { this.localizacao = localizacao; }

    public Double getLatitude() {
        if (localizacao != null) {
            return localizacao.getY();
        }
        return latitude;
    }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() {
        if (localizacao != null) {
            return localizacao.getX();
        }
        return longitude;
    }

    public void setLongitude(Double longitude) { this.longitude = longitude; }
}

