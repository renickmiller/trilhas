package br.trilhas.ufpa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.trilhas.ufpa.IntegrationTest;
import br.trilhas.ufpa.domain.Fotografias;
import br.trilhas.ufpa.repository.FotografiasRepository;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link FotografiasResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class FotografiasResourceIT {

    private static final String DEFAULT_DESCRICAO = "AAAAAAAAAA";
    private static final String UPDATED_DESCRICAO = "BBBBBBBBBB";

    private static final String DEFAULT_AUTOR = "AAAAAAAAAA";
    private static final String UPDATED_AUTOR = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/fotografias";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private FotografiasRepository fotografiasRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFotografiasMockMvc;

    private Fotografias fotografias;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Fotografias createEntity(EntityManager em) {
        Fotografias fotografias = new Fotografias().descricao(DEFAULT_DESCRICAO).autor(DEFAULT_AUTOR);
        return fotografias;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Fotografias createUpdatedEntity(EntityManager em) {
        Fotografias fotografias = new Fotografias().descricao(UPDATED_DESCRICAO).autor(UPDATED_AUTOR);
        return fotografias;
    }

    @BeforeEach
    public void initTest() {
        fotografias = createEntity(em);
    }

    @Test
    @Transactional
    void createFotografias() throws Exception {
        int databaseSizeBeforeCreate = fotografiasRepository.findAll().size();
        // Create the Fotografias
        restFotografiasMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fotografias)))
            .andExpect(status().isCreated());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeCreate + 1);
        Fotografias testFotografias = fotografiasList.get(fotografiasList.size() - 1);
        assertThat(testFotografias.getDescricao()).isEqualTo(DEFAULT_DESCRICAO);
        assertThat(testFotografias.getAutor()).isEqualTo(DEFAULT_AUTOR);
    }

    @Test
    @Transactional
    void createFotografiasWithExistingId() throws Exception {
        // Create the Fotografias with an existing ID
        fotografias.setId(1L);

        int databaseSizeBeforeCreate = fotografiasRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFotografiasMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fotografias)))
            .andExpect(status().isBadRequest());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllFotografias() throws Exception {
        // Initialize the database
        fotografiasRepository.saveAndFlush(fotografias);

        // Get all the fotografiasList
        restFotografiasMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fotografias.getId().intValue())))
            .andExpect(jsonPath("$.[*].descricao").value(hasItem(DEFAULT_DESCRICAO)))
            .andExpect(jsonPath("$.[*].autor").value(hasItem(DEFAULT_AUTOR)));
    }

    @Test
    @Transactional
    void getFotografias() throws Exception {
        // Initialize the database
        fotografiasRepository.saveAndFlush(fotografias);

        // Get the fotografias
        restFotografiasMockMvc
            .perform(get(ENTITY_API_URL_ID, fotografias.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(fotografias.getId().intValue()))
            .andExpect(jsonPath("$.descricao").value(DEFAULT_DESCRICAO))
            .andExpect(jsonPath("$.autor").value(DEFAULT_AUTOR));
    }

    @Test
    @Transactional
    void getNonExistingFotografias() throws Exception {
        // Get the fotografias
        restFotografiasMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewFotografias() throws Exception {
        // Initialize the database
        fotografiasRepository.saveAndFlush(fotografias);

        int databaseSizeBeforeUpdate = fotografiasRepository.findAll().size();

        // Update the fotografias
        Fotografias updatedFotografias = fotografiasRepository.findById(fotografias.getId()).get();
        // Disconnect from session so that the updates on updatedFotografias are not directly saved in db
        em.detach(updatedFotografias);
        updatedFotografias.descricao(UPDATED_DESCRICAO).autor(UPDATED_AUTOR);

        restFotografiasMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedFotografias.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedFotografias))
            )
            .andExpect(status().isOk());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeUpdate);
        Fotografias testFotografias = fotografiasList.get(fotografiasList.size() - 1);
        assertThat(testFotografias.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testFotografias.getAutor()).isEqualTo(UPDATED_AUTOR);
    }

    @Test
    @Transactional
    void putNonExistingFotografias() throws Exception {
        int databaseSizeBeforeUpdate = fotografiasRepository.findAll().size();
        fotografias.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFotografiasMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fotografias.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(fotografias))
            )
            .andExpect(status().isBadRequest());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFotografias() throws Exception {
        int databaseSizeBeforeUpdate = fotografiasRepository.findAll().size();
        fotografias.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFotografiasMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(fotografias))
            )
            .andExpect(status().isBadRequest());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFotografias() throws Exception {
        int databaseSizeBeforeUpdate = fotografiasRepository.findAll().size();
        fotografias.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFotografiasMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fotografias)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFotografiasWithPatch() throws Exception {
        // Initialize the database
        fotografiasRepository.saveAndFlush(fotografias);

        int databaseSizeBeforeUpdate = fotografiasRepository.findAll().size();

        // Update the fotografias using partial update
        Fotografias partialUpdatedFotografias = new Fotografias();
        partialUpdatedFotografias.setId(fotografias.getId());

        partialUpdatedFotografias.autor(UPDATED_AUTOR);

        restFotografiasMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFotografias.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFotografias))
            )
            .andExpect(status().isOk());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeUpdate);
        Fotografias testFotografias = fotografiasList.get(fotografiasList.size() - 1);
        assertThat(testFotografias.getDescricao()).isEqualTo(DEFAULT_DESCRICAO);
        assertThat(testFotografias.getAutor()).isEqualTo(UPDATED_AUTOR);
    }

    @Test
    @Transactional
    void fullUpdateFotografiasWithPatch() throws Exception {
        // Initialize the database
        fotografiasRepository.saveAndFlush(fotografias);

        int databaseSizeBeforeUpdate = fotografiasRepository.findAll().size();

        // Update the fotografias using partial update
        Fotografias partialUpdatedFotografias = new Fotografias();
        partialUpdatedFotografias.setId(fotografias.getId());

        partialUpdatedFotografias.descricao(UPDATED_DESCRICAO).autor(UPDATED_AUTOR);

        restFotografiasMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFotografias.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFotografias))
            )
            .andExpect(status().isOk());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeUpdate);
        Fotografias testFotografias = fotografiasList.get(fotografiasList.size() - 1);
        assertThat(testFotografias.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testFotografias.getAutor()).isEqualTo(UPDATED_AUTOR);
    }

    @Test
    @Transactional
    void patchNonExistingFotografias() throws Exception {
        int databaseSizeBeforeUpdate = fotografiasRepository.findAll().size();
        fotografias.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFotografiasMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, fotografias.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(fotografias))
            )
            .andExpect(status().isBadRequest());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFotografias() throws Exception {
        int databaseSizeBeforeUpdate = fotografiasRepository.findAll().size();
        fotografias.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFotografiasMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(fotografias))
            )
            .andExpect(status().isBadRequest());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFotografias() throws Exception {
        int databaseSizeBeforeUpdate = fotografiasRepository.findAll().size();
        fotografias.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFotografiasMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(fotografias))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Fotografias in the database
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFotografias() throws Exception {
        // Initialize the database
        fotografiasRepository.saveAndFlush(fotografias);

        int databaseSizeBeforeDelete = fotografiasRepository.findAll().size();

        // Delete the fotografias
        restFotografiasMockMvc
            .perform(delete(ENTITY_API_URL_ID, fotografias.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Fotografias> fotografiasList = fotografiasRepository.findAll();
        assertThat(fotografiasList).hasSize(databaseSizeBeforeDelete - 1);
    }
}