package br.trilhas.ufpa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.trilhas.ufpa.IntegrationTest;
import br.trilhas.ufpa.domain.CadastroTrilha;
import br.trilhas.ufpa.repository.CadastroTrilhaRepository;
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
 * Integration tests for the {@link CadastroTrilhaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CadastroTrilhaResourceIT {

    private static final String DEFAULT_DATA_HORA = "AAAAAAAAAA";
    private static final String UPDATED_DATA_HORA = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/cadastro-trilhas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CadastroTrilhaRepository cadastroTrilhaRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCadastroTrilhaMockMvc;

    private CadastroTrilha cadastroTrilha;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CadastroTrilha createEntity(EntityManager em) {
        CadastroTrilha cadastroTrilha = new CadastroTrilha().dataHora(DEFAULT_DATA_HORA);
        return cadastroTrilha;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CadastroTrilha createUpdatedEntity(EntityManager em) {
        CadastroTrilha cadastroTrilha = new CadastroTrilha().dataHora(UPDATED_DATA_HORA);
        return cadastroTrilha;
    }

    @BeforeEach
    public void initTest() {
        cadastroTrilha = createEntity(em);
    }

    @Test
    @Transactional
    void createCadastroTrilha() throws Exception {
        int databaseSizeBeforeCreate = cadastroTrilhaRepository.findAll().size();
        // Create the CadastroTrilha
        restCadastroTrilhaMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cadastroTrilha))
            )
            .andExpect(status().isCreated());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeCreate + 1);
        CadastroTrilha testCadastroTrilha = cadastroTrilhaList.get(cadastroTrilhaList.size() - 1);
        assertThat(testCadastroTrilha.getDataHora()).isEqualTo(DEFAULT_DATA_HORA);
    }

    @Test
    @Transactional
    void createCadastroTrilhaWithExistingId() throws Exception {
        // Create the CadastroTrilha with an existing ID
        cadastroTrilha.setId(1L);

        int databaseSizeBeforeCreate = cadastroTrilhaRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCadastroTrilhaMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cadastroTrilha))
            )
            .andExpect(status().isBadRequest());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCadastroTrilhas() throws Exception {
        // Initialize the database
        cadastroTrilhaRepository.saveAndFlush(cadastroTrilha);

        // Get all the cadastroTrilhaList
        restCadastroTrilhaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cadastroTrilha.getId().intValue())))
            .andExpect(jsonPath("$.[*].dataHora").value(hasItem(DEFAULT_DATA_HORA)));
    }

    @Test
    @Transactional
    void getCadastroTrilha() throws Exception {
        // Initialize the database
        cadastroTrilhaRepository.saveAndFlush(cadastroTrilha);

        // Get the cadastroTrilha
        restCadastroTrilhaMockMvc
            .perform(get(ENTITY_API_URL_ID, cadastroTrilha.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cadastroTrilha.getId().intValue()))
            .andExpect(jsonPath("$.dataHora").value(DEFAULT_DATA_HORA));
    }

    @Test
    @Transactional
    void getNonExistingCadastroTrilha() throws Exception {
        // Get the cadastroTrilha
        restCadastroTrilhaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCadastroTrilha() throws Exception {
        // Initialize the database
        cadastroTrilhaRepository.saveAndFlush(cadastroTrilha);

        int databaseSizeBeforeUpdate = cadastroTrilhaRepository.findAll().size();

        // Update the cadastroTrilha
        CadastroTrilha updatedCadastroTrilha = cadastroTrilhaRepository.findById(cadastroTrilha.getId()).get();
        // Disconnect from session so that the updates on updatedCadastroTrilha are not directly saved in db
        em.detach(updatedCadastroTrilha);
        updatedCadastroTrilha.dataHora(UPDATED_DATA_HORA);

        restCadastroTrilhaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCadastroTrilha.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedCadastroTrilha))
            )
            .andExpect(status().isOk());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeUpdate);
        CadastroTrilha testCadastroTrilha = cadastroTrilhaList.get(cadastroTrilhaList.size() - 1);
        assertThat(testCadastroTrilha.getDataHora()).isEqualTo(UPDATED_DATA_HORA);
    }

    @Test
    @Transactional
    void putNonExistingCadastroTrilha() throws Exception {
        int databaseSizeBeforeUpdate = cadastroTrilhaRepository.findAll().size();
        cadastroTrilha.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCadastroTrilhaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cadastroTrilha.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cadastroTrilha))
            )
            .andExpect(status().isBadRequest());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCadastroTrilha() throws Exception {
        int databaseSizeBeforeUpdate = cadastroTrilhaRepository.findAll().size();
        cadastroTrilha.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCadastroTrilhaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cadastroTrilha))
            )
            .andExpect(status().isBadRequest());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCadastroTrilha() throws Exception {
        int databaseSizeBeforeUpdate = cadastroTrilhaRepository.findAll().size();
        cadastroTrilha.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCadastroTrilhaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cadastroTrilha)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCadastroTrilhaWithPatch() throws Exception {
        // Initialize the database
        cadastroTrilhaRepository.saveAndFlush(cadastroTrilha);

        int databaseSizeBeforeUpdate = cadastroTrilhaRepository.findAll().size();

        // Update the cadastroTrilha using partial update
        CadastroTrilha partialUpdatedCadastroTrilha = new CadastroTrilha();
        partialUpdatedCadastroTrilha.setId(cadastroTrilha.getId());

        restCadastroTrilhaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCadastroTrilha.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCadastroTrilha))
            )
            .andExpect(status().isOk());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeUpdate);
        CadastroTrilha testCadastroTrilha = cadastroTrilhaList.get(cadastroTrilhaList.size() - 1);
        assertThat(testCadastroTrilha.getDataHora()).isEqualTo(DEFAULT_DATA_HORA);
    }

    @Test
    @Transactional
    void fullUpdateCadastroTrilhaWithPatch() throws Exception {
        // Initialize the database
        cadastroTrilhaRepository.saveAndFlush(cadastroTrilha);

        int databaseSizeBeforeUpdate = cadastroTrilhaRepository.findAll().size();

        // Update the cadastroTrilha using partial update
        CadastroTrilha partialUpdatedCadastroTrilha = new CadastroTrilha();
        partialUpdatedCadastroTrilha.setId(cadastroTrilha.getId());

        partialUpdatedCadastroTrilha.dataHora(UPDATED_DATA_HORA);

        restCadastroTrilhaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCadastroTrilha.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCadastroTrilha))
            )
            .andExpect(status().isOk());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeUpdate);
        CadastroTrilha testCadastroTrilha = cadastroTrilhaList.get(cadastroTrilhaList.size() - 1);
        assertThat(testCadastroTrilha.getDataHora()).isEqualTo(UPDATED_DATA_HORA);
    }

    @Test
    @Transactional
    void patchNonExistingCadastroTrilha() throws Exception {
        int databaseSizeBeforeUpdate = cadastroTrilhaRepository.findAll().size();
        cadastroTrilha.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCadastroTrilhaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, cadastroTrilha.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cadastroTrilha))
            )
            .andExpect(status().isBadRequest());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCadastroTrilha() throws Exception {
        int databaseSizeBeforeUpdate = cadastroTrilhaRepository.findAll().size();
        cadastroTrilha.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCadastroTrilhaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cadastroTrilha))
            )
            .andExpect(status().isBadRequest());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCadastroTrilha() throws Exception {
        int databaseSizeBeforeUpdate = cadastroTrilhaRepository.findAll().size();
        cadastroTrilha.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCadastroTrilhaMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(cadastroTrilha))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CadastroTrilha in the database
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCadastroTrilha() throws Exception {
        // Initialize the database
        cadastroTrilhaRepository.saveAndFlush(cadastroTrilha);

        int databaseSizeBeforeDelete = cadastroTrilhaRepository.findAll().size();

        // Delete the cadastroTrilha
        restCadastroTrilhaMockMvc
            .perform(delete(ENTITY_API_URL_ID, cadastroTrilha.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CadastroTrilha> cadastroTrilhaList = cadastroTrilhaRepository.findAll();
        assertThat(cadastroTrilhaList).hasSize(databaseSizeBeforeDelete - 1);
    }
}