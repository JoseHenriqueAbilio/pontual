package br.com.guiaestabelecimentos.repository;

import br.com.guiaestabelecimentos.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {
}

