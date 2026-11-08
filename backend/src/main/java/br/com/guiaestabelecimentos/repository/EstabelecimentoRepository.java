package br.com.guiaestabelecimentos.repository;

import br.com.guiaestabelecimentos.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {


    @Query(value = """
            SELECT e.* FROM public.estabelecimento e
            WHERE ST_DWithin(
                e.localizacao,
                CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography),
                :distancia
            )
            """, nativeQuery = true)
    List<Estabelecimento> buscarProximos(@Param("lat") double latitude,
                                       @Param("lng") double longitude,
                                       @Param("distancia") double distancia);

    @Query(value = """
            SELECT e.* FROM public.estabelecimento e
            WHERE ST_Intersects(
                CAST(e.localizacao AS geometry),
                ST_MakeEnvelope(:oeste, :sul, :leste, :norte, 4326)
            )
            """, nativeQuery = true)
    List<Estabelecimento> buscarPorArea(@Param("sul") double sul,
                                      @Param("norte") double norte,
                                      @Param("oeste") double oeste,
                                      @Param("leste") double leste);
}

//essa parte nos precisamos de IA
